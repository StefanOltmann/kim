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

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.tiff.constant.GeoTiffTag
import de.stefan_oltmann.kim.format.tiff.geotiff.GeoTiffDirectory
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.writeInt
import de.stefan_oltmann.kim.output.write2BytesAsInt
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
     * A negative NumberOfKeys is corrupt (the shorts are unsigned in
     * spirit). The bounds check would silently treat it as zero keys,
     * dropping the directory - the read must fail instead.
     */
    @Test
    fun testParseFromRejectsNegativeNumberOfKeys() {

        assertFailsWith<ImageReadException> {
            GeoTiffDirectory.parseFrom(shortArrayOf(1, 1, 1, -1))
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
    /**
     * A TIFF whose GeoKeyDirectory cannot be parsed must fail the read.
     * Geo keys are structured metadata; per the strict read policy a
     * silently missing directory is a policy violation, not a degradation.
     */
    @Test
    fun testTiffReadFailsOnUnparseableGeoKeyDirectory() {

        val bytes = buildTiffWithGeoKeyDirectory(shortArrayOf(1, 1, 0, 0x7FFF))

        assertFailsWith<ImageReadException> {
            TiffReader.read(bytes)
        }
    }

    /**
     * Builds a minimal little-endian TIFF whose IFD0 carries the
     * GeoKeyDirectory tag with the given shorts (offset-based, since four
     * shorts exceed the four inline value bytes).
     */
    private fun buildTiffWithGeoKeyDirectory(geoShorts: ShortArray): ByteArray {

        val out = ByteArrayByteWriter()

        val dataOffset = 8 + 2 + ENTRY_LENGTH + 4

        out.write(byteArrayOf(0x49, 0x49, 0x2A, 0x00)) // TIFF header.
        out.writeInt(8, ByteOrder.LITTLE_ENDIAN)

        out.write2BytesAsInt(1, ByteOrder.LITTLE_ENDIAN)

        out.write2BytesAsInt(GeoTiffTag.EXIF_TAG_GEO_KEY_DIRECTORY_TAG.tag, ByteOrder.LITTLE_ENDIAN)
        out.write2BytesAsInt(TYPE_SHORT, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(geoShorts.size, ByteOrder.LITTLE_ENDIAN)
        out.writeInt(dataOffset, ByteOrder.LITTLE_ENDIAN)

        out.writeInt(0, ByteOrder.LITTLE_ENDIAN) // No next IFD.

        for (short in geoShorts) {
            out.write(byteArrayOf(
                (short.toInt() and 0xFF).toByte(),
                ((short.toInt() shr 8) and 0xFF).toByte()
            ))
        }

        return out.toByteArray()
    }

    private companion object {

        private const val TYPE_SHORT: Int = 3

        private const val ENTRY_LENGTH: Int = 12
    }
}
