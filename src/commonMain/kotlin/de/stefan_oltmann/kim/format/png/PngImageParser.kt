/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package de.stefan_oltmann.kim.format.png

import de.stefan_oltmann.kim.common.HEX_RADIX
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.MAX_DECOMPRESSED_BYTE_COUNT
import de.stefan_oltmann.kim.common.MAX_DECOMPRESSED_TEXT_BYTES
import de.stefan_oltmann.kim.common.MAX_METADATA_BYTE_BUDGET
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.icc.IccProfileParser
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcParser
import de.stefan_oltmann.kim.format.png.PngConstants.PNG_BYTE_ORDER
import de.stefan_oltmann.kim.format.png.PngCrc.continuePartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.finishPartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.startPartialCrc
import de.stefan_oltmann.kim.format.png.chunk.PngChunk
import de.stefan_oltmann.kim.format.png.chunk.PngChunkExif
import de.stefan_oltmann.kim.format.png.chunk.PngChunkIccp
import de.stefan_oltmann.kim.format.png.chunk.PngChunkIhdr
import de.stefan_oltmann.kim.format.png.chunk.PngChunkItxt
import de.stefan_oltmann.kim.format.png.chunk.PngChunkText
import de.stefan_oltmann.kim.format.png.chunk.PngChunkZtxt
import de.stefan_oltmann.kim.format.png.chunk.PngTextChunk
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.xmp.requireValidXmpPacket
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readAndVerifyBytes
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.skipBytes
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.output.ByteWriter
import de.stefan_oltmann.kim.output.writeInt
import kotlin.jvm.JvmStatic

/**
 * Parses the metadata of PNG files.
 */
public object PngImageParser : ImageParser {

    /*
     * The payload of every kept chunk is buffered, so like the JPEG
     * path's header segment budget, a hostile file of oversized
     * metadata chunks must not accumulate unboundedly.
     */
    internal const val MAX_RETAINED_CHUNK_BYTES: Int = MAX_METADATA_BYTE_BUDGET

    /**
     * The number of ancillary chunks one read stores, bounded exactly
     * like the chunk cache of libpng: a file of many tiny chunks
     * inflates the heap through object overhead alone, since every
     * stored chunk costs more memory than its payload draws from the
     * byte budgets. Critical chunks do not count - libpng streams the
     * image data instead of caching it, and Kim keeps the critical
     * chunks only for the rewrite.
     *
     * libpng stops storing further chunks beyond its limit; Kim fails
     * the read instead - silently dropping metadata would violate the
     * strict read policy.
     *
     * See png_set_chunk_cache_max in
     * https://www.libpng.org/pub/png/libpng-manual.txt
     */
    internal const val MAX_STORED_CHUNK_COUNT: Int = 1000

    /* Note that [\\p{Cntrl}] does not work for Kotlin/JS. */
    private val controlCharRegex = Regex("""[\x00-\x1F\x7F-\x9F]""")

    private val metadataChunkTypes = listOf(
        PngChunkType.IHDR,
        PngChunkType.TEXT,
        PngChunkType.ZTXT,
        PngChunkType.ITXT,
        PngChunkType.EXIF,
        PngChunkType.ZXIF,
        PngChunkType.ICCP
    )

    @Throws(ImageReadException::class)
    override fun parseMetadata(byteReader: ByteReader): MediaMetadata =
        tryWithImageReadException {

            val chunks = readChunks(byteReader, metadataChunkTypes)

            if (chunks.isEmpty())
                throw ImageReadException("Did not find any chunks in file.")

            return@tryWithImageReadException parseMetadataFromChunks(chunks)
        }

    /**
     * Assembles the metadata from the chunks of a PNG file.
     */
    @Throws(ImageReadException::class)
    @JvmStatic
    public fun parseMetadataFromChunks(chunks: List<PngChunk>): MediaMetadata =
        tryWithImageReadException {

            require(chunks.isNotEmpty()) {
                "Given chunk list was empty."
            }

            val headerChunk = chunks.filterIsInstance<PngChunkIhdr>().firstOrNull()

            checkNotNull(headerChunk) {
                "Did not find mandatory IHDR chunk. " +
                    "Found chunk types: ${chunks.map { it.type }}"
            }

            val imageSize = headerChunk.imageSize

            /*
             * The EXIF chunk has been the standard location since 2017.
             * The chunk walk rejects duplicate eXIf chunks, so the first
             * one is the only eXIf chunk of the file, and a text-chunk
             * EXIF only serves as the fallback when no chunk exists.
             */
            val exifPair = chunks.filterIsInstance<PngChunkExif>()
                .firstOrNull()
                ?.let { it.exifBytes to it.tiffContents }
                ?: getExifFromTextChunk(chunks)

            val iptc = getIptcFromTextChunk(chunks)

            /*
             * A packet cut between the opening and the closing element
             * is truncated content: it must fail the read like it fails
             * the update path, instead of reaching sidecar writers.
             */
            val xmp = requireValidXmpPacket(
                xmp = getXmpXml(chunks),
                sourceDescription = "The PNG XMP text chunk"
            )

            val iccProfile = chunks.filterIsInstance<PngChunkIccp>()
                .firstOrNull()
                ?.let { chunk -> IccProfileParser.parse(chunk.profileBytes) }

            return@tryWithImageReadException MediaMetadata(
                mediaFormat = MediaFormat.PNG,
                imageSize = imageSize,
                exif = exifPair?.second,
                exifBytes = exifPair?.first,
                iptc = iptc,
                xmp = xmp,
                iccProfile = iccProfile
            )
        }

    /**
     * Reads the hex encoded EXIF profile from the text chunk that carries
     * [PngConstants.EXIF_KEYWORD], or returns NULL when no chunk holds it.
     */
    private fun getExifFromTextChunk(chunks: List<PngChunk>): Pair<ByteArray, TiffContents>? {

        val exifText = extractHexEncodedProfile(
            chunks = chunks,
            keyword = PngConstants.EXIF_KEYWORD,
            identifierHex = JpegConstants.EXIF_IDENTIFIER_CODE_HEX,
            profileName = "EXIF"
        ) ?: return null

        /*
         * A hex encoded profile that claims to be EXIF but does not end
         * at the JPEG EOI marker is a truncated record. Per the strict
         * read policy the read fails instead of silently dropping the
         * EXIF content. The marker check ignores the case, because the
         * hex encoding itself is case insensitive.
         */
        if (!exifText.endsWith("ffd9", ignoreCase = true))
            throw ImageReadException("The EXIF text chunk of the PNG is truncated.")

        /*
         * Convert it to bytes and drop the header.
         */
        val exifBytes = convertHexStringToByteArray(exifText)

        val exifBytesWithoutIdentifier =
            exifBytes.drop(JpegConstants.EXIF_IDENTIFIER_CODE.size)
                .toByteArray()

        /*
         * This should be fine now to be fed into the TIFF reader.
         */
        return exifBytesWithoutIdentifier to
            TiffReader.read(exifBytesWithoutIdentifier)
    }

    /**
     * Reads the hex encoded IPTC profile from the text chunk that carries
     * [PngConstants.IPTC_KEYWORD], or returns NULL when no chunk holds it.
     */
    private fun getIptcFromTextChunk(chunks: List<PngChunk>): IptcMetadata? {

        val iptcText = extractHexEncodedProfile(
            chunks = chunks,
            keyword = PngConstants.IPTC_KEYWORD,
            identifierHex = JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_HEX,
            profileName = "IPTC"
        ) ?: return null

        /*
         * Convert it to bytes.
         */
        val iptcBytes = convertHexStringToByteArray(iptcText)

        /*
         * This should be fine now to be fed into the IPTC reader.
         * The bytes don't have the APP13 header, because it's not taken from an JPEG segment.
         */
        return IptcParser.parseIptc(
            bytes = iptcBytes,
            startsWithApp13Header = false
        )
    }

    /**
     * Finds the text chunk with the given keyword, strips the tool
     * specific prefix in front of the given identifier and decodes the
     * remaining hex text, or returns NULL when no chunk carries the
     * identifier or the text is not hex encoded.
     *
     * The identifier marks where the profile starts: how the characters
     * before it look seems to depend on the tool writing it, there may
     * be no standard.
     *
     * According to https://dev.exiv2.org/projects/exiv2/wiki/The_Metadata_in_PNG_files
     * Exiv2 saves EXIF & IPTC in zTXT chunks. This library is widely used
     * and therefore we can expect a lot of files storing the information
     * in that way. According to https://exiftool.sourceforge.net/TagNames/PNG.html
     * it may even be in uncompressed text, so all PNG text chunk types
     * are searched for the keyword.
     *
     * An odd number of hex digits cannot be converted to bytes
     * completely - the record is truncated. Per the strict read policy
     * the read fails instead of silently dropping the content.
     */
    private fun extractHexEncodedProfile(
        chunks: List<PngChunk>,
        keyword: String,
        identifierHex: String,
        profileName: String
    ): String? {

        val chunkText = getTextChunkWithKeyword(chunks, keyword) ?: return null

        val index = chunkText.indexOf(identifierHex)

        /*
         * The keyword is present, so the chunk claims to carry this
         * profile. Without the identifier its content is metadata that
         * cannot be read cleanly - per the strict read policy the read
         * fails instead of silently dropping it.
         */
        if (index == -1)
            throw ImageReadException(
                "The $profileName text chunk of the PNG has no profile identifier."
            )

        /*
         * The profile text is HEX encoded and contains control chars.
         * We need to remove them before the conversion to bytes.
         */
        val profileText = chunkText
            .substring(startIndex = index)
            .replace(controlCharRegex, "")
            .trim()

        /*
         * The chunk claims to carry the profile, so content that is not
         * hex encoded cannot be read cleanly - the read fails instead
         * of silently dropping it.
         */
        if (!profileText.isValidHexString())
            throw ImageReadException(
                "The $profileName text chunk of the PNG is not hex encoded."
            )

        if (profileText.length % 2 != 0)
            throw ImageReadException("The $profileName text chunk of the PNG is truncated.")

        return profileText
    }

    private fun getXmpXml(chunks: List<PngChunk>): String? =
        /*
         * The XMP keyword is looked up in all text chunk types, not just
         * iTXt: Exiv2 wrote the packet into tEXt or zTXt chunks, and the
         * writer below removes every text chunk with this keyword. A
         * packet that is not read here would be destroyed on the next
         * update.
         */
        getTextChunkWithKeyword(chunks, PngConstants.XMP_KEYWORD)

    /**
     * Whether the string consists of hex digits only, so a profile that
     * is not hex encoded fails the read.
     */
    private fun String.isValidHexString(): Boolean =
        isNotEmpty() && all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }

    private fun getTextChunkWithKeyword(chunks: List<PngChunk>, keyword: String): String? {

        val text = chunks
            .filterIsInstance<PngTextChunk>()
            .firstOrNull { it.getKeyword() == keyword }
            ?.getText()

        return text
    }

    private fun readAndVerifySignature(byteReader: ByteReader) =
        byteReader.readAndVerifyBytes("PNG signature", PngConstants.PNG_SIGNATURE)

    /**
     * Reads the chunks of a whole PNG file.
     *
     * With a non-NULL filter only chunks of the listed types are returned;
     * skipped chunks are still consumed, so the reader stays in sync.
     */
    @JvmStatic
    public fun readChunks(
        byteReader: ByteReader,
        chunkTypeFilter: List<PngChunkType>?
    ): List<PngChunk> = tryWithImageReadException {

        readAndVerifySignature(byteReader)

        return readChunksInternal(
            byteReader,
            chunkTypeFilter,
            imageDataHeaderWriter = null,
            skipChunkTypes = emptyList()
        )
    }

    /**
     * Reads the PNG chunks up to the start of the image data, so the image
     * data can be streamed afterwards without buffering the whole file.
     *
     * The length and type field of the first IDAT chunk are written to the
     * given writer, because they are consumed by the reader but belong to
     * the streamed image data.
     *
     * Chunks of a type in [skipChunkTypes] are skipped like the chunks
     * outside a metadata filter: consumed, but not kept, not CRC-verified
     * and not decompressed. Deletion passes the types it is about to
     * drop, so a discarded chunk cannot fail the write with its parse
     * error - the metadata content does not need to be parseable when it
     * is removed anyway. The EXIF containers are the one exception when
     * the orientation is preserved: their payload must be parseable, so
     * the rotation can be read from it.
     */
    internal fun readChunksUntilImageData(
        byteReader: ByteReader,
        imageDataHeaderWriter: ByteWriter,
        skipChunkTypes: List<PngChunkType>
    ): List<PngChunk> {

        readAndVerifySignature(byteReader)

        return readChunksInternal(
            byteReader,
            chunkTypeFilter = null,
            imageDataHeaderWriter,
            skipChunkTypes
        )
    }

    /**
     * The single chunk walk of the PNG read: every chunk is framed
     * (length, type, data, CRC) exactly once, so the strict-read rules
     * cannot drift between callers.
     *
     * With a non-NULL [chunkTypeFilter] the payload of chunks outside the
     * filter is consumed but not kept, and their CRC is not verified,
     * because they are neither interpreted nor rewritten by Kim.
     *
     * When [imageDataHeaderWriter] is given, the walk stops before the
     * image data and emits the consumed length and type fields of the
     * first IDAT chunk to it.
     */
    private fun readChunksInternal(
        byteReader: ByteReader,
        chunkTypeFilter: List<PngChunkType>?,
        imageDataHeaderWriter: ByteWriter?,
        skipChunkTypes: List<PngChunkType>
    ): List<PngChunk> {

        val chunks = mutableListOf<PngChunk>()

        /*
         * The budget only guards the metadata-scoped reads: a full-file
         * rewrite (updateThumbnail, public writeImage) inherently buffers
         * the whole image, so counting its image data against a metadata
         * budget would fail legitimate files. The metadata reads
         * (metadata filter, or the walk that stops at the image data)
         * buffer only metadata, and that is what the budget protects.
         */
        val enforceRetainedBudget =
            chunkTypeFilter != null || imageDataHeaderWriter != null

        var retainedChunkBytes = 0L

        /*
         * The types of the single-appearance chunks seen so far. A
         * repeated one is corrupt per the chunk ordering rules of the
         * PNG specification, and the hostile case - repeated iCCP chunks
         * with decompression bombs - is rejected before the payload of
         * the duplicate is even read, let alone decompressed.
         */
        val seenSingleAppearanceChunks = mutableSetOf<PngChunkType>()

        /*
         * The text chunk types are the only ones that occur multiple
         * times per file and still decompress, so a hostile file of
         * repeated zTXt or iTXt bombs is bounded by one cumulative
         * budget: every text decompression of this walk draws from it,
         * whatever the walk keeps. The single-occurrence chunks are
         * bounded by the per-operation limit alone.
         */
        var remainingTextBytes = MAX_DECOMPRESSED_TEXT_BYTES

        var storedChunkCount = 0

        while (true) {

            val length = byteReader.read4BytesAsInt("chunk length", PNG_BYTE_ORDER)

            if (length < 0)
                throw ImageReadException("Invalid PNG chunk length: $length")

            val chunkType = PngChunkType.of(
                byteReader.readBytes("chunk type", PngConstants.TYPE_LENGTH)
            )

            if (chunkType == PngChunkType.IDAT && imageDataHeaderWriter != null) {

                imageDataHeaderWriter.writeInt(length, PNG_BYTE_ORDER)
                imageDataHeaderWriter.write(chunkType.bytes)

                break
            }

            val keep = chunkType !in skipChunkTypes &&
                (chunkTypeFilter?.contains(chunkType) ?: true)

            /*
             * The chunk ordering rules of the PNG specification allow
             * these chunk types only once per datastream, so a second
             * occurrence marks the file as corrupt and fails the read
             * before its payload is read or verified.
             */
            if (
                keep &&
                chunkType in PngChunkType.SINGLE_APPEARANCE_CHUNK_TYPES &&
                !seenSingleAppearanceChunks.add(chunkType)
            )
                throw ImageReadException(
                    "The PNG file contains more than one $chunkType chunk, " +
                        "which the PNG specification allows only once."
                )

            var bytes: ByteArray? = null

            /*
             * Validate before allocating: a single hostile chunk declaring
             * hundreds of megabytes must fail without materializing its
             * payload first.
             */
            if (
                keep &&
                enforceRetainedBudget &&
                retainedChunkBytes + length > MAX_RETAINED_CHUNK_BYTES
            )
                throw ImageReadException(
                    "The PNG metadata chunks exceed $MAX_RETAINED_CHUNK_BYTES bytes."
                )

            if (keep)
                bytes = byteReader.readBytes("chunk data", length)
            else
                byteReader.skipBytes("chunk data", length)

            val crc = byteReader.read4BytesAsInt("crc", PNG_BYTE_ORDER)

            if (keep) {

                requireNotNull(bytes)

                retainedChunkBytes += bytes.size

                verifyChunkCrc(chunkType, bytes, crc)

                val chunk = createChunk(
                    chunkType = chunkType,
                    bytes = bytes,
                    crc = crc,
                    textByteAllowance = minOf(MAX_DECOMPRESSED_BYTE_COUNT, remainingTextBytes)
                )

                chunks.add(chunk)

                remainingTextBytes -= chunk.decompressedByteCount

                if (chunk.ancillary) {

                    storedChunkCount++

                    if (storedChunkCount > MAX_STORED_CHUNK_COUNT)
                        throw ImageReadException(
                            "The PNG file contains more than $MAX_STORED_CHUNK_COUNT chunks."
                        )
                }
            }

            if (PngChunkType.IEND == chunkType)
                break
        }

        return chunks
    }

    /**
     * Verifies that the stored CRC of the chunk matches the computed one.
     *
     * Attention: A CRC mismatch deliberately fails the read instead of
     * being ignored. Ignoring it would let a rewrite emit the corrupted
     * chunk with a fresh, valid CRC, which would hide the corruption from
     * all further tools.
     */
    private fun verifyChunkCrc(
        chunkType: PngChunkType,
        bytes: ByteArray,
        storedCrc: Int
    ) {

        @Suppress("MagicNumber")
        val computedCrc = finishPartialCrc(
            continuePartialCrc(startPartialCrc(chunkType.bytes), bytes)
        ).toInt()

        if (computedCrc != storedCrc)
            throw ImageReadException(
                "CRC mismatch in $chunkType chunk: " +
                    "stored 0x${storedCrc.toUInt().toString(HEX_RADIX)}, " +
                    "computed 0x${computedCrc.toUInt().toString(HEX_RADIX)}."
            )
    }

    /**
     * Builds the chunk object of the given chunk type.
     *
     * The [textByteAllowance] bounds how many bytes the compressed text
     * chunks may inflate; the single-occurrence decompressing chunks are
     * bounded by the per-operation limit alone.
     */
    private fun createChunk(
        chunkType: PngChunkType,
        bytes: ByteArray,
        crc: Int,
        textByteAllowance: Int
    ): PngChunk =
        when (chunkType) {

            PngChunkType.TEXT ->
                PngChunkText(PngChunkType.TEXT, bytes, crc)

            PngChunkType.ZTXT ->
                PngChunkZtxt.of(bytes, crc, textByteAllowance)

            PngChunkType.IHDR ->
                PngChunkIhdr(bytes, crc)

            PngChunkType.ITXT ->
                PngChunkItxt.of(bytes, crc, textByteAllowance)

            PngChunkType.ICCP ->
                PngChunkIccp(bytes, crc)

            /*
             * A duplicate of either type can no longer reach this walk,
             * so the first eXIf or zxIf chunk is the only one parsed.
             * zxIf carries the same TIFF payload compressed.
             */
            PngChunkType.EXIF, PngChunkType.ZXIF ->
                PngChunkExif(chunkType, bytes, crc)

            else -> PngChunk(chunkType, bytes, crc)
        }
}
