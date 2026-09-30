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

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeUndefined
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonPictureControl2Tag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonPictureControlTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MakerNoteVersionDispatchTest {

    /**
     * Exposes the protected blob sub-directory walk of the shared
     * MakerNoteHandler for the dispatch tests.
     */
    private class TestMakerNoteHandler : MakerNoteHandler() {

        fun readSubDirectories(
            directory: TiffDirectory,
            blobPointers: List<MakerNoteBlobPointer>,
            addDirectory: (TiffDirectory) -> Unit
        ) = readMakerNoteBlobSubDirectories(
            directory = directory,
            byteOrder = ByteOrder.LITTLE_ENDIAN,
            blobPointers = blobPointers,
            addDirectory = addDirectory
        )
    }

    /**
     * ExifTool dispatches the Nikon PictureControl blob on the version
     * PREFIX ("/^02/" selects the PictureControl2 layout), not on an
     * exact version string. Blobs with versions 0201 to 0204 must use
     * the 2-byte-spaced layout, or Sharpness reads the wrong offset and
     * Clarity disappears.
     */
    @Test
    fun testPictureControlVersionPrefixSelectsTheVersion2Layout() {

        /* A PictureControl2 blob with version bytes 0201. */
        val blob = ByteArray(0x40)

        "0201".encodeToByteArray().copyInto(blob)

        blob[0x33] = 7 /* Sharpness of the 2-byte layout. */

        val directory = TiffDirectory(
            type = TiffConstants.TIFF_MAKER_NOTE_NIKON,
            entries = listOf(
                TiffField(
                    offset = 0,
                    tag = 0x0023,
                    directoryType = TiffConstants.TIFF_MAKER_NOTE_NIKON,
                    fieldType = FieldTypeUndefined,
                    count = blob.size,
                    localValue = null,
                    valueOffset = 0,
                    valueBytes = blob,
                    byteOrder = ByteOrder.LITTLE_ENDIAN,
                    sortHint = 0x0023
                )
            ),
            offset = 0,
            nextDirectoryOffset = 0,
            byteOrder = ByteOrder.LITTLE_ENDIAN
        )

        /* The same dispatch NikonMakerNoteHandler registers. */
        val pointer = MakerNoteBlobPointer(
            tagId = 0x0023,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_NIKON_PICTURE_CONTROL,
            tagTable = NikonPictureControlTag.ALL,
            byteOffsetMultiplier = 1,
            versionTables = mapOf(
                "02" to MakerNoteBlobPointer(
                    tagId = 0x0023,
                    directoryType = TiffConstants.TIFF_MAKER_NOTE_NIKON_PICTURE_CONTROL,
                    tagTable = NikonPictureControl2Tag.ALL,
                    byteOffsetMultiplier = 1
                )
            )
        )

        val directories = mutableListOf<TiffDirectory>()

        TestMakerNoteHandler().readSubDirectories(directory, listOf(pointer)) { subDirectory ->
            directories.add(subDirectory)
        }

        val pictureControl = directories.first()

        val sharpness = assertNotNull(
            pictureControl.entries.find { it.tagInfoOverride?.name == "Sharpness" }
        )

        assertEquals("7", sharpness.valueDescription)

        /* The Clarity entry only exists in the version 2 layout. */
        assertNotNull(
            pictureControl.entries.find { it.tagInfoOverride?.name == "Clarity" },
            "The version 2 layout was not used for version bytes 0201."
        )
    }
}
