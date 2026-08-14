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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.ImageParser
import de.stefan_oltmann.kim.format.MediaMetadata
import de.stefan_oltmann.kim.format.jpeg.JpegConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcParser
import de.stefan_oltmann.kim.format.png.PngConstants.PNG_BYTE_ORDER
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
        PngChunkType.EXIF
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
             * We attempt to read EXIF data from the EXIF chunk, which has been the standard
             * location since 2017. If the EXIF chunk is not present, we fallback to reading
             * it from TXT. Some older apps may still store the data there.
             */
            val exifChunk = chunks.filterIsInstance<PngChunkExif>().firstOrNull()

            val exifPair = exifChunk?.let { it.bytes to it.tiffContents }
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
         * Ensure the block is completely read and is a multiple of two.
         * We don't want the following ByteArray-conversion to fail.
         */
        if (!exifText.endsWith("ffd9") || exifText.length % 2 != 0)
            return null

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
         * Ensure the block is completely read and is a multiple of two.
         * We don't want the following ByteArray-conversion to fail.
         */
        if (iptcText.length % 2 != 0)
            return null

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

    private fun getXmpXml(chunks: List<PngChunk>): String? {

        val text = chunks
            .filterIsInstance<PngChunkItxt>()
            .filter { it.getKeyword() == PngConstants.XMP_KEYWORD }
            .firstOrNull()
            ?.getText()

        if (text.isNullOrBlank())
            return text

        return text
    }

    private fun getTextChunkWithKeyword(chunks: List<PngChunk>, keyword: String): String? {

        val text = chunks
            .filterIsInstance<PngTextChunk>()
            .filter { it.getKeyword() == keyword }
            .firstOrNull()
            ?.getText()

        if (text.isNullOrBlank())
            return text

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

                chunks.add(createChunk(chunkType, bytes, crc))
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

            chunks.add(createChunk(chunkType, bytes, crc))

            if (PngChunkType.IEND == chunkType)
                break
        }

        return chunks
    }

    private fun createChunk(chunkType: PngChunkType, bytes: ByteArray, crc: Int): PngChunk =
        when (chunkType) {
            PngChunkType.TEXT -> PngChunkText(PngChunkType.TEXT, bytes, crc)
            PngChunkType.ZTXT -> PngChunkZtxt(bytes, crc)
            PngChunkType.IHDR -> PngChunkIhdr(bytes, crc)
            PngChunkType.ITXT -> PngChunkItxt(bytes, crc)
            PngChunkType.EXIF -> PngChunkExif(bytes, crc)
            else -> PngChunk(chunkType, bytes, crc)
        }
}
