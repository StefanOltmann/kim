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
package de.stefan_oltmann.kim.format.tiff.makernote.fujifilm

import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.DefaultRandomAccessByteReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FujiFilmFocusSettingsTagTest {

    /**
     * ExifTool reads the FocusSettings and DriveSettings blobs as one
     * int32u per entry and applies the 32-bit masks to it - the dump of
     * media_58 reports AFAreaMode = 2 for the FocusSettings blob
     * `12 02 00 00` and DriveSpeed = 8 for the DriveSettings blob
     * `01 03 01 08`. The masked entries must therefore read four bytes,
     * not one, or the high-byte masks always evaluate to zero.
     */
    @Test
    fun testMaskedEntriesReadTheFullInt32() {

        /*
         * A FujiFilm MakerNote: signature, version and a little-endian
         * IFD with the FocusSettings and DriveSettings blobs inline.
         */
        val makerNoteBytes = byteArrayOf(
            /* "FUJIFILM" */
            0x46, 0x55, 0x4A, 0x49, 0x46, 0x49, 0x4C, 0x4D,
            /* Version. */
            0x01, 0x00, 0x00, 0x00,
            /* IFD entry count. */
            2, 0,
            /* Tag 0x102D FocusSettings. */
            0x2D, 0x10,
            /* Type UNDEFINED. */
            7, 0,
            /* Count 4. */
            4, 0, 0, 0,
            /* The blob, stored inline. */
            0x12, 0x02, 0x00, 0x00,
            /* Tag 0x1103 DriveSettings. */
            0x03, 0x11,
            /* Type UNDEFINED. */
            7, 0,
            /* Count 4. */
            4, 0, 0, 0,
            /* The blob, stored inline. */
            0x01, 0x03, 0x01, 0x08,
            /* No next IFD. */
            0, 0, 0, 0
        )

        val directories = mutableListOf<TiffDirectory>()

        FujiFilmMakerNoteHandler.read(
            byteReader = DefaultRandomAccessByteReader(ByteArrayByteReader(makerNoteBytes)),
            makerNoteValueOffset = 0,
            addDirectory = { directory -> directories.add(directory) }
        )

        val focusSettings = directories.first { directory ->
            directory.type == TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_FOCUS_SETTINGS
        }

        val afAreaMode = assertNotNull(
            focusSettings.entries.find { it.tagInfoOverride?.name == "AFAreaMode" }
        )

        /* ExifTool reports 2 (Wide/Tracking) for this blob. */
        assertEquals("2", afAreaMode.valueDescription)

        val driveSettings = directories.first { directory ->
            directory.type == TiffConstants.TIFF_MAKER_NOTE_FUJIFILM_DRIVE_SETTINGS
        }

        val driveSpeed = assertNotNull(
            driveSettings.entries.find { it.tagInfoOverride?.name == "DriveSpeed" }
        )

        /* ExifTool reports 8 for this blob. */
        assertEquals("8", driveSpeed.valueDescription)
    }
}
