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
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcParser
import de.stefan_oltmann.kim.format.png.PngConstants.PNG_BYTE_ORDER
import de.stefan_oltmann.kim.format.png.PngCrc.continuePartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.finishPartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.startPartialCrc
import de.stefan_oltmann.kim.format.png.chunk.PngChunk
import de.stefan_oltmann.kim.format.png.chunk.PngChunkExif
import de.stefan_oltmann.kim.format.png.chunk.PngChunkIhdr
import de.stefan_oltmann.kim.format.png.chunk.PngChunkItxt
import de.stefan_oltmann.kim.format.png.chunk.PngChunkText
import de.stefan_oltmann.kim.format.png.chunk.PngChunkZtxt
import de.stefan_oltmann.kim.format.png.chunk.PngTextChunk
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffReader
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

    /* Note that [\\p{Cntrl}] does not work for Kotlin/JS. */
    private val controlCharRegex = Regex("""[\x00-\x1F\x7F-\x9F]""")

    private val metadataChunkTypes = listOf(
        PngChunkType.IHDR,
        PngChunkType.TEXT,
        PngChunkType.ZTXT,
        PngChunkType.ITXT,
        PngChunkType.EXIF,
        PngChunkType.ZXIF
    )

    @Throws(ImageReadException::class)
    override fun parseMetadata(byteReader: ByteReader): MediaMetadata =
        tryWithImageReadException {

            val chunks = readChunks(byteReader, metadataChunkTypes)

            if (chunks.isEmpty())
                throw ImageReadException("Did not find any chunks in file.")

            return@tryWithImageReadException parseMetadataFromChunks(chunks)
        }

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
             * Like ExifTool, only the first eXIf/zxIf chunk is
             * authoritative: later ones are ignored entirely (not
             * merged), and a text-chunk EXIF only serves as the fallback
             * when no chunk exists.
             */
            val exifPair = chunks.filterIsInstance<PngChunkExif>()
                .firstOrNull()
                ?.let { it.exifBytes to it.tiffContents }
                ?: getExifFromTextChunk(chunks)

            val iptc = getIptcFromTextChunk(chunks)

            val xmp = getXmpXml(chunks)

            return@tryWithImageReadException MediaMetadata(
                mediaFormat = MediaFormat.PNG,
                imageSize = imageSize,
                exif = exifPair?.second,
                exifBytes = exifPair?.first,
                iptc = iptc,
                xmp = xmp
            )
        }

    /*
     * According to https://dev.exiv2.org/projects/exiv2/wiki/The_Metadata_in_PNG_files
     * Exiv2 saves EXIF & IPTC in zTXT chunks. This library is widely used and therefore
     * we can expect a lot of files storing the information in that way.
     * According to https://exiftool.sourceforge.net/TagNames/PNG.html it may even be in uncompressed text.
     * So we look for all PNG text chunk types and take the first one that matches the keyword.
     */
    private fun getExifFromTextChunk(chunks: List<PngChunk>): Pair<ByteArray, TiffContents>? {

        val chunkText = getTextChunkWithKeyword(chunks, PngConstants.EXIF_KEYWORD) ?: return null

        /*
         * Before the EXIF block starts there are some characters before that.
         * How these look seems to depend on the tool writing it. There may be no standard.
         */
        val index = chunkText.indexOf(JpegConstants.EXIF_IDENTIFIER_CODE_HEX)

        /* If we did not find the identifier we may have invalid data. */
        if (index == -1)
            return null

        /*
         * This should be a text starting with EXIF identifier code "45786966"
         * and ending with the regular "ffd9". It's HEX encoded and contains
         * control chars. We need to remove them and convert it to a ByteArray.
         */
        val exifText = chunkText
            .substring(startIndex = index)
            .replace(controlCharRegex, "")
            .trim()

        /*
         * The chunk content is file-controlled and may be garbage, which
         * is ignored instead of failing the read.
         */
        if (!exifText.isValidHexString())
            return null

        /*
         * A hex encoded profile that claims to be EXIF but does not end
         * at the JPEG EOI marker on an even boundary is a truncated
         * record. Per the strict read policy the read fails instead of
         * silently dropping the EXIF content. The marker check ignores
         * the case, because the hex encoding itself is case insensitive.
         */
        if (!exifText.endsWith("ffd9", ignoreCase = true) || exifText.length % 2 != 0)
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

    private fun getIptcFromTextChunk(chunks: List<PngChunk>): IptcMetadata? {

        val chunkText = getTextChunkWithKeyword(chunks, PngConstants.IPTC_KEYWORD) ?: return null

        /*
         * Before the IPTC block starts there are some characters before that.
         * How these look seems to depend on the tool writing it. There may be no standard.
         */
        val index = chunkText.indexOf(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_HEX)

        /* If we did not find the identifier we may have invalid data. */
        if (index == -1)
            return null

        /*
         * This text is HEX encoded and contains control chars.
         * We need to remove them and convert it to a ByteArray.
         */
        val iptcText = chunkText
            .substring(startIndex = index)
            .replace(controlCharRegex, "")
            .trim()

        /*
         * The chunk content is file-controlled and may be garbage, which
         * is ignored instead of failing the read.
         */
        if (!iptcText.isValidHexString())
            return null

        /*
         * An odd number of hex digits cannot be converted to bytes
         * completely - the record is truncated. Per the strict read
         * policy the read fails instead of silently dropping the IPTC
         * content.
         */
        if (iptcText.length % 2 != 0)
            throw ImageReadException("The IPTC text chunk of the PNG is truncated.")

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
     * is not hex encoded is ignored instead of failing the conversion.
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

    @JvmStatic
    public fun readChunks(
        byteReader: ByteReader,
        chunkTypeFilter: List<PngChunkType>?
    ): List<PngChunk> = tryWithImageReadException {

        readAndVerifySignature(byteReader)

        return readChunksInternal(byteReader, chunkTypeFilter)
    }

    private fun readChunksInternal(
        byteReader: ByteReader,
        chunkTypeFilter: List<PngChunkType>?
    ): List<PngChunk> {

        val chunks = mutableListOf<PngChunk>()

        /*
         * Only the first EXIF chunk is parsed; like ExifTool, later ones
         * are ignored instead of failing or merging the read.
         */
        var haveParsedExifChunk = false

        while (true) {

            val length = byteReader.read4BytesAsInt("chunk length", PNG_BYTE_ORDER)

            if (length < 0)
                throw ImageReadException("Invalid PNG chunk length: $length")

            val chunkType = PngChunkType.of(
                byteReader.readBytes("chunk type", PngConstants.TPYE_LENGTH)
            )

            val keep = chunkTypeFilter?.contains(chunkType) ?: true

            var bytes: ByteArray? = null

            if (keep)
                bytes = byteReader.readBytes("chunk data", length)
            else
                byteReader.skipBytes("chunk data", length)

            val crc = byteReader.read4BytesAsInt("crc", PNG_BYTE_ORDER)

            if (keep) {

                requireNotNull(bytes)

                /*
                 * Chunks that are not kept are not verified, because they
                 * are neither interpreted nor rewritten by Kim.
                 */
                verifyChunkCrc(chunkType, bytes, crc)

                val parseExif =
                    (chunkType == PngChunkType.EXIF || chunkType == PngChunkType.ZXIF) &&
                        !haveParsedExifChunk

                if (parseExif)
                    haveParsedExifChunk = true

                chunks.add(createChunk(chunkType, bytes, crc, parseExif))
            }

            if (PngChunkType.IEND == chunkType)
                break
        }

        return chunks
    }

    /**
     * Reads the PNG chunks up to the start of the image data, so the image
     * data can be streamed afterwards without buffering the whole file.
     *
     * The length and type field of the first IDAT chunk are written to the
     * given writer, because they are consumed by the reader but belong to
     * the streamed image data.
     */
    internal fun readChunksUntilImageData(
        byteReader: ByteReader,
        imageDataHeaderWriter: ByteWriter
    ): List<PngChunk> {

        readAndVerifySignature(byteReader)

        val chunks = mutableListOf<PngChunk>()

        /* See readChunksInternal: only the first EXIF chunk is parsed. */
        var haveParsedExifChunk = false

        while (true) {

            val length = byteReader.read4BytesAsInt("chunk length", PNG_BYTE_ORDER)

            if (length < 0)
                throw ImageReadException("Invalid PNG chunk length: $length")

            val chunkType = PngChunkType.of(
                byteReader.readBytes("chunk type", PngConstants.TPYE_LENGTH)
            )

            if (chunkType == PngChunkType.IDAT) {

                imageDataHeaderWriter.writeInt(length, PNG_BYTE_ORDER)
                imageDataHeaderWriter.write(chunkType.bytes)

                break
            }

            val bytes = byteReader.readBytes("chunk data", length)

            val crc = byteReader.read4BytesAsInt("crc", PNG_BYTE_ORDER)

            verifyChunkCrc(chunkType, bytes, crc)

            val parseExif =
                (chunkType == PngChunkType.EXIF || chunkType == PngChunkType.ZXIF) &&
                    !haveParsedExifChunk

            if (parseExif)
                haveParsedExifChunk = true

            chunks.add(createChunk(chunkType, bytes, crc, parseExif))

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

    private fun createChunk(
        chunkType: PngChunkType,
        bytes: ByteArray,
        crc: Int,
        parseExif: Boolean
    ): PngChunk =
        when {
            chunkType == PngChunkType.TEXT ->
                PngChunkText(PngChunkType.TEXT, bytes, crc)

            chunkType == PngChunkType.ZTXT ->
                PngChunkZtxt(bytes, crc)

            chunkType == PngChunkType.IHDR ->
                PngChunkIhdr(bytes, crc)

            chunkType == PngChunkType.ITXT ->
                PngChunkItxt(bytes, crc)

            chunkType == PngChunkType.EXIF || chunkType == PngChunkType.ZXIF ->
                if (parseExif)
                    PngChunkExif(chunkType, bytes, crc)
                else

                    /*
                     * A later duplicate EXIF chunk is ignored like
                     * ExifTool does, so its bytes stay preserved but are
                     * never parsed - a corrupt duplicate must not fail
                     * the read of the authoritative first chunk.
                     */
                    PngChunk(chunkType, bytes, crc)

            else -> PngChunk(chunkType, bytes, crc)
        }
}
