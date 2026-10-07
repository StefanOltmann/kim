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
import de.stefan_oltmann.kim.common.decodeStrictUtf8
import de.stefan_oltmann.kim.common.decodeUtf16BytesToString
import de.stefan_oltmann.kim.common.slice
import de.stefan_oltmann.kim.common.toUInt8
import de.stefan_oltmann.kim.common.tryWithImageReadException
import kotlin.jvm.JvmStatic

/**
 * Parses ICC color profile bytes - the 128-byte profile header followed
 * by the tag table - into an [IccProfile].
 *
 * Every ICC value carries its own 4-character type signature, so the
 * decoder dispatches on the value, not the table entry. Text types
 * ("desc", "mluc", "text") and the number-list types ("XYZ ", "sf32",
 * "sig", "date") are decoded; binary types like curve or matrix
 * parameter blocks keep their type signature with a NULL value, which
 * keeps the entry queryable without fabricating a rendering of binary
 * data.
 */
public object IccProfileParser {

    /** The profile file signature every ICC profile carries at offset 36. */
    private const val FILE_SIGNATURE = "acsp"

    private const val HEADER_SIZE = 128

    private const val TAG_TABLE_ENTRY_SIZE = 12

    private const val TAG_COUNT_OFFSET = HEADER_SIZE

    private const val TAG_TABLE_OFFSET = HEADER_SIZE + 4

    private const val FILE_SIGNATURE_OFFSET = 36

    private const val MAX_TAG_COUNT = 1024

    /** Byte width of the fixed-size string fields of the header and the signatures. */
    private const val FIELD_SIZE = 4

    /** Byte width of the tag count field between header and tag table. */
    private const val TAG_COUNT_SIZE = 4

    private const val CMM_TYPE_OFFSET = 4

    private const val PROFILE_CLASS_OFFSET = 12

    private const val COLOR_SPACE_OFFSET = 16

    private const val CONNECTION_SPACE_OFFSET = 20

    private const val PRIMARY_PLATFORM_OFFSET = 40

    private const val RENDERING_INTENT_OFFSET = 64

    private const val VERSION_MAJOR_OFFSET = 8

    private const val VERSION_MINOR_OFFSET = 9

    /** The version minor and bugfix nibbles are BCD-coded half bytes. */
    private const val BCD_NIBBLE_BITS = 4

    private const val BCD_NIBBLE_MASK = 0x0F

    /** Number values of one XYZ type: the three color columns. */
    private const val XYZ_COMPONENT_COUNT = 3

    /** The s15Fixed16 encoding stores 1.0 as 65536. */
    private const val S15_FIXED16_ONE = 65536.0

    /** Field count of the ICC date-time: year, month, day, hour, minute, second. */
    private const val DATE_TIME_FIELD_COUNT = 6

    private const val BYTE_SHIFT_8 = 8

    private const val BYTE_SHIFT_16 = 16

    private const val BYTE_SHIFT_24 = 24

    /** Masks a 4-byte value to its unsigned 32-bit range. */
    private const val UINT32_MASK = 0xFFFFFFFFL

    /** Header + type signature + reserved + length of the smallest value layout. */
    private const val MIN_VALUE_HEADER_SIZE = 8

    /** Type signature, reserved, record count and record size of an mluc value. */
    private const val MLUC_HEADER_SIZE = 16

    /** Language, country, byte length and offset of one mluc record. */
    private const val MIN_MLUC_RECORD_SIZE = 12

    /** Byte width of the six uint16 fields of an ICC date-time value. */
    private const val DATE_TIME_SIZE = 12

    /**
     * The ICC tag registry names of the signatures found in real
     * profiles, like ExifTool reports them. Signatures without an entry
     * stay queryable under their signature.
     */
    private val TAG_NAMES: Map<String, String> = mapOf(
        "desc" to "ProfileDescription",
        "cprt" to "ProfileCopyright",
        "dmnd" to "DeviceMfgDesc",
        "dmdd" to "DeviceModelDesc",
        "wtpt" to "MediaWhitePoint",
        "bkpt" to "MediaBlackPoint",
        "lumi" to "Luminance",
        "chad" to "ChromaticAdaptation",
        "meas" to "Measurement",
        "chrm" to "Chromaticity",
        "rXYZ" to "RedMatrixColumn",
        "gXYZ" to "GreenMatrixColumn",
        "bXYZ" to "BlueMatrixColumn",
        "rTRC" to "RedTRC",
        "gTRC" to "GreenTRC",
        "bTRC" to "BlueTRC",
        "kTRC" to "GrayTRC",
        "rICC" to "RedColorant",
        "gICC" to "GreenColorant",
        "bICC" to "BlueColorant",
        "tech" to "Technology",
        "view" to "ViewingConditions",
        "vued" to "ViewingCondDesc",
        "gamt" to "Gamut",
        "A2B0" to "AToB0",
        "A2B1" to "AToB1",
        "A2B2" to "AToB2",
        "B2A0" to "BToA0",
        "B2A1" to "BToA1",
        "B2A2" to "BToA2"
    )

    @JvmStatic
    public fun parse(bytes: ByteArray): IccProfile =

        tryWithImageReadException {

            if (bytes.size < HEADER_SIZE + TAG_COUNT_SIZE)
                throw ImageReadException(
                    "The ICC profile is ${bytes.size} bytes, smaller than its header."
                )

            val declaredSize = readUInt32(bytes, 0)

            if (declaredSize != bytes.size)
                throw ImageReadException(
                    "The ICC profile declares $declaredSize bytes, " +
                        "but ${bytes.size} are present."
                )

            if (stringAt(bytes, FILE_SIGNATURE_OFFSET, FIELD_SIZE) != FILE_SIGNATURE)
                throw ImageReadException(
                    "The ICC profile lacks the '$FILE_SIGNATURE' file signature."
                )

            val tagCount = readUInt32AsLong(bytes, TAG_COUNT_OFFSET)

            if (tagCount > MAX_TAG_COUNT ||
                TAG_TABLE_OFFSET + tagCount * TAG_TABLE_ENTRY_SIZE > bytes.size
            )
                throw ImageReadException(
                    "The ICC profile declares an invalid tag count of $tagCount."
                )

            IccProfile(
                size = bytes.size,
                cmmType = stringAt(bytes, CMM_TYPE_OFFSET, FIELD_SIZE),
                version = renderVersion(bytes),
                profileClass = stringAt(bytes, PROFILE_CLASS_OFFSET, FIELD_SIZE),
                colorSpace = stringAt(bytes, COLOR_SPACE_OFFSET, FIELD_SIZE),
                connectionSpace = stringAt(bytes, CONNECTION_SPACE_OFFSET, FIELD_SIZE),
                primaryPlatform = readOptionalString(bytes, PRIMARY_PLATFORM_OFFSET),
                renderingIntent = readUInt32(bytes, RENDERING_INTENT_OFFSET),
                entries = (0 until tagCount.toInt())
                    .map { index -> readTagTableEntry(bytes, TAG_TABLE_OFFSET + index * TAG_TABLE_ENTRY_SIZE) }
            )
        }

    private fun readTagTableEntry(bytes: ByteArray, entryOffset: Int): IccEntry {

        val signature = stringAt(bytes, entryOffset, FIELD_SIZE)

        /*
         * The offset and size fields are unsigned 32-bit, so both are
         * read as Long: through a signed Int a size of 0xFFFFFFFF reads
         * as -1 and slips past every bounds check.
         */
        val valueOffset = readUInt32AsLong(bytes, entryOffset + FIELD_SIZE)

        val valueSize = readUInt32AsLong(bytes, entryOffset + 2 * FIELD_SIZE)

        if (valueOffset < HEADER_SIZE || valueOffset + valueSize > bytes.size)
            throw ImageReadException(
                "The ICC tag '$signature' points $valueOffset+$valueSize bytes, " +
                    "which lies outside the ${bytes.size}-byte profile."
            )

        val valueBytes = bytes.slice(valueOffset.toInt(), valueSize.toInt())

        val decoded = decodeValue(valueBytes)

        return IccEntry(
            signature = signature,
            name = TAG_NAMES[signature],
            textValue = decoded.text,
            numericComponents = decoded.numbers
        )
    }

    /**
     * The decoded parts of a tag value: exactly one of both is present,
     * or neither for an undecoded binary type.
     */
    private class DecodedValue(
        val text: String?,
        val numbers: DoubleArray?
    )

    /**
     * Decodes a tag value by its own 4-character type signature. Both
     * parts NULL means the type is a binary block this parser does not
     * render.
     */
    private fun decodeValue(valueBytes: ByteArray): DecodedValue {

        if (valueBytes.size < MIN_VALUE_HEADER_SIZE)
            return DecodedValue(text = null, numbers = null)

        return when (stringAt(valueBytes, 0, FIELD_SIZE)) {

            "desc" -> DecodedValue(decodeTextDescription(valueBytes), null)

            "mluc" -> DecodedValue(decodeMultiLanguageUnicode(valueBytes), null)

            "text" -> DecodedValue(
                valueBytes.slice(MIN_VALUE_HEADER_SIZE, valueBytes.size - MIN_VALUE_HEADER_SIZE)
                    .decodeStrictUtf8("The ICC text value")
                    .trimEnd('\u0000'),
                null
            )

            "XYZ " -> DecodedValue(
                null,
                readS15Fixed16Components(valueBytes, MIN_VALUE_HEADER_SIZE, XYZ_COMPONENT_COUNT)
            )

            "sf32" -> DecodedValue(
                null,
                readS15Fixed16Components(
                    valueBytes,
                    MIN_VALUE_HEADER_SIZE,
                    (valueBytes.size - MIN_VALUE_HEADER_SIZE) / FIELD_SIZE
                )
            )

            "sig" -> DecodedValue(stringAt(valueBytes, MIN_VALUE_HEADER_SIZE, FIELD_SIZE), null)

            "date" -> DecodedValue(decodeDateTime(valueBytes), null)

            /* An undecoded binary type: no fabricated rendering. */
            else -> DecodedValue(text = null, numbers = null)
        }
    }

    /**
     * The legacy text description: signature, reserved, ASCII length and
     * the ASCII text.
     */
    private fun decodeTextDescription(valueBytes: ByteArray): String? {

        if (valueBytes.size < MIN_VALUE_HEADER_SIZE + FIELD_SIZE)
            return null

        val asciiLength = readUInt32AsLong(valueBytes, MIN_VALUE_HEADER_SIZE)

        if (asciiLength > valueBytes.size - MIN_VALUE_HEADER_SIZE - FIELD_SIZE)
            throw ImageReadException(
                "The ICC text description declares $asciiLength bytes, " +
                    "but only ${valueBytes.size - MIN_VALUE_HEADER_SIZE - FIELD_SIZE} remain."
            )

        return valueBytes.slice(MIN_VALUE_HEADER_SIZE + FIELD_SIZE, asciiLength.toInt())
            .decodeStrictUtf8("The ICC text description")
            .trimEnd('\u0000')
    }

    /**
     * The multi-language unicode text: record count, record size and per
     * record the language, country, byte length and offset. The first
     * record carries the text.
     */
    private fun decodeMultiLanguageUnicode(valueBytes: ByteArray): String? {

        if (valueBytes.size < MLUC_HEADER_SIZE)
            return null

        /*
         * Record count, record size, text length and text offset are
         * unsigned 32-bit, so all are read as Long: a signed Int product
         * like 65536 * 65536 overflows and would pass the bounds check.
         * The comparison divides instead of multiplying - even the Long
         * product of two maximal 32-bit values would wrap around.
         */
        val recordCount = readUInt32AsLong(valueBytes, MIN_VALUE_HEADER_SIZE)

        if (recordCount == 0L)
            return ""

        val recordSize = readUInt32AsLong(valueBytes, 3 * FIELD_SIZE)

        if (recordSize < MIN_MLUC_RECORD_SIZE ||
            recordCount > (valueBytes.size - MLUC_HEADER_SIZE) / recordSize
        )
            throw ImageReadException(
                "The ICC unicode text declares $recordCount records of " +
                    "$recordSize bytes, which exceeds the value."
            )

        val textLength = readUInt32AsLong(valueBytes, MLUC_HEADER_SIZE + FIELD_SIZE)
        val textOffset = readUInt32AsLong(valueBytes, MLUC_HEADER_SIZE + 2 * FIELD_SIZE)

        if (textOffset > valueBytes.size - textLength)
            throw ImageReadException(
                "The ICC unicode text points $textOffset+$textLength bytes, " +
                    "which lies outside the value."
            )

        /* The ICC specification fixes mluc text as UTF-16 big endian. */
        return valueBytes.slice(textOffset.toInt(), textLength.toInt())
            .decodeUtf16BytesToString(littleEndian = false)
            .trimEnd('\u0000')
    }

    /**
     * Reads the fixed-point components. The per-component string
     * rendering happens later, when the entry value is read - parsing
     * only validates and converts, which keeps the format cost out of
     * the read path for profiles whose numbers are never displayed.
     */
    private fun readS15Fixed16Components(
        valueBytes: ByteArray,
        firstValueOffset: Int,
        count: Int
    ): DoubleArray {

        if (firstValueOffset + count * FIELD_SIZE > valueBytes.size)
            throw ImageReadException("The ICC number list is truncated.")

        return DoubleArray(count) { index ->
            readS15Fixed16(valueBytes, firstValueOffset + index * FIELD_SIZE)
        }
    }

    private fun readS15Fixed16(bytes: ByteArray, offset: Int): Double {

        val raw = readInt32(bytes, offset)

        return raw / S15_FIXED16_ONE
    }

    /** The 6 uint16 fields of the ICC date-time render as ISO-8601. */
    private fun decodeDateTime(valueBytes: ByteArray): String {

        if (valueBytes.size < MIN_VALUE_HEADER_SIZE + DATE_TIME_SIZE)
            throw ImageReadException("The ICC date value is truncated.")

        val fields = (0 until DATE_TIME_FIELD_COUNT)
            .map { index -> readUInt16(valueBytes, MIN_VALUE_HEADER_SIZE + index * 2) }

        val paddedYear = fields[0].toString().padStart(4, '0')
        val paddedMonth = fields[1].toString().padStart(2, '0')
        val paddedDay = fields[2].toString().padStart(2, '0')
        val paddedHour = fields[3].toString().padStart(2, '0')
        val paddedMinute = fields[4].toString().padStart(2, '0')
        val paddedSecond = fields[5].toString().padStart(2, '0')

        return "$paddedYear-$paddedMonth-$paddedDay " +
            "$paddedHour:$paddedMinute:$paddedSecond"
    }

    /**
     * The ICC version byte triple is BCD-coded: 0x04 0x30 renders as
     * "4.3". The trailing zero minor byte is omitted when zero.
     */
    private fun renderVersion(bytes: ByteArray): String {

        val major = bytes[VERSION_MAJOR_OFFSET].toUInt8()

        val versionByte = bytes[VERSION_MINOR_OFFSET].toUInt8()

        val minor = versionByte shr BCD_NIBBLE_BITS

        val bugfix = versionByte and BCD_NIBBLE_MASK

        return if (bugfix == 0) "$major.$minor" else "$major.$minor.$bugfix"
    }

    /** 4 printable characters, or NULL when all bytes are zero. */
    private fun readOptionalString(bytes: ByteArray, offset: Int): String? {

        if (bytes.slice(offset, FIELD_SIZE).all { it.toInt() == 0 })
            return null

        return stringAt(bytes, offset, FIELD_SIZE)
    }

    private fun stringAt(bytes: ByteArray, offset: Int, count: Int): String =
        bytes.slice(offset, count).decodeStrictUtf8("The ICC signature")

    private fun readUInt16(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toUInt8() shl BYTE_SHIFT_8) or bytes[offset + 1].toUInt8())

    /** Masks a 4-byte value to its unsigned 32-bit range. */
    private fun readUInt32(bytes: ByteArray, offset: Int): Int =
        readInt32(bytes, offset).toLong().and(UINT32_MASK).toInt()

    /** Masks a 4-byte value to its unsigned 32-bit range as a Long. */
    private fun readUInt32AsLong(bytes: ByteArray, offset: Int): Long =
        readInt32(bytes, offset).toLong().and(UINT32_MASK)

    @Suppress("MagicNumber")
    private fun readInt32(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() shl BYTE_SHIFT_24) or
            (bytes[offset + 1].toUInt8() shl BYTE_SHIFT_16) or
            (bytes[offset + 2].toUInt8() shl BYTE_SHIFT_8) or
            bytes[offset + 3].toUInt8()

}
