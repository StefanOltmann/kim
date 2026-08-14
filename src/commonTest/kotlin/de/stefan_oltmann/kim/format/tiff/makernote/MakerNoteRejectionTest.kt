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
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Regression tests for the ExifTool-compatible handling of broken
 * MakerNotes.
 *
 * Like ExifTool, an unreadable MakerNote sub-directory is skipped
 * while the MakerNote is kept as an opaque binary block, and only a
 * MakerNote field that cannot be read at all rejects the file.
 */
class MakerNoteRejectionTest {

    /**
     * Regression test: a MakerNote sub-directory that points to
     * corrupted data is skipped instead of rejecting the file.
     */
    @Test
    fun testBrokenMakerNoteSubDirectoryIsSkipped() {

        val tiffBytes = BrokenMakerNoteTiff.buildTiff(makerNoteValueOffsetHex = "40000000")

        val metadata = assertNotNull(Kim.readMetadata(tiffBytes))

        val makerNoteField = TiffDirectory.findTiffField(
            assertNotNull(metadata.exif).directories,
            ExifTag.EXIF_TAG_MAKER_NOTE
        )

        assertNotNull(makerNoteField, "The MakerNote field must be preserved.")

        /* The broken FocusInfoIFD sub-directory must not appear. */
        assertNull(
            metadata.exif.findMakerNoteSubDirectory(
                TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_FOCUS_INFO
            )
        )
    }

    /**
     * Regression test: a MakerNote field whose value cannot be read
     * rejects the file, matching ExifTool's fatal error for this case.
     */
    @Test
    fun testUnreadableMakerNoteFieldRejectsFile() {

        /* The MakerNote value offset points beyond the file. */
        val tiffBytes = BrokenMakerNoteTiff.buildTiff(makerNoteValueOffsetHex = "ffffff00")

        assertFailsWith<ImageReadException> {
            Kim.readMetadata(tiffBytes)
        }
    }
}
