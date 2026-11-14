/*
 * Copyright 2026 Stefan Oltmann
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
package de.stefan_oltmann.kim.format

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.jxl.JxlWriter
import de.stefan_oltmann.kim.format.png.PngConstants
import de.stefan_oltmann.kim.format.png.PngCrc.continuePartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.finishPartialCrc
import de.stefan_oltmann.kim.format.png.PngCrc.startPartialCrc
import de.stefan_oltmann.kim.format.png.PngImageParser
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.webp.WebPImageParser
import de.stefan_oltmann.kim.format.webp.WebPWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.writeInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Tests the orientation preservation of [Kim.deleteMetadata]: with the
 * keepOrientationFlag on, the metadata container carrying an explicitly
 * stored Exif orientation (1-8) is replaced in place by a minimal
 * structure carrying only that value, so the stored value survives the
 * removal. Only an absent orientation - which per the specification
 * means 1 - writes nothing: the deletion fabricates no tag the file
 * never stated.
 */
class KeepOrientationFlagTest {

    /**
     * Builds EXIF bytes carrying a Make field and the given orientation,
     * so the deletion can prove that everything besides the orientation
     * is gone afterwards.
     */
    private fun exifBytesWith(orientation: Short): ByteArray {

        val outputSet = TiffOutputSet()

        outputSet.getOrCreateRootDirectory()
            .add(TiffTag.TIFF_TAG_MAKE, "Canon")

        outputSet.getOrCreateRootDirectory()
            .add(TiffTag.TIFF_TAG_ORIENTATION, orientation)

        return outputSet.toTiffBytes()
    }

    /**
     * Builds EXIF bytes carrying a Make field and no orientation tag,
     * so the deletion can prove that an absent orientation fabricates
     * nothing.
     */
    private fun exifBytesWithoutOrientation(): ByteArray {

        val outputSet = TiffOutputSet()

        outputSet.getOrCreateRootDirectory()
            .add(TiffTag.TIFF_TAG_MAKE, "Canon")

        return outputSet.toTiffBytes()
    }

    /**
     * A PNG whose eXIf chunk carries a Make field and the orientation of
     * the given value, built by rewriting the committed original with an
     * orientation update.
     */
    private fun pngWithOrientation(orientation: TiffOrientation): ByteArray =
        Kim.update(
            bytes = Resource(
                "de/stefan_oltmann/kim/updates_png/original.png"
            ).readBytes(),
            update = MetadataUpdate.Orientation(orientation)
        )

    /**
     * A JPEG whose EXIF segment carries a Make field and the orientation
     * of the given value, built by rewriting the committed original with
     * an orientation update.
     */
    private fun jpegWithOrientation(orientation: TiffOrientation): ByteArray =
        Kim.update(
            bytes = Resource(
                "de/stefan_oltmann/kim/updates_jpg/original.jpg"
            ).readBytes(),
            update = MetadataUpdate.Orientation(orientation)
        )

    /**
     * A PNG whose legacy text Exif representation carries the given
     * orientation: the committed Exif text resource is hex patched to
     * the rotation, since its own tag reads 1. The patched entry is the
     * orientation tag itself - tag 0x0112, SHORT, count 1, little
     * endian value.
     */
    private fun pngWithLegacyTextExif(orientation: TiffOrientation): ByteArray {

        val exifText = Resource(
            "de/stefan_oltmann/kim/testdata/headers/media_51_header_exif.txt"
        ).readBytes().decodeToString()

        val rotatedText = exifText.replace(
            oldValue = "120103000100000001000000",
            newValue = "1201030001000000" +
                orientation.value.toString(16).padStart(2, '0') + "000000",
            ignoreCase = true
        )

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        writeChunk(
            byteWriter = byteWriter,
            typeName = "tEXt",
            data = "Raw profile type exif\u0000".encodeToByteArray() + rotatedText.encodeToByteArray()
        )

        writeChunk(byteWriter = byteWriter, typeName = "IDAT", data = byteArrayOf(1, 2, 3, 4))

        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        return byteWriter.toByteArray()
    }

    /**
     * A WebP whose EXIF chunk carries a Make field and the given
     * orientation, built by writing the committed original with the EXIF
     * chunk injected.
     */
    private fun webPWithOrientation(orientation: TiffOrientation): ByteArray {

        val originalChunks = WebPImageParser.readChunks(
            ByteArrayByteReader(
                Resource("de/stefan_oltmann/kim/updates_webp/original.webp").readBytes()
            ),
            stopAfterMetadataRead = false
        )

        val byteWriter = ByteArrayByteWriter()

        WebPWriter.writeImage(
            chunks = originalChunks,
            byteWriter = byteWriter,
            exifBytes = exifBytesWith(orientation.value.toShort()),
            xmp = null
        )

        return byteWriter.toByteArray()
    }

    /**
     * A JXL whose Exif box carries a Make field and the given
     * orientation, built by streaming the committed original with the
     * Exif box injected - the streaming writer carries the codestream
     * over, which the plain writer would not.
     */
    private fun jxlWithOrientation(orientation: TiffOrientation): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        JxlWriter.writeImageStreaming(
            byteReader = ByteArrayByteReader(
                Resource("de/stefan_oltmann/kim/updates_jxl/original.jxl").readBytes()
            ),
            byteWriter = byteWriter
        ) { boxes, outputWriter ->

            JxlWriter.writeImage(
                boxes = boxes,
                byteWriter = outputWriter,
                exifBytes = exifBytesWith(orientation.value.toShort()),
                xmp = null
            )

            setOf()
        }

        return byteWriter.toByteArray()
    }

    /**
     * The PNG chunk type names of the file in order.
     */
    private fun pngChunkTypes(pngBytes: ByteArray): List<String> =

        PngImageParser.readChunks(
            ByteArrayByteReader(pngBytes),
            chunkTypeFilter = null
        ).map { it.type.name }

    /**
     * PNG: the minimal eXIf chunk replaces the original one at its own
     * position, and everything besides the orientation is gone.
     */
    @Test
    fun testPngReplacesExifChunkInPlaceWithMinimalOrientation() {

        val originalBytes = pngWithOrientation(TiffOrientation.ROTATE_RIGHT)

        val originalExifIndex = pngChunkTypes(originalBytes).indexOf("eXIf")

        val newBytes = Kim.deleteMetadata(originalBytes)

        /* The replacement sits exactly where the original chunk was. */
        assertEquals(originalExifIndex, pngChunkTypes(newBytes).indexOf("eXIf"))

        val metadata = Kim.readMetadata(newBytes)

        assertEquals(6, metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
        assertNull(metadata?.findStringValue(TiffTag.TIFF_TAG_MAKE))
    }

    /**
     * PNG: a legacy text Exif representation is modernized - the
     * rotation moves into a minimal eXIf chunk at the position of the
     * text chunk, and the text chunk goes.
     */
    @Test
    fun testPngReplacesLegacyTextExifWithModernExifChunk() {

        val originalBytes = pngWithLegacyTextExif(TiffOrientation.ROTATE_RIGHT)

        val originalTextIndex = pngChunkTypes(originalBytes).indexOf("tEXt")

        val newBytes = Kim.deleteMetadata(originalBytes)

        val chunkTypes = pngChunkTypes(newBytes)

        assertEquals(originalTextIndex, chunkTypes.indexOf("eXIf"))
        assertEquals(0, chunkTypes.count { it == "tEXt" })

        val metadata = Kim.readMetadata(newBytes)

        assertEquals(6, metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
        assertNull(metadata?.findStringValue(TiffTag.TIFF_TAG_MAKE))
    }

    /**
     * Two text chunks claiming to be Exif produce exactly one minimal
     * eXIf chunk, carrying the orientation of the first one - the read
     * path is first-wins, and the output can never hold double Exif.
     */
    @Test
    fun testPngWritesExactlyOneExifChunkForMultipleLegacySources() {

        val exifText = Resource(
            "de/stefan_oltmann/kim/testdata/headers/media_51_header_exif.txt"
        ).readBytes().decodeToString()

        val rotatedOnce = exifText.replace(
            oldValue = "120103000100000001000000",
            newValue = "120103000100000006000000",
            ignoreCase = true
        )

        val rotatedTwice = exifText.replace(
            oldValue = "120103000100000001000000",
            newValue = "120103000100000005000000",
            ignoreCase = true
        )

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        writeChunk(
            byteWriter = byteWriter,
            typeName = "tEXt",
            data = "Raw profile type exif\u0000".encodeToByteArray() + rotatedOnce.encodeToByteArray()
        )

        writeChunk(
            byteWriter = byteWriter,
            typeName = "tEXt",
            data = "Raw profile type exif\u0000".encodeToByteArray() + rotatedTwice.encodeToByteArray()
        )

        writeChunk(byteWriter = byteWriter, typeName = "IDAT", data = byteArrayOf(1, 2, 3, 4))

        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        val newBytes = Kim.deleteMetadata(byteWriter.toByteArray())

        val chunkTypes = pngChunkTypes(newBytes)

        assertEquals(1, chunkTypes.count { it == "eXIf" })

        val metadata = Kim.readMetadata(newBytes)

        assertEquals(6, metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
    }

    /**
     * An explicitly stored orientation survives the removal even when
     * it is the unrotated 1: the committed original states the value,
     * and the deletion must not drop a stated value. The minimal eXIf
     * chunk replaces the original one and carries nothing else.
     */
    @Test
    fun testPngPreservesExplicitUnrotatedOrientation() {

        val newBytes = Kim.deleteMetadata(
            Resource("de/stefan_oltmann/kim/updates_png/original.png").readBytes()
        )

        assertEquals(1, pngChunkTypes(newBytes).count { it == "eXIf" })

        val metadata = Kim.readMetadata(newBytes)

        assertEquals(1, metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
        assertNull(metadata?.findStringValue(TiffTag.TIFF_TAG_MAKE))
    }

    /**
     * An EXIF container without an orientation tag fabricates nothing:
     * per the specification an absent tag means 1, so writing a chunk
     * for it would state nothing the file did not state itself, and the
     * deletion writes no eXIf chunk at all.
     */
    @Test
    fun testPngWritesNoExifChunkWhenOrientationIsAbsent() {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        writeChunk(
            byteWriter = byteWriter,
            typeName = "eXIf",
            data = exifBytesWithoutOrientation()
        )

        writeChunk(byteWriter = byteWriter, typeName = "IDAT", data = byteArrayOf(1, 2, 3, 4))

        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        val newBytes = Kim.deleteMetadata(byteWriter.toByteArray())

        assertEquals(0, pngChunkTypes(newBytes).count { it == "eXIf" })
    }

    /**
     * With the flag off, the deletion behaves like before: no eXIf
     * chunk is written even for a rotated image.
     */
    @Test
    fun testPngKeepOrientationFlagFalseWritesNoExifChunk() {

        val newBytes = Kim.deleteMetadata(
            bytes = pngWithOrientation(TiffOrientation.ROTATE_RIGHT),
            keepIccProfile = true,
            keepOrientationFlag = false
        )

        assertEquals(0, pngChunkTypes(newBytes).count { it == "eXIf" })
    }

    /**
     * With orientation preservation the eXIf content becomes relevant,
     * so it is parsed like the read parses it: a corrupt chunk fails the
     * operation instead of losing the rotation silently.
     */
    @Test
    fun testPngFailsWhenExifIsCorruptAndOrientationIsKept() {

        val byteWriter = ByteArrayByteWriter()

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        writeChunk(
            byteWriter = byteWriter,
            typeName = "IHDR",
            data = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0)
        )

        writeChunk(
            byteWriter = byteWriter,
            typeName = "eXIf",
            data = byteArrayOf(1, 2, 3)
        )

        writeChunk(byteWriter = byteWriter, typeName = "IDAT", data = byteArrayOf(1, 2, 3, 4))

        writeChunk(byteWriter = byteWriter, typeName = "IEND", data = byteArrayOf())

        assertFailsWith<ImageWriteException> {
            Kim.deleteMetadata(byteWriter.toByteArray())
        }
    }

    /**
     * JPEG: the minimal EXIF segment replaces the original one at its
     * own position, and everything besides the orientation is gone.
     */
    @Test
    fun testJpegReplacesExifSegmentInPlaceWithMinimalOrientation() {

        val newBytes = Kim.deleteMetadata(jpegWithOrientation(TiffOrientation.ROTATE_RIGHT))

        val metadata = Kim.readMetadata(newBytes)

        assertEquals(6, metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
        assertNull(metadata?.findStringValue(TiffTag.TIFF_TAG_MAKE))
    }

    /**
     * JPEG: an explicitly stored unrotated orientation survives like a
     * rotation does - the stated value is preserved, not dropped.
     */
    @Test
    fun testJpegPreservesExplicitUnrotatedOrientation() {

        val newBytes = Kim.deleteMetadata(jpegWithOrientation(TiffOrientation.STANDARD))

        val metadata = Kim.readMetadata(newBytes)

        assertEquals(1, metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
        assertNull(metadata?.findStringValue(TiffTag.TIFF_TAG_MAKE))
    }

    /**
     * WebP: the minimal EXIF chunk replaces the original one, and
     * everything besides the orientation is gone.
     */
    @Test
    fun testWebPReplacesExifChunkWithMinimalOrientation() {

        val newBytes = Kim.deleteMetadata(webPWithOrientation(TiffOrientation.ROTATE_RIGHT))

        val metadata = Kim.readMetadata(newBytes)

        assertEquals(6, metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
        assertNull(metadata?.findStringValue(TiffTag.TIFF_TAG_MAKE))
    }

    /**
     * WebP: an explicitly stored unrotated orientation survives like a
     * rotation does - the stated value is preserved, not dropped.
     */
    @Test
    fun testWebPPreservesExplicitUnrotatedOrientation() {

        val newBytes = Kim.deleteMetadata(webPWithOrientation(TiffOrientation.STANDARD))

        val metadata = Kim.readMetadata(newBytes)

        assertEquals(1, metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
        assertNull(metadata?.findStringValue(TiffTag.TIFF_TAG_MAKE))
    }

    /**
     * JXL: the minimal Exif box replaces the original one, and
     * everything besides the orientation is gone.
     */
    @Test
    fun testJxlReplacesExifBoxWithMinimalOrientation() {

        val newBytes = Kim.deleteMetadata(jxlWithOrientation(TiffOrientation.ROTATE_RIGHT))

        val metadata = Kim.readMetadata(newBytes)

        assertEquals(6, metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
        assertNull(metadata?.findStringValue(TiffTag.TIFF_TAG_MAKE))
    }

    /**
     * JXL: an explicitly stored unrotated orientation survives like a
     * rotation does - the stated value is preserved, not dropped.
     */
    @Test
    fun testJxlPreservesExplicitUnrotatedOrientation() {

        val newBytes = Kim.deleteMetadata(jxlWithOrientation(TiffOrientation.STANDARD))

        val metadata = Kim.readMetadata(newBytes)

        assertEquals(1, metadata?.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())
        assertNull(metadata?.findStringValue(TiffTag.TIFF_TAG_MAKE))
    }

    /**
     * Writes a single well-formed PNG chunk with a correct CRC.
     */
    private fun writeChunk(
        byteWriter: ByteArrayByteWriter,
        typeName: String,
        data: ByteArray
    ) {

        val typeBytes = typeName.encodeToByteArray()

        byteWriter.writeInt(data.size, PngConstants.PNG_BYTE_ORDER)
        byteWriter.write(typeBytes)
        byteWriter.write(data)

        val crc =
            finishPartialCrc(continuePartialCrc(startPartialCrc(typeBytes), data)).toInt()

        byteWriter.writeInt(crc, PngConstants.PNG_BYTE_ORDER)
    }
}
