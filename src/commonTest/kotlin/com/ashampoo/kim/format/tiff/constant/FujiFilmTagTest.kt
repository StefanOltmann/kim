/*
 * Copyright 2026 Gnod
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
package de.stefan_oltmann.kim.format.tiff.constant

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FujiFilmTagTest {

    @Test
    fun testFilmModeNames() {

        assertEquals(
            expected = "Provia/Standard",
            actual = FujiFilmTag.getFilmModeName(FujiFilmTag.FILM_MODE_PROVIA_STANDARD)
        )

        assertEquals(
            expected = "Velvia/Vivid",
            actual = FujiFilmTag.getFilmModeName(FujiFilmTag.FILM_MODE_VELVIA_VIVID)
        )

        assertEquals(
            expected = "Astia/Soft",
            actual = FujiFilmTag.getFilmModeName(FujiFilmTag.FILM_MODE_ASTIA_SOFT)
        )

        assertEquals(
            expected = "Classic Chrome",
            actual = FujiFilmTag.getFilmModeName(FujiFilmTag.FILM_MODE_CLASSIC_CHROME)
        )

        assertEquals(
            expected = "Classic Negative",
            actual = FujiFilmTag.getFilmModeName(FujiFilmTag.FILM_MODE_CLASSIC_NEG)
        )

        assertEquals(
            expected = "Eterna",
            actual = FujiFilmTag.getFilmModeName(FujiFilmTag.FILM_MODE_ETERNA)
        )

        assertEquals(
            expected = "Nostalgic Neg",
            actual = FujiFilmTag.getFilmModeName(FujiFilmTag.FILM_MODE_NOSTALGIC_NEG)
        )

        assertEquals(
            expected = "Reala ACE",
            actual = FujiFilmTag.getFilmModeName(FujiFilmTag.FILM_MODE_REALA_ACE)
        )

        assertEquals(
            expected = "Pro Neg. Std",
            actual = FujiFilmTag.getFilmModeName(FujiFilmTag.FILM_MODE_PRO_NEG_STD)
        )

        assertEquals(
            expected = "Pro Neg. Hi",
            actual = FujiFilmTag.getFilmModeName(FujiFilmTag.FILM_MODE_PRO_NEG_HI)
        )

        assertNull(FujiFilmTag.getFilmModeName(0x999))
    }

    @Test
    fun testTagValues() {

        assertEquals(0x0000, FujiFilmTag.MAKER_NOTE_VERSION.tag)
        assertEquals(0x0010, FujiFilmTag.INTERNAL_SERIAL_NUMBER.tag)
        assertEquals(0x1000, FujiFilmTag.QUALITY.tag)
        assertEquals(0x1001, FujiFilmTag.SHARPNESS.tag)
        assertEquals(0x1002, FujiFilmTag.WHITE_BALANCE.tag)
        assertEquals(0x1003, FujiFilmTag.SATURATION.tag)
        assertEquals(0x1004, FujiFilmTag.CONTRAST.tag)
        assertEquals(0x1400, FujiFilmTag.DYNAMIC_RANGE.tag)
        assertEquals(0x1401, FujiFilmTag.FILM_MODE.tag)
        assertEquals(0x1402, FujiFilmTag.DYNAMIC_RANGE_SETTING.tag)
    }

    @Test
    fun testDirectoryType() {

        assertEquals(
            TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_FUJIFILM,
            FujiFilmTag.FILM_MODE.directoryType
        )
    }
}
