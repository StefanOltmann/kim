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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.tiff.geotiff.GeoTiffDirectory
import kotlin.test.Test
import kotlin.test.assertFailsWith

class GeoTiffDirectoryTest {

    /**
     * A GeoKey directory claiming more keys than the array holds is
     * hostile input. It must fail with an ImageReadException instead of
     * a raw index exception escaping this public API.
     */
    @Test
    fun testParseFromRejectsNumberOfKeysBeyondArray() {

        /* Version 1, revision 1, minor 0, NumberOfKeys 0x7FFF - no keys follow. */
        val shorts = shortArrayOf(1, 1, 0, 0x7FFF)

        assertFailsWith<ImageReadException> {
            GeoTiffDirectory.parseFrom(shorts)
        }
    }

    /**
     * A directory smaller than the mandatory key header has no keys to
     * parse and must be rejected like any other corrupt directory.
     */
    @Test
    fun testParseFromRejectsUndersizedDirectory() {

        assertFailsWith<ImageReadException> {
            GeoTiffDirectory.parseFrom(shortArrayOf(1, 1))
        }
    }
}
