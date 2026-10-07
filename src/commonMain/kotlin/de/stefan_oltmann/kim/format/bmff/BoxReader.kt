/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2026 Ramon Bouckaert
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2002-2023 Drew Noakes and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.stefan_oltmann.kim.format.bmff

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.readUnsignedInt
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.box.Box
import de.stefan_oltmann.kim.format.bmff.box.FileTypeBox
import de.stefan_oltmann.kim.format.bmff.box.HandlerReferenceBox
import de.stefan_oltmann.kim.format.bmff.box.ItemInfoEntryBox
import de.stefan_oltmann.kim.format.bmff.box.ItemInformationBox
import de.stefan_oltmann.kim.format.bmff.box.ItemLocationBox
import de.stefan_oltmann.kim.format.bmff.box.MediaBox
import de.stefan_oltmann.kim.format.bmff.box.MediaDataBox
import de.stefan_oltmann.kim.format.bmff.box.MetaBox
import de.stefan_oltmann.kim.format.bmff.box.MetaBoxTopLevel
import de.stefan_oltmann.kim.format.bmff.box.MovieBox
import de.stefan_oltmann.kim.format.bmff.box.PrimaryItemBox
import de.stefan_oltmann.kim.format.bmff.box.TrackBox
import de.stefan_oltmann.kim.format.bmff.box.TrackHeaderBox
import de.stefan_oltmann.kim.format.bmff.box.UserDataBox
import de.stefan_oltmann.kim.format.bmff.box.UuidBox
import de.stefan_oltmann.kim.format.jxl.box.CompressedBox
import de.stefan_oltmann.kim.format.jxl.box.ExifBox
import de.stefan_oltmann.kim.format.jxl.box.JxlPartialCodestreamBox
import de.stefan_oltmann.kim.format.jxl.box.XmlBox
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read8BytesAsLong
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.output.ByteArrayByteWriter

/**
 * Reads ISOBMFF boxes.
 */
public object BoxReader {

    /*
     * Real files nest container boxes only a few levels deep
     * (moov > trak > mdia > meta), so this limit only rejects hostile input.
     */
    private const val MAX_BOX_DEPTH: Int = 16

    /** Chunk size for reading possibly-truncated box payloads. */
    private const val READ_CHUNK_SIZE: Long = 64 * 1024

    /*
     * The payload of every buffered box is held in memory, so like the
     * JPEG path's header segment budget, a box beyond this limit is
     * hostile input rather than a legitimate file: no real photo or
     * video carries metadata-sized boxes anywhere near it.
     */
    internal const val MAX_METADATA_BOX_BYTES: Int = 16 * 1024 * 1024

    /* The JXL codestream signature the first JXLP fragment starts with. */
    private const val JXL_HEADER_SIGNATURE_LENGTH: Int = 6

    /* The largesize form stores its 64-bit length behind the type. */
    private const val LARGESIZE_LENGTH: Int = 8

    /**
     * Reads all top-level boxes of the file completely, including the
     * image data payloads.
     *
     * **Attention:** Must be public API as this is used by https://stefan-oltmann.de/exif-viewer
     *
     * @param byteReader The reader as source for the bytes
     * @param offsetShift The shift to apply to the reported box offsets
     */
    public fun readAllBoxes(
        byteReader: ByteReader,
        offsetShift: Long = 0
    ): List<Box> =
        readBoxes(
            byteReader = byteReader,
            offsetShift = offsetShift
        )

    /**
     * Reads all top-level boxes for a rewrite that re-emits only the
     * parsed boxes, like the JPEG XL writer does.
     *
     * Bytes that end inside a box header are a truncated box: the scans
     * stop there as a clean boundary, but such a rewrite would silently
     * drop the fragment, and the clean boundary rule requires the raw
     * bytes to survive a rewrite byte-exact. This read fails instead.
     *
     * @param byteReader The reader as source for the bytes
     */
    internal fun readAllBoxesForRewrite(byteReader: ByteReader): List<Box> =
        readBoxes(
            byteReader = byteReader,
            rejectTrailingFragment = true
        )

    /**
     * Scans only the leading metadata boxes of the file top level, so the
     * image data block is not read in. The scan may continue past the meta
     * box while an XMP UUID box is still missing, because Samsung HEIC has
     * "meta" coming after "mdat".
     *
     * @param byteReader The reader as source for the bytes
     * @param updatePosition A callback to report the position when reading
     * has finished
     */
    internal fun scanMetadataBoxes(
        byteReader: ByteReader,
        updatePosition: ((Long) -> Unit)? = null
    ): List<Box> =
        readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = true,
            updatePosition = updatePosition
        )

    /**
     * Scans all top-level boxes of a video container without retaining the
     * media data, so metadata can be read from any position of arbitrarily
     * large video files. Media data and padding boxes are skipped in
     * bounded chunks instead of being buffered; a stream that ends inside
     * such a box - an interrupted recording - ends the scan with the boxes
     * parsed so far instead of failing the read.
     *
     * @param byteReader The reader as source for the bytes
     */
    internal fun scanVideoMetadataBoxes(byteReader: ByteReader): List<Box> =
        readBoxes(
            byteReader = byteReader,
            skipDataBoxPayloads = true
        )

    /**
     * Reads the leading boxes of a JPEG XL file for an update and stops
     * before the image data starts, so the image data can be streamed
     * without buffering the whole file. The first JXLP box contains the
     * codestream header, so every following JXLP box is image data. The
     * cut box is returned with an empty payload, because its content is
     * streamed by the caller.
     *
     * @param byteReader The reader as source for the bytes
     */
    internal fun readBoxesForUpdate(byteReader: ByteReader): List<Box> =
        readBoxes(
            byteReader = byteReader,
            stopBeforeImageData = true
        )

    /**
     * Reads the child boxes of the given container box.
     *
     * @param byteReader The reader as source for the bytes
     * @param parentBoxType The type of the container whose children are
     * read - a "meta" box below the top level needs to be treated
     * differently to a "meta" box at the top level
     * @param depth The nesting level of the children, used to bound the
     * recursion for hostile files that nest container boxes arbitrarily
     * @param offsetShift The shift to apply to the reported box offsets
     * @param positionOffset The position where to start reading boxes
     */
    internal fun readChildBoxes(
        byteReader: ByteReader,
        parentBoxType: BoxType,
        depth: Int,
        offsetShift: Long,
        positionOffset: Long = 0
    ): List<Box> =
        readBoxes(
            byteReader = byteReader,
            positionOffset = positionOffset,
            offsetShift = offsetShift,
            parentBoxType = parentBoxType,
            depth = depth
        )

    /**
     * Validates the declared size of a box before any position math
     * happens.
     *
     * A non-positive size, a size below the box's own 8-byte header and
     * a largesize below both headers would all rewind the scan position
     * and re-parse consumed bytes as boxes, or compute a negative
     * remaining length that the metadata scan would mistake for
     * truncation and silently stop mid-file. The streaming writer
     * rejects the same input.
     */
    private fun validateBoxLength(
        type: BoxType,
        size: Long,
        actualLength: Long
    ) {

        if (actualLength <= 0)
            throw ImageReadException("Box $type has an invalid size: $size.")

        if (actualLength < BMFFConstants.BOX_HEADER_LENGTH)
            throw ImageReadException(
                "Box $type declares a size smaller than its header: $size."
            )

        if (size == 1L && actualLength < 2 * BMFFConstants.BOX_HEADER_LENGTH)
            throw ImageReadException(
                "Box $type declares a largesize below its own header: $actualLength."
            )
    }

    /**
     * Ends the box walk at the end of the stream.
     *
     * Bytes that end inside a box header are a truncated box: the scans
     * stop there as a clean boundary, but a rewrite that re-emits only
     * the parsed boxes would silently drop the fragment - the clean
     * boundary rule requires the raw bytes to survive a rewrite
     * byte-exact, so the rewrite-feeding read fails instead.
     */
    private fun checkTrailingFragment(
        available: Long,
        rejectTrailingFragment: Boolean
    ) {

        if (available > 0 && rejectTrailingFragment)
            throw ImageReadException(
                "$available trailing bytes end inside a box header."
            )
    }

    /**
     * The result of reading a box payload: the bytes kept for the box
     * object (possibly empty for skipped payloads) and whether the
     * source ended inside the payload.
     */
    private class BoxPayloadResult(
        val bytes: ByteArray,
        val truncated: Boolean
    )

    /**
     * Reads one box payload according to the scan mode: skipped boxes
     * stream through in bounded chunks, the video-scan file-level meta
     * box is buffered with the metadata budget so its item children can
     * be identified, mdat on the metadata path is retained by the
     * CopyByteReader alone, and everything else buffers.
     */
    private fun readBoxPayload(
        byteReader: ByteReader,
        type: BoxType,
        remainingBytesToReadInThisBox: Long,
        skipDataBoxPayloads: Boolean,
        stopAfterMetadataRead: Boolean,
        haveSeenJxlHeaderBox: Boolean
    ): BoxPayloadResult {

        var payloadTruncated = false

        /*
         * The payload boxes the video scan buffers instead of skipping,
         * because their content feeds the metadata parse.
         */
        val isMetadataPayloadBox =
            type == BoxType.MOOV ||
                type == BoxType.UUID ||
                type == BoxType.XMP_ ||
                type == BoxType.FTYP

        val isSkippableDataBox = skipDataBoxPayloads && !isMetadataPayloadBox

        val bytes: ByteArray = when {

            /*
             * The video scan must look inside a file-level meta box:
             * the ISO item layout in it carries metadata, and a meta
             * bearing it fails the read below instead of being
             * skipped. The payload is therefore buffered with the
             * metadata budget, like every other box the scan looks
             * into.
             */
            type == BoxType.META && skipDataBoxPayloads -> {

                if (remainingBytesToReadInThisBox > MAX_METADATA_BOX_BYTES)
                    throw ImageReadException(
                        "Box $type carries $remainingBytesToReadInThisBox bytes of " +
                            "payload, which exceeds the metadata budget of " +
                            "$MAX_METADATA_BOX_BYTES bytes."
                    )

                val payload = readPayloadUpToEof(
                    byteReader,
                    remainingBytesToReadInThisBox.toInt()
                )

                payloadTruncated = payload.size < remainingBytesToReadInThisBox

                payload
            }

            isSkippableDataBox -> {

                val skippedByteCount = skipPayloadUpToEof(
                    byteReader,
                    remainingBytesToReadInThisBox
                )

                payloadTruncated = skippedByteCount < remainingBytesToReadInThisBox

                /* The payload is discarded, not retained. */
                ByteArray(0)
            }

            type == BoxType.MDAT &&
                stopAfterMetadataRead &&
                byteReader.isRetaining -> {

                val retained = readPayloadUpToEof(
                    byteReader,
                    remainingBytesToReadInThisBox.toInt()
                )

                payloadTruncated = retained.size < remainingBytesToReadInThisBox

                /* The reader itself retains the bytes. */
                ByteArray(0)
            }

            /*
             * JXL codestream fragments are image data, not metadata:
             * a metadata read must not buffer them a second time. The
             * scan reads them through the retaining reader (which
             * already holds the bytes) and keeps only the leading
             * signature bytes of the first fragment - they decide
             * whether the fragment is the codestream header.
             */
            (type == BoxType.JXLC || type == BoxType.JXLP) &&
                stopAfterMetadataRead &&
                byteReader.isRetaining -> {

                val retained = readPayloadUpToEof(
                    byteReader,
                    remainingBytesToReadInThisBox.toInt()
                )

                payloadTruncated = retained.size < remainingBytesToReadInThisBox

                if (type == BoxType.JXLP && !haveSeenJxlHeaderBox)
                    retained.copyOf(minOf(retained.size, JXL_HEADER_SIGNATURE_LENGTH))
                else
                    ByteArray(0)
            }

            stopAfterMetadataRead -> {

                val payload = readPayloadUpToEof(
                    byteReader,
                    remainingBytesToReadInThisBox.toInt()
                )

                payloadTruncated = payload.size < remainingBytesToReadInThisBox

                payload
            }

            else ->
                byteReader.readBytes("data", remainingBytesToReadInThisBox.toInt())
        }

        return BoxPayloadResult(bytes, payloadTruncated)
    }

    /**
     * The parsed 8-byte box header: the declared size field and the type.
     */
    private class BoxHeader(
        val declaredSize: Long,
        val type: BoxType
    )

    /**
     * Parses the buffered 8-byte box header: the 4-byte size field
     * followed by the type FourCC.
     */
    private fun parseBoxHeader(headerBytes: ByteArray): BoxHeader =

        BoxHeader(
            declaredSize = headerBytes.readUnsignedInt(
                0,
                BMFFConstants.SIZE_LENGTH,
                BMFF_BYTE_ORDER
            ),
            type = BoxType.of(
                headerBytes.copyOfRange(
                    BMFFConstants.SIZE_LENGTH,
                    BMFFConstants.BOX_HEADER_LENGTH
                )
            )
        )

    /**
     * Whether the direct children of a buffered file-level meta box
     * contain an item information entry - the marker of the ISO
     * 14496-12 item layout that carries metadata. The walk is generic on
     * purpose: only the child's type FourCC is identified, so hostile or
     * unknown children cannot break the skip semantics of the video
     * scan, and a meta whose children cannot even be walked keeps the
     * skip behavior.
     */
    private fun hasItemMetadataChildren(payload: ByteArray): Boolean {

        var offset = 0L

        while (offset + BMFFConstants.BOX_HEADER_LENGTH <= payload.size) {

            val type = payload.decodeToString(
                offset.toInt() + BMFFConstants.SIZE_LENGTH,
                offset.toInt() + BMFFConstants.BOX_HEADER_LENGTH
            )

            /*
             * The item layout's marker at meta child level is "iinf", the
             * item information box wrapping the infe entries. A bare
             * "infe" child is malformed but seen in the wild and
             * identifies the layout just the same.
             */
            if (type == "iinf" || type == "infe")
                return true

            val declaredSize = payload.readUnsignedInt(
                offset.toInt(),
                BMFFConstants.SIZE_LENGTH,
                BMFF_BYTE_ORDER
            )

            /*
             * A declared size of 1 announces an 8-byte largesize field
             * behind the type FourCC; a size below the box header cannot
             * be walked.
             */
            val size =
                if (declaredSize == 1L) {

                    if (offset + BMFFConstants.BOX_HEADER_LENGTH + LARGESIZE_LENGTH > payload.size)
                        return false

                    payload.readUnsignedInt(
                        offset.toInt() + BMFFConstants.BOX_HEADER_LENGTH,
                        LARGESIZE_LENGTH,
                        BMFF_BYTE_ORDER
                    )
                } else {
                    declaredSize
                }

            /*
             * The lower bound covers both forms: a declared size below the
             * header and a largesize value below the (larger) header. A
             * hostile zero or negative largesize must end the walk, or the
             * same child would be re-read forever.
             */
            if (size < BMFFConstants.BOX_HEADER_LENGTH)
                return false

            offset += size
        }

        return false
    }

    /**
     * The one shared box scan loop. Every entry point passes a fixed,
     * tested combination of the mode flags into it.
     *
     * @param byteReader The reader as source for the bytes
     * @param stopAfterMetadataRead Stop after the top-level metadata boxes, so the whole image
     * data block is not read in - see [scanMetadataBoxes]
     * @param stopBeforeImageData Stop before the JXL image data starts - see [readBoxesForUpdate]
     * @param skipDataBoxPayloads Skip media data and padding payloads instead of buffering
     * them - see [scanVideoMetadataBoxes]
     * @param rejectTrailingFragment Fail on bytes that end inside a box header instead of
     * stopping there as a clean boundary - see [readAllBoxesForRewrite]
     * @param positionOffset The position where to start reading boxes
     * @param offsetShift The shift to apply to the reported box offsets
     * @param updatePosition A callback to report the position when reading has finished
     * @param parentBoxType The type of the container whose children are read
     * @param depth The nesting level of the boxes, used to bound the recursion
     */
    @Suppress("NestedBlockDepth")
    private fun readBoxes(
        byteReader: ByteReader,
        stopAfterMetadataRead: Boolean = false,
        stopBeforeImageData: Boolean = false,
        skipDataBoxPayloads: Boolean = false,
        rejectTrailingFragment: Boolean = false,
        positionOffset: Long = 0,
        offsetShift: Long = 0,
        updatePosition: ((Long) -> Unit)? = null,
        parentBoxType: BoxType? = null,
        depth: Int = 0
    ): List<Box> {

        if (depth >= MAX_BOX_DEPTH)
            throw ImageReadException("Boxes are nested too deeply: $depth levels.")

        var haveSeenJxlHeaderBox = false

        var haveSeenTopLevelMetaBox = false

        var haveSeenXmpDataInUuid = false

        var haveSeenJxlpBox = false

        val boxes = mutableListOf<Box>()

        var position: Long = positionOffset

        while (true) {

            /*
             * The box walk ends at the delegate's real end of data, never
             * at the length hint: the hint is caller-supplied and may
             * understate the content, and boxes behind it would silently
             * vanish from the parse. The header is read through the raw
             * short-read contract, because the field-based reads throw on
             * a short read while the walk needs the boundary decision.
             */
            val headerBytes = byteReader.readBytes(BMFFConstants.BOX_HEADER_LENGTH)

            if (headerBytes.size < BMFFConstants.BOX_HEADER_LENGTH) {

                /* An empty read is the clean end; a short one a fragment. */
                checkTrailingFragment(headerBytes.size.toLong(), rejectTrailingFragment)

                break
            }

            val offset: Long = position

            /* Note: The declared length includes the 8 header bytes. */
            val header = parseBoxHeader(headerBytes)

            val size: Long = header.declaredSize

            val type = header.type

            position += BMFFConstants.BOX_HEADER_LENGTH

            /*
             * If we read an JXL file and we already have seen the header,
             * all remaining JXLP boxes are image data that we can skip.
             */
            if (stopAfterMetadataRead && type == BoxType.JXLP && haveSeenJxlHeaderBox)
                break

            var largeSize: Long? = null

            val actualLength: Long = when (size) {

                /*
                 * A value of zero indicates that it's the last box, which
                 * extends to the end of the content. The extent is measured
                 * from the box start like every declared extent.
                 */
                0L -> byteReader.contentLength - offset

                /* A length of 1 indicates that we should read the next 8 bytes to get a long value. */
                1L -> {
                    largeSize = byteReader.read8BytesAsLong("length", BMFF_BYTE_ORDER)
                    largeSize
                }

                /*
                 * Keep the length we already read. ISOBMFF sizes are
                 * unsigned, so the high bit encodes boxes of 2 GiB and
                 * above instead of a negative value.
                 */
                else -> size and 0xFFFFFFFFL
            }

            /*
             * Rejects non-positive sizes, sizes below the box's own
             * header and largesize values below both headers - see
             * [validateBoxLength]. The 2^31 rejection for buffered
             * boxes happens separately below, because skippable boxes
             * stream through without a signed read count.
             */
            validateBoxLength(type, size, actualLength)

            /*
             * The first JXLP box contains the codestream header, so every
             * following JXLP box is image data. It is returned with an empty
             * payload, because the caller streams its content.
             */
            if (stopBeforeImageData && type == BoxType.JXLP && haveSeenJxlpBox) {

                boxes.add(Box(type, offset, size, largeSize, ByteArray(0)))

                break
            }

            /*
             * A JXLC box carries the complete codestream. Like a following
             * JXLP box it is pure image data for an update: it is cut here
             * with an empty payload, so its content is streamed by the
             * caller instead of buffering the whole codestream in memory.
             */
            if (stopBeforeImageData && type == BoxType.JXLC) {

                boxes.add(Box(type, offset, size, largeSize, ByteArray(0)))

                break
            }

            val nextBoxOffset = offset + actualLength

            @Suppress("MagicNumber")
            if (size == 1L)
                position += 8

            val remainingBytesToReadInThisBox = nextBoxOffset - position

            /*
             * In the video scan only the payload of boxes that carry
             * metadata is buffered (moov and the XMP boxes); media data,
             * padding and unknown boxes of arbitrary size are streamed
             * through in bounded chunks instead.
             */
            val isMetadataPayloadBox = skipDataBoxPayloads &&
                (
                    type == BoxType.MOOV ||
                        type == BoxType.UUID ||
                        type == BoxType.XMP_ ||
                        type == BoxType.FTYP
                    )

            val isSkippableDataBox = skipDataBoxPayloads && !isMetadataPayloadBox

            requireBufferSizeAllowed(
                type, isSkippableDataBox, remainingBytesToReadInThisBox,
                enforceBudget = stopAfterMetadataRead || skipDataBoxPayloads
            )

            /*
             * Attention: When the reader retains every consumed byte (the
             * metadata path wraps the stream in a CopyByteReader, because
             * meta boxes after the mdat box need already-read regions for
             * their extent re-reads), a large mdat payload must not ALSO
             * be kept inside its box object - that would hold the whole
             * image data twice. Nothing reads the box payload in that
             * mode, so it is dropped immediately.
             */
            val payloadResult = readBoxPayload(
                byteReader = byteReader,
                type = type,
                remainingBytesToReadInThisBox = remainingBytesToReadInThisBox,
                skipDataBoxPayloads = skipDataBoxPayloads,
                stopAfterMetadataRead = stopAfterMetadataRead,
                haveSeenJxlHeaderBox = haveSeenJxlHeaderBox
            )

            val payloadTruncated = payloadResult.truncated

            val bytes: ByteArray = payloadResult.bytes

            position += remainingBytesToReadInThisBox

            val globalOffset = offset + offsetShift

            val box = when (type) {
                /* Generic ISO/IEC 14496-12 boxes. */
                BoxType.FTYP -> FileTypeBox(globalOffset, size, largeSize, bytes)
                BoxType.META -> if (parentBoxType == null) {

                    /*
                     * The video scan skips payloads that carry nothing it
                     * consumes, so a file-level meta box would arrive with
                     * an empty payload. Such a meta is legal in a video
                     * container - but the ISO 14496-12 item layout is legal
                     * in it too, and muxers write video XMP into that
                     * layout. Silently skipping a metadata-bearing meta
                     * would lose it, so a meta carrying item boxes fails
                     * the read; an unparseable meta keeps the skip
                     * semantics, because its content cannot be identified
                     * as metadata. The generic box keeps the (skipped) box
                     * available instead.
                     */
                    if (isSkippableDataBox) {

                        if (hasItemMetadataChildren(bytes))
                            throw ImageReadException(
                                "The file-level meta box of the video " +
                                    "carries item metadata, which is not " +
                                    "read here."
                            )

                        Box(BoxType.META, globalOffset, size, largeSize, bytes)
                    } else {
                        MetaBoxTopLevel(globalOffset, size, largeSize, bytes, depth + 1)
                    }
                } else {
                    MetaBox(globalOffset, size, largeSize, bytes, depth + 1)
                }

                BoxType.HDLR -> HandlerReferenceBox(globalOffset, size, largeSize, bytes)
                BoxType.IINF -> ItemInformationBox(globalOffset, size, largeSize, bytes, depth + 1)
                BoxType.INFE -> ItemInfoEntryBox(globalOffset, size, largeSize, bytes)
                BoxType.ILOC -> ItemLocationBox(globalOffset, size, largeSize, bytes)
                BoxType.PITM -> PrimaryItemBox(globalOffset, size, largeSize, bytes)
                BoxType.MDAT -> MediaDataBox(globalOffset, size, largeSize, bytes, resolvedLength = actualLength)
                BoxType.MOOV -> MovieBox(globalOffset, size, largeSize, bytes, depth + 1)
                BoxType.TRAK -> TrackBox(globalOffset, size, largeSize, bytes, depth + 1)
                BoxType.TKHD -> TrackHeaderBox(globalOffset, size, largeSize, bytes)
                BoxType.MDIA -> MediaBox(globalOffset, size, largeSize, bytes, depth + 1)
                BoxType.UUID -> UuidBox(globalOffset, size, largeSize, bytes)
                BoxType.UDTA -> UserDataBox(globalOffset, size, largeSize, bytes, depth + 1)
                /* JXL boxes */
                BoxType.EXIF -> ExifBox(globalOffset, size, largeSize, bytes)
                BoxType.XML -> XmlBox(globalOffset, size, largeSize, bytes)
                BoxType.JXLP -> JxlPartialCodestreamBox(globalOffset, size, largeSize, bytes)
                BoxType.BROB -> CompressedBox(globalOffset, size, largeSize, bytes)
                /* Unknown box; skippable ones stream through with an empty payload. */
                else -> Box(type, globalOffset, size, largeSize, bytes, resolvedLength = actualLength)
            }

            boxes.add(box)

            /*
             * An interrupted recording cuts the file inside a box payload
             * while the header still declares the full size. In the
             * read-metadata path the boxes parsed so far are returned and
             * reading ends here - there cannot be any further boxes after
             * a truncation.
             */
            if (payloadTruncated)
                break

            if (type == BoxType.JXLP)
                haveSeenJxlpBox = true

            if (stopAfterMetadataRead) {

                /* Metadata is here for most HEIC & AVIF */
                if (type == BoxType.META && parentBoxType == null) {
                    haveSeenTopLevelMetaBox = true

                    box as MetaBoxTopLevel

                    /*
                     * If this box references XMP data, we can break. If it's missing XMP, we should
                     * continue reading the file to search for an XMP UUID box (or break now if
                     * we've already seen it)
                     */
                    if (box.referencesXmp || haveSeenXmpDataInUuid) {
                        break
                    }
                }

                /* Some store XMP data in a UUID box instead */
                if (type == BoxType.UUID) {
                    box as UuidBox

                    /*
                     * If this box contains XMP, we can break as soon as we also find the top-level
                     *  META box (or break now if we've already seen it)
                     */
                    if (box.isXmp) {
                        haveSeenXmpDataInUuid = true
                        if (haveSeenTopLevelMetaBox) break
                    }
                }

                /*
                 * When parsing JXL we need to take a note that we saw the header.
                 * This is usually the first JXLP box.
                 */
                if (type == BoxType.JXLP) {

                    box as JxlPartialCodestreamBox

                    if (box.isHeader)
                        haveSeenJxlHeaderBox = true
                }
            }
        }

        updatePosition?.let { it(position) }

        return boxes
    }

    /**
     * Discards up to [count] bytes in bounded chunks and returns how many
     * bytes were actually skipped, so media data never has to be buffered.
     *
     * Only used in the video scan; the write paths read via
     * [ByteReader.readBytes] and fail loudly on truncation.
     */
    private fun skipPayloadUpToEof(byteReader: ByteReader, count: Long): Long {

        var remaining = count

        while (remaining > 0) {

            val chunkSize = minOf(remaining, READ_CHUNK_SIZE).toInt()

            val skippedByteCount = byteReader.readBytes(chunkSize).size

            if (skippedByteCount == 0)
                break

            remaining -= skippedByteCount
        }

        return count - remaining
    }

    /**
     * Rejects a payload that must not be buffered: boxes beyond the
     * Int range would overflow the read count in every mode, and in the
     * scan modes every non-image box beyond the metadata budget is
     * hostile input - a hostile meta, moov or free box must not exhaust
     * the memory on constrained targets.
     *
     * The budget is intentionally not enforced in the full-read modes:
     * there the caller explicitly asked for the whole file to be
     * buffered (the JXL rewrite and the public readAllBoxes), and the
     * JXL image data lives in jxlp/jxlc boxes, which are image data
     * just like mdat and never budget-bound.
     */
    private fun requireBufferSizeAllowed(
        type: BoxType,
        isSkippableDataBox: Boolean,
        remainingBytesToReadInThisBox: Long,
        enforceBudget: Boolean
    ) {

        if (isSkippableDataBox)
            return

        if (remainingBytesToReadInThisBox > Int.MAX_VALUE)
            throw ImageReadException(
                "Box $type is too large: $remainingBytesToReadInThisBox bytes."
            )

        val isImageDataBox =
            type == BoxType.MDAT || type == BoxType.JXLP || type == BoxType.JXLC

        if (enforceBudget && !isImageDataBox && remainingBytesToReadInThisBox > MAX_METADATA_BOX_BYTES) {

            throw ImageReadException(
                "Box $type carries $remainingBytesToReadInThisBox bytes of " +
                    "payload, which exceeds the metadata budget of " +
                    "$MAX_METADATA_BOX_BYTES bytes."
            )
        }
    }

    /**
     * Reads exactly [count] bytes, or everything up to the end of the
     * stream. A result shorter than [count] means the stream ended inside
     * the box payload - an interrupted recording.
     *
     * Only used in the read-metadata path; write paths read via
     * [ByteReader.readBytes] and fail loudly on truncation.
     */
    private fun readPayloadUpToEof(byteReader: ByteReader, count: Int): ByteArray {

        val writer = ByteArrayByteWriter()

        var remaining = count.toLong()

        while (remaining > 0) {

            val chunk = byteReader.readBytes(minOf(remaining, READ_CHUNK_SIZE).toInt())

            if (chunk.isEmpty())
                break

            writer.write(chunk)

            remaining -= chunk.size
        }

        return writer.toByteArray()
    }
}
