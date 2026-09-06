/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
import de.stefan_oltmann.kim.common.HEX_RADIX
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.RationalNumber
import de.stefan_oltmann.kim.common.RationalNumbers
import de.stefan_oltmann.kim.common.toInvariantString
import de.stefan_oltmann.kim.common.toSingleNumberHexes
import de.stefan_oltmann.kim.format.tiff.TiffTags.getTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldType
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeSShort
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeShort
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeSByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoGpsText

/**
 * A TIFF field in a TIFF directory.
 */
public class TiffField(
    /** Offset relative to TIFF header. */
    public val offset: Int,
    public val tag: Int,
    public val directoryType: Int,
    public val fieldType: FieldType<out Any>,
    public val count: Int,
    /** Set if field has a local value. */
    public val localValue: Int?,
    /**
     * The absolute offset of the value within the TIFF bytes, resolved
     * against the start of the read data, or null when the value is
     * stored locally in the entry itself.
     */
    public val valueOffset: Int?,
    public val valueBytes: ByteArray,
    public val byteOrder: ByteOrder,
    public val sortHint: Int,
    /**
     * The TagInfo that belongs to this field, when the parsing context
     * already resolved it, for example a model-specific MakerNote blob
     * table. The registry lookup would be ambiguous when several tables
     * share the directory type but use the same tag for different names.
     */
    public val tagInfoOverride: TagInfo? = null
) {

    /**
     * Returns the offset with padding.
     * Because TIFF files can be as big as 4 GB we need 10 digits to present that.
     */
    public val offsetFormatted: String =
        offset.toString().padStart(10, '0')

    /** Return a proper Tag ID like 0x0100. */
    public val tagFormatted: String =
        "0x" + tag.toString(HEX_RADIX).padStart(4, '0')

    /** TagInfo, if the tag is found in our registry. */
    public val tagInfo: TagInfo? = tagInfoOverride ?: getTag(directoryType, tag)

    public val value: Any = if (tagInfo is TagInfoGpsText)

        /*
         * A single hostile GPS text field (e.g. a UserComment with a LONG
         * type) must not fail the whole directory. Like every other
         * per-entry corruption it is skipped by falling back to the
         * generic type decode.
         */
        try {
            tagInfo.getValue(this)
        } catch (_: ImageReadException) {
            fieldType.getValue(this.valueBytes, this.byteOrder)
        }
    else
        fieldType.getValue(this.valueBytes, this.byteOrder)

    public val valueDescription: String by lazy {
        try {

            val maskedValue = tagInfo?.mask?.let { mask ->
                when {
                    value is Number -> (value.toInt() and mask) ushr mask.countTrailingZeroBits()
                    value is ByteArray && value.size == 1 ->
                        (value.first().toInt() and mask) ushr mask.countTrailingZeroBits()

                    value is ShortArray && value.size == 1 ->
                        (value.first().toInt() and mask) ushr mask.countTrailingZeroBits()

                    value is IntArray && value.size == 1 ->
                        (value.first() and mask) ushr mask.countTrailingZeroBits()

                    else -> null
                }
            }

            if (maskedValue != null)
                return@lazy maskedValue.toString()

            if (value is ByteArray) {

                if (value.size == 1)
                    return@lazy value.first().toString()

                if (value.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy "[${value.toSingleNumberHexes()}]"

                return@lazy "[${value.size} bytes]"
            }

            if (value is IntArray) {

                if (value.size == 1)
                    return@lazy value.first().toString()

                if (value.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy value.contentToString()

                return@lazy "[${value.size} ints]"
            }

            if (value is ShortArray) {

                if (value.size == 1)
                    return@lazy value.first().toString()

                if (value.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy value.contentToString()

                return@lazy "[${value.size} shorts]"
            }

            if (value is DoubleArray) {

                if (value.size == 1)
                    return@lazy value.first().toInvariantString()

                /*
                 * Rendered per element instead of contentToString, because
                 * the platform double renderings differ (see
                 * [toInvariantString]) and text dumps must be
                 * byte-identical on every platform.
                 */
                if (value.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy value.joinToString(
                        separator = ", ",
                        prefix = "[",
                        postfix = "]",
                        transform = Double::toInvariantString
                    )

                return@lazy "[${value.size} doubles]"
            }

            if (value is FloatArray) {

                if (value.size == 1)
                    return@lazy value.first().toInvariantString()

                if (value.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy value.joinToString(
                        separator = ", ",
                        prefix = "[",
                        postfix = "]",
                        transform = Float::toInvariantString
                    )

                return@lazy "[${value.size} floats]"
            }

            if (value is LongArray) {

                if (value.size == 1)
                    return@lazy value.first().toString()

                if (value.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy value.contentToString()

                return@lazy "[${value.size} longs]"
            }

            if (value is RationalNumbers) {

                if (value.values.size == 1)
                    return@lazy value.values.first().toString()

                if (value.values.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy value.values.contentToString()

                return@lazy "[${value.values.size} rationals]"
            }

            value.toString()

        } catch (ex: ImageReadException) {
            "Invalid value: " + ex.message
        }
    }

    public fun toStringValue(): String {

        if (value is List<*>) {

            /*
             * If the field is all NULLs, this wil result in an empty list.
             */
            val firstValue = value.firstOrNull() ?: return ""

            return firstValue.toString()
        }

        if (value !is String)
            throw ImageReadException("Expected String for $tagFormatted, but got: $value")

        return value
    }

    public fun toIntArray(): IntArray {

        if (value is Number)
            return intArrayOf(value.toInt())

        if (value is IntArray)
            return value

        if (value is ShortArray) {

            val result = IntArray(value.size)

            repeat(result.size) { index ->
                result[index] = if (fieldType == FieldTypeSShort)
                    value[index].toInt()
                else
                    value[index].toUShort().toInt()
            }

            return result
        }

        throw ImageReadException("Can't format value of tag $tagFormatted as int: $value")
    }

    /**
     * Returns the first value as Int, or NULL when the field has no values
     * (count == 0) or a type that cannot be converted.
     *
     * Hostile files can legally carry zero-count fields, so callers skip
     * the NULL case instead of failing the whole parse.
     */
    public fun toInt(): Int? = when (value) {
        is ByteArray -> value.firstOrNull()?.toIntByFieldType()
        is ShortArray -> value.firstOrNull()?.toIntByFieldType()
        is IntArray -> value.firstOrNull()
        else -> (value as? Number)?.toInt()
    }

    /**
     * Returns the first value as Short, or NULL when the field has no
     * values (count == 0) or a type that cannot be converted.
     */
    public fun toShort(): Short? = when (value) {
        is ByteArray -> value.firstOrNull()?.toShort()
        is ShortArray -> value.firstOrNull()
        is IntArray -> value.firstOrNull()?.toShort()
        else -> (value as? Number)?.toShort()
    }

    /**
     * Returns the first value as Double, or NULL when the field has no
     * values (count == 0) or a type that cannot be converted.
     */
    public fun toDouble(): Double? = when (value) {
        is RationalNumbers -> value.values.firstOrNull()?.doubleValue()
        is RationalNumber -> value.doubleValue()
        is ByteArray -> value.firstOrNull()?.toDouble()
        is ShortArray -> value.firstOrNull()?.toIntByFieldType()?.toDouble()
        is IntArray -> value.firstOrNull()?.toDouble()
        is FloatArray -> value.firstOrNull()?.toDouble()
        is DoubleArray -> value.firstOrNull()
        else -> (value as? Number)?.toDouble()
    }

    /**
     * Interprets this [Short] according to the field type of this [TiffField].
     *
     * The TIFF SHORT type is unsigned, but Short cannot represent values
     * above 32767. Widening such values with the signed conversion would
     * report e.g. an ISO of 51200 as -14336.
     */
    private fun Short.toIntByFieldType(): Int =
        if (fieldType === FieldTypeShort)
            toUShort().toInt()
        else
            toInt()

    /**
     * Interprets this [Byte] according to the field type of this [TiffField].
     *
     * The TIFF BYTE type is unsigned, so e.g. a BYTE-typed offset of 0x90
     * must widen to 0x90 and not to the signed -112. Only the SBYTE type
     * is signed by definition.
     */
    private fun Byte.toIntByFieldType(): Int =
        if (fieldType === FieldTypeSByte)
            toInt()
        else
            toUByte().toInt()

    /*
     * Note that we need to show the local 'tagFormatted', because
     * 'tagInfo' might be an Unknown tag and show a placeholder.
     */
    override fun toString(): String =
        "$offsetFormatted $tagFormatted ${tagInfo?.name ?: "Unknown"} = $valueDescription"

    internal fun createOversizeValueElement(): TiffElement? =
        valueOffset?.let { OversizeValueElement(it, valueBytes.size) }

    internal inner class OversizeValueElement(offset: Int, length: Int) : TiffElement(
        debugDescription = "Value of $tagInfo ($fieldType) @ $offset",
        offset = offset,
        length = length
    ) {

        override fun toString(): String =
            debugDescription
    }

    private companion object {

        /**
         * Limit to 16 bytes, so that a GeoTiff ModelTransformationTag
         * is still displayed in full, but not values greater than that.
         */
        private const val MAX_ARRAY_LENGTH_DISPLAY_SIZE = 16
    }
}
