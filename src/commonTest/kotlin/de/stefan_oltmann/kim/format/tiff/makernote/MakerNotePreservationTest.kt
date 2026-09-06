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
package de.stefan_oltmann.kim.format.tiff.makernote

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeUndefined
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputField
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.datetime.TimeZone
import kotlin.test.BeforeTest
import kotlin.test.assertFailsWith
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Regression tests for the byte-exact preservation of the MakerNote
 * on rewrite.
 *
 * The writer keeps the MakerNote value byte for byte at its original
 * offset: both the value bytes and the offset within the EXIF segment
 * have to survive a metadata update unchanged, or the photo would be
 * damaged.
 */
class MakerNotePreservationTest {

    @BeforeTest
    fun setUp() {
        Kim.defaultTimeZone = TimeZone.of("GMT+02:00")
    }

    /**
     * Regression test: every JPEG and PNG test file with a MakerNote
     * keeps its MakerNote bytes and offset after a metadata update.
     */
    @Test
    fun testMakerNoteSurvivesUpdateByteIdentically() {

        var checkedFileCount = 0

        for (index in testMediaIndices) {

            /* FIXME files that the rewrite tests skip as well. */
            if (index in unrewritableIndices)
                continue

            val bytes = KimTestData.getBytesOf(index)

            val originalField = findMakerNoteField(bytes) ?: continue

            val updatedBytes = Kim.update(
                bytes = bytes,
                update = MetadataUpdate.TakenDate(TEST_TAKEN_DATE_MILLIS)
            )

            val updatedField = findMakerNoteField(updatedBytes)
                ?: fail("Update of media_$index dropped the MakerNote.")

            assertContentEquals(
                expected = originalField.valueBytes,
                actual = updatedField.valueBytes,
                message = "MakerNote bytes of media_$index changed on update."
            )

            assertEquals(
                expected = originalField.valueOffset,
                actual = updatedField.valueOffset,
                message = "MakerNote offset of media_$index changed on update."
            )

            checkedFileCount++
        }

        assertTrue(
            checkedFileCount > 0,
            "The test must check at least one file with a MakerNote."
        )
    }

    /**
     * Regression test: like ExifTool, a file with a broken MakerNote
     * sub-directory is still updatable, and the MakerNote survives the
     * update byte for byte.
     */
    @Test
    fun testBrokenMakerNoteSurvivesUpdateByteIdentically() {

        val bytes = BrokenMakerNoteTiff.buildJpeg()

        val originalField = assertNotNull(
            findMakerNoteField(bytes),
            "The test JPEG must contain a MakerNote."
        )

        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.TakenDate(TEST_TAKEN_DATE_MILLIS)
        )

        val updatedField = assertNotNull(
            findMakerNoteField(updatedBytes),
            "Update of the file with the broken MakerNote dropped it."
        )

        assertContentEquals(
            expected = originalField.valueBytes,
            actual = updatedField.valueBytes,
            message = "MakerNote bytes changed on update."
        )

        assertEquals(
            expected = originalField.valueOffset,
            actual = updatedField.valueOffset,
            message = "MakerNote offset changed on update."
        )
    }

    /**
     * Regression test: the MakerNote keeps its offset even when the
     * content before it grows beyond that offset.
     *
     * The writer defers items that would overlap the MakerNote region,
     * so growing IFD0 values can never push it away.
     */
    @Test
    fun testMakerNoteKeepsOffsetWhenEarlierContentGrows() {

        val bytes = KimTestData.getBytesOf(1)

        val originalField = assertNotNull(
            findMakerNoteField(bytes),
            "media_1 must contain a MakerNote."
        )

        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.Description("Long description ".repeat(200))
        )

        val updatedField = assertNotNull(
            findMakerNoteField(updatedBytes),
            "Update of media_1 dropped the MakerNote."
        )

        assertContentEquals(
            expected = originalField.valueBytes,
            actual = updatedField.valueBytes,
            message = "MakerNote bytes changed on update."
        )

        assertEquals(
            expected = originalField.valueOffset,
            actual = updatedField.valueOffset,
            message = "MakerNote offset changed on update."
        )
    }

    /**
     * Regression test: items that fit before the MakerNote are written
     * before it, so the file starts with the IFD0 directory instead of
     * a large zero block.
     */
    @Test
    fun testContentBeforeMakerNoteIsWrittenFirst() {

        val bytes = KimTestData.getBytesOf(50)

        val originalField = assertNotNull(
            findMakerNoteField(bytes),
            "media_50 must contain a MakerNote."
        )

        val originalOffset = assertNotNull(
            originalField.valueOffset,
            "The MakerNote of media_50 must have a value offset."
        )

        val updatedBytes = Kim.update(
            bytes = bytes,
            update = MetadataUpdate.TakenDate(TEST_TAKEN_DATE_MILLIS)
        )

        val exif = assertNotNull(
            Kim.readMetadata(updatedBytes)?.exif,
            "The updated file must contain EXIF data."
        )

        assertEquals(
            expected = TiffConstants.TIFF_HEADER_SIZE,
            actual = exif.header.offsetToFirstIFD,
            message = "IFD0 must be written directly behind the TIFF header."
        )

        assertTrue(
            exif.header.offsetToFirstIFD < originalOffset,
            "IFD0 must be written before the MakerNote."
        )
    }

    /**
     * The MakerNote must never move - at all costs. If a write would
     * relocate it (anchor impossible to honor), the write must fail
     * loudly instead of producing a file with corrupted vendor offsets.
     */
    @Test
    fun testMakerNoteRelocationFailsTheWrite() {

        val outputSet = TiffOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        rootDirectory.add(TiffTag.TIFF_TAG_MAKE, "Canon")

        val makerNoteField = TiffOutputField(
            tag = ExifTag.EXIF_TAG_MAKER_NOTE.tag,
            fieldType = FieldTypeUndefined,
            count = 8,
            bytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        )

        /* An anchor before the TIFF header (8 bytes) can never be
           honored - the MakerNote would have to move. */
        makerNoteField.originalOffset = 4

        rootDirectory.add(makerNoteField)

        val byteWriter = ByteArrayByteWriter()

        assertFailsWith<ImageWriteException> {
            TiffWriter(outputSet.byteOrder).write(byteWriter, outputSet)
        }
    }

    /**
     * Returns the MakerNote field of the given bytes, or null when
     * the file does not contain a MakerNote.
     */
    private fun findMakerNoteField(bytes: ByteArray): TiffField? {

        val contents = assertNotNull(Kim.readMetadata(bytes)?.exif)

        return TiffDirectory.findTiffField(contents.directories, ExifTag.EXIF_TAG_MAKER_NOTE)
    }

    /**
     * Regression test: the indices in [unrewritableIndices] must stay
     * unrewritable. If the underlying files become parseable, this test
     * fails and forces re-inclusion into [testMakerNoteSurvivesUpdateByteIdentically].
     */
    @Test
    fun testUnrewritableIndicesAreStillUnrewritable() {

        for (index in unrewritableIndices) {

            val bytes = KimTestData.getBytesOf(index)

            var threw = false

            try {
                Kim.update(
                    bytes = bytes,
                    update = MetadataUpdate.TakenDate(TEST_TAKEN_DATE_MILLIS)
                )
            } catch (_: Exception) {
                threw = true
            }

            assertTrue(
                threw,
                "media_$index was marked unrewritable but Kim.update succeeded. " +
                    "Remove $index from unrewritableIndices."
            )
        }
    }

    private companion object {

        /* The JPEG files with update support and the PNG files. */
        val testMediaIndices: List<Int> = (1..KimTestData.HIGHEST_JPEG_INDEX).toList() +
            listOf(
                KimTestData.PNG_TEST_IMAGE_INDEX,
                KimTestData.PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX,
                KimTestData.PNG_GIMP_TEST_IMAGE_INDEX
            )

        /*
         * Files that contain invalid segment lengths (44, 45, 47) and are
         * rejected by the rewriter. Keep this set in sync with
         * KotlinIoPathSourceTest.rejectedJpegIds and KimUpdateSmallFileTest.
         * If a file becomes parseable, remove it here so the MakerNote
         * preservation check covers it.
         */
        val unrewritableIndices: Set<Int> = setOf(44, 45, 47)

        const val TEST_TAKEN_DATE_MILLIS: Long = 1_575_302_400_000
    }
}
