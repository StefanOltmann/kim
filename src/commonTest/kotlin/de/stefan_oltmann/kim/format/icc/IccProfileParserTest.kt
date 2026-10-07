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
package de.stefan_oltmann.kim.format.icc

import de.stefan_oltmann.kim.common.ImageReadException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * The parser reads the fixed header layout and dispatches the tag
 * values on their own type signatures. The expected values follow the
 * ICC specification; corrupt layouts must fail the read instead of
 * producing a partial profile.
 */
class IccProfileParserTest {

    private val fixture = IccFixture()

    @Test
    fun testParsesHeaderAndAllValueTypes() {

        val profile = IccProfileParser.parse(fixture.bytes)

        assertEquals(fixture.bytes.size, profile.size)
        assertEquals("lcms", profile.cmmType)
        assertEquals("2.1", profile.version)
        assertEquals("mntr", profile.profileClass)
        assertEquals("RGB ", profile.colorSpace)
        assertEquals("XYZ ", profile.connectionSpace)
        assertNull(profile.primaryPlatform)
        assertEquals(0, profile.renderingIntent)

        /* The legacy text description decodes without its padding zeros. */
        assertEquals("Test profile", profile.description)

        /* The multi-language unicode text decodes as UTF-16 big endian. */
        assertEquals(
            expected = "Öffentlich",
            actual = profile.findEntry("ProfileCopyright")?.value
        )

        /* The XYZ value renders its three s15Fixed16 numbers. */
        assertEquals(
            expected = "0.964202880859375 1.0 0.8249053955078125",
            actual = profile.findEntry("MediaWhitePoint")?.value
        )

        /* An undecoded binary type keeps the entry without a value. */
        assertNull(profile.findEntry("RedTRC")?.value)
    }

    @Test
    fun testRejectsSizeMismatch() {

        val exception = assertFailsWith<ImageReadException> {
            IccProfileParser.parse(fixture.bytes.copyOf(fixture.bytes.size - 1))
        }

        assertEquals(
            "The ICC profile declares ${fixture.bytes.size} bytes, " +
                "but ${fixture.bytes.size - 1} are present.",
            exception.message
        )
    }

    @Test
    fun testRejectsMissingFileSignature() {

        val bytes = fixture.bytes

        bytes[36] = 'X'.code.toByte()

        assertFailsWith<ImageReadException> {
            IccProfileParser.parse(bytes)
        }
    }

    @Test
    fun testRejectsTagEntryBeyondTheProfile() {

        val bytes = fixture.bytes

        /* Point the first tag entry's value behind the profile. */
        fixture.writeUInt32(bytes, fixture.firstEntryOffset + 4, fixture.bytes.size + 4)

        assertFailsWith<ImageReadException> {
            IccProfileParser.parse(bytes)
        }
    }

    /**
     * A size field of 0xFFFFFFFF reads as -1 through a signed Int and
     * slips past the bounds check - the entry must fail the read instead
     * of passing as an undecodable block with a NULL value.
     */
    @Test
    fun testRejectsTagEntrySizeWithTheSignBitSet() {

        val bytes = fixture.bytes

        fixture.writeUInt32(bytes, fixture.firstEntryOffset + 8, -1)

        assertFailsWith<ImageReadException> {
            IccProfileParser.parse(bytes)
        }
    }

    /**
     * A record count and size whose product exceeds 32 bits overflows a
     * signed Int multiplication - possibly to zero - so the record table
     * would claim fewer bytes than it does. The read must fail instead.
     */
    @Test
    fun testRejectsMlucRecordTableOverflowingTheValue() {

        val bytes = fixture.bytes

        /* The second entry ("cprt") carries the mluc value. */
        val entryOffset = fixture.firstEntryOffset + IccFixture.TAG_TABLE_ENTRY_SIZE

        val valueOffset = fixture.readUInt32(bytes, entryOffset + IccFixture.FIELD_SIZE)

        /* 65536 records of 65536 bytes each: the product is 2^32. */
        fixture.writeUInt32(bytes, valueOffset + 8, 0x00010000)
        fixture.writeUInt32(bytes, valueOffset + 12, 0x00010000)

        assertFailsWith<ImageReadException> {
            IccProfileParser.parse(bytes)
        }
    }

    /**
     * Both fields are unsigned 32-bit, so their product exceeds the Long
     * range: 0xFFFFFFFF * 0xFFFFFFFF wraps to a negative product that
     * passes any signed comparison. The record table must fail the read.
     */
    @Test
    fun testRejectsMlucRecordTableOverflowingTheLongRange() {

        val bytes = fixture.bytes

        val entryOffset = fixture.firstEntryOffset + IccFixture.TAG_TABLE_ENTRY_SIZE

        val valueOffset = fixture.readUInt32(bytes, entryOffset + IccFixture.FIELD_SIZE)

        fixture.writeUInt32(bytes, valueOffset + 8, -1) /* 0xFFFFFFFF */
        fixture.writeUInt32(bytes, valueOffset + 12, -1) /* 0xFFFFFFFF */

        assertFailsWith<ImageReadException> {
            IccProfileParser.parse(bytes)
        }
    }

    /**
     * The legacy text description's length field is unsigned, too - a
     * negative reading must fail like any other lying length instead of
     * decoding to an empty text.
     */
    @Test
    fun testRejectsTextDescriptionLengthWithTheSignBitSet() {

        val bytes = fixture.bytes

        val valueOffset = fixture.readUInt32(bytes, fixture.firstEntryOffset + IccFixture.FIELD_SIZE)

        /* The "desc" value declares its ASCII length at offset 8. */
        fixture.writeUInt32(bytes, valueOffset + 8, -1)

        assertFailsWith<ImageReadException> {
            IccProfileParser.parse(bytes)
        }
    }

    /**
     * Builds a 4-entry profile: header, tag table, then the values - a
     * legacy "desc" text, an "mluc" text, an "XYZ " number triple and a
     * binary "para" block the parser does not render.
     */
    private class IccFixture {

        val entries = listOf(
            Entry("desc"),
            Entry("cprt"),
            Entry("wtpt"),
            Entry("rTRC")
        )

        val tagTableSize = 4 + entries.size * 12

        val valueAreaOffset = 128 + tagTableSize

        val bytes: ByteArray = run {

            val descText = "Test profile"
            val descValue = ByteArray(12 + descText.length + 1)

            "desc".forEachIndexed { index, char -> descValue[index] = char.code.toByte() }
            writeUInt32(descValue, 8, descText.length + 1)
            descText.forEachIndexed { index, char -> descValue[12 + index] = char.code.toByte() }

            val mlucText = "Öffentlich"

            /* mluc text is UTF-16 big endian. */
            val mlucTextBytes = mlucText.map { char -> char.code }
                .flatMap { code -> listOf((code shr 8).toByte(), code.toByte()) }
                .toByteArray()

            val cprtValue = ByteArray(28 + mlucTextBytes.size)

            "mluc".forEachIndexed { index, char -> cprtValue[index] = char.code.toByte() }
            writeUInt32(cprtValue, 8, 1)
            writeUInt32(cprtValue, 12, 12)
            /* Language "de", country "DE", byte length, offset behind the record. */
            cprtValue[16] = 'd'.code.toByte()
            cprtValue[17] = 'e'.code.toByte()
            cprtValue[18] = 'D'.code.toByte()
            cprtValue[19] = 'E'.code.toByte()
            writeUInt32(cprtValue, 20, mlucTextBytes.size)
            writeUInt32(cprtValue, 24, 28)
            mlucTextBytes.forEachIndexed { index, byte -> cprtValue[28 + index] = byte }

            val wtptValue = ByteArray(20)

            "XYZ ".forEachIndexed { index, char -> wtptValue[index] = char.code.toByte() }
            writeUInt32(wtptValue, 8, 63190)
            writeUInt32(wtptValue, 12, 65536)
            writeUInt32(wtptValue, 16, 54061)

            val rtrcValue = ByteArray(32)

            "para".forEachIndexed { index, char -> rtrcValue[index] = char.code.toByte() }

            val values = descValue + cprtValue + wtptValue + rtrcValue

            val bytes = ByteArray(128 + tagTableSize + values.size)

            writeUInt32(bytes, 0, bytes.size)
            "lcms".forEachIndexed { index, char -> bytes[4 + index] = char.code.toByte() }
            bytes[8] = 2
            bytes[9] = 0x10
            "mntr".forEachIndexed { index, char -> bytes[12 + index] = char.code.toByte() }
            "RGB ".forEachIndexed { index, char -> bytes[16 + index] = char.code.toByte() }
            "XYZ ".forEachIndexed { index, char -> bytes[20 + index] = char.code.toByte() }
            "acsp".forEachIndexed { index, char -> bytes[36 + index] = char.code.toByte() }

            writeUInt32(bytes, 128, entries.size)

            var valueOffset = valueAreaOffset

            entries.forEachIndexed { index, entry ->

                val entryOffset = 132 + index * 12

                entry.signature.forEachIndexed { charIndex, char ->
                    bytes[entryOffset + charIndex] = char.code.toByte()
                }

                val value = when (entry.signature) {
                    "desc" -> descValue
                    "cprt" -> cprtValue
                    "wtpt" -> wtptValue
                    else -> rtrcValue
                }

                writeUInt32(bytes, entryOffset + 4, valueOffset)
                writeUInt32(bytes, entryOffset + 8, value.size)

                value.copyInto(bytes, valueOffset)

                valueOffset += value.size
            }

            bytes
        }

        val firstEntryOffset = 132

        class Entry(val signature: String)

        fun writeUInt32(bytes: ByteArray, offset: Int, value: Int) {

            bytes[offset] = (value ushr 24).toByte()
            bytes[offset + 1] = (value ushr 16).toByte()
            bytes[offset + 2] = (value ushr 8).toByte()
            bytes[offset + 3] = value.toByte()
        }

        fun readUInt32(bytes: ByteArray, offset: Int): Int =

            (bytes[offset].toInt() and 0xFF) shl 24 or
                (bytes[offset + 1].toInt() and 0xFF) shl 16 or
                (bytes[offset + 2].toInt() and 0xFF) shl 8 or
                (bytes[offset + 3].toInt() and 0xFF)

        companion object {

            const val TAG_TABLE_ENTRY_SIZE: Int = 12

            const val FIELD_SIZE: Int = 4
        }
    }

}
