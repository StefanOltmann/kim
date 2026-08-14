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

import de.stefan_oltmann.kim.format.tiff.makernote.apple.AppleTag
import de.stefan_oltmann.kim.format.tiff.makernote.canon.CanonTag
import de.stefan_oltmann.kim.format.tiff.makernote.fujifilm.FujiFilmTag
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonTag
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusTag
import de.stefan_oltmann.kim.format.tiff.makernote.panasonic.PanasonicTag
import de.stefan_oltmann.kim.format.tiff.makernote.sony.SonyTag
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Verifies that the MakerNote tag tables list every tag exactly once
 * and in the same (ascending tag number) order as the ExifTool
 * documentation.
 */
class MakerNoteTagTableTest {

    @Test
    fun testAllTagTablesAreSortedAndUnique() {

        val tables = mapOf(
            "Apple" to AppleTag.ALL,
            "Canon" to CanonTag.ALL,
            "FujiFilm" to FujiFilmTag.ALL,
            "Nikon" to NikonTag.ALL,
            "Olympus" to OlympusTag.ALL,
            "Panasonic" to PanasonicTag.ALL,
            "Sony" to SonyTag.ALL
        )

        for ((name, tags) in tables)
            assertSortedAndUnique(name, tags)
    }

    private fun assertSortedAndUnique(name: String, tags: List<TagInfo>) {

        var previousTag = -1

        for (tag in tags) {

            if (tag.tag <= previousTag)
                fail(
                    "$name table is not sorted: ${toHex(tag.tag)} after ${toHex(previousTag)}"
                )

            previousTag = tag.tag
        }

        assertEquals(tags.size, tags.map { it.tag }.toSet().size, "$name table has duplicate tags")
    }

    private fun toHex(value: Int): String {

        val hex = value.toString(16).uppercase()

        return "0x" + hex.padStart(4, '0')
    }
}
