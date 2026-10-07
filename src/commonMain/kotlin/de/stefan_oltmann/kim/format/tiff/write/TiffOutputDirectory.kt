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
package de.stefan_oltmann.kim.format.tiff.write

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.RationalNumber
import de.stefan_oltmann.kim.common.RationalNumbers
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.format.tiff.TiffDirectory.Companion.description
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_DIRECTORY_FOOTER_LENGTH
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_DIRECTORY_HEADER_LENGTH
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_ENTRY_LENGTH
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_ENTRY_MAX_VALUE_LENGTH
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeByte
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeDouble
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeFloat
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeRational
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeSByte
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeSLong
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeSRational
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeSShort
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoDouble
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoDoubles
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoFloat
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoFloats
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoGpsText
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoRational
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSByte
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSBytes
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSLongs
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRational
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSRationals
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoSShorts
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShort
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoShorts
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputItem.Companion.UNDEFINED_VALUE
import de.stefan_oltmann.kim.output.BinaryByteWriter

/**
 * A TIFF directory to be written.
 */
@Suppress("TooManyFunctions", "MethodOverloading")
public class TiffOutputDirectory(
    public val type: Int,
    private val byteOrder: ByteOrder
) : TiffOutputItem {

    private val fields = mutableSetOf<TiffOutputField>()

    private var nextDirectory: TiffOutputDirectory? = null

    override var offset: Int = UNDEFINED_VALUE

    public var thumbnailBytes: ByteArray? = null
        private set

    public var tiffImageBytes: ByteArray? = null
        private set

    internal fun setNextDirectory(nextDirectory: TiffOutputDirectory?) {
        this.nextDirectory = nextDirectory
    }

    private fun checkMatchingLength(tagInfo: TagInfo, length: Int) {

        if (tagInfo.length > 0 && tagInfo.length != length)
            throw ImageWriteException("Tag length is ${tagInfo.length}, parameter length was $length")
    }

    public fun add(tagInfo: TagInfoByte, value: Byte) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeByte,
                count = 1,
                bytes = byteArrayOf(value)
            )
        )
    }

    public fun add(tagInfo: TagInfoBytes, bytes: ByteArray) {

        checkMatchingLength(tagInfo, bytes.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeByte,
                count = bytes.size,
                bytes = bytes
            )
        )
    }

    public fun add(tagInfo: TagInfoAscii, value: String) {

        val bytes = FieldTypeAscii.writeData(value, byteOrder)

        checkMatchingLength(tagInfo, bytes.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeAscii,
                count = bytes.size,
                bytes = bytes
            )
        )
    }

    public fun add(tagInfo: TagInfoShort, value: Short) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeShort,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoShorts, values: ShortArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeShort,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoLong, value: Int) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeLong,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoLongs, values: IntArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeLong,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoRational, value: RationalNumber) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeRational,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoRationals, values: RationalNumbers) {

        checkMatchingLength(tagInfo, values.values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeRational,
                count = values.values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSByte, value: Byte) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSByte,
                count = 1,
                bytes = byteArrayOf(value)
            )
        )
    }

    public fun add(tagInfo: TagInfoSBytes, value: ByteArray) {

        checkMatchingLength(tagInfo, value.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSByte,
                count = value.size,
                bytes = value
            )
        )
    }

    public fun add(tagInfo: TagInfoSShort, value: Short) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSShort,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSShorts, values: ShortArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSShort,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSLong, value: Int) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSLong,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSLongs, values: IntArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSLong,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSRational, value: RationalNumber) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSRational,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSRationals, value: RationalNumbers) {

        checkMatchingLength(tagInfo, value.values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSRational,
                count = value.values.size,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoFloat, value: Float) {

        val bytes = value.toBytes(byteOrder)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeFloat,
                count = 1,
                bytes = bytes
            )
        )
    }

    public fun add(tagInfo: TagInfoFloats, values: FloatArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeFloat,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoDouble, value: Double) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeDouble,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoDoubles, values: DoubleArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeDouble,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoGpsText, value: String) {

        val bytes = tagInfo.encodeValue(value)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = tagInfo.fieldType,
                count = bytes.size,
                bytes = bytes
            )
        )
    }

    public fun add(field: TiffOutputField): Boolean =
        fields.add(field)

    /**
     * Returns a snapshot of the fields of this directory, so callers can
     * inspect them without holding the live set the writer mutates.
     */
    public fun getFields(): Set<TiffOutputField> =
        fields.toSet()

    public fun removeField(tagInfo: TagInfo): Boolean =
        removeField(tagInfo.tag)

    public fun removeField(tag: Int): Boolean =
        fields.removeAll { it.tag == tag }

    public fun findField(tagInfo: TagInfo): TiffOutputField? =
        findField(tagInfo.tag)

    /** Returns the field with the given tag id, or NULL when it is absent. */
    public fun findField(tag: Int): TiffOutputField? =
        fields.find { it.tag == tag }

    override fun writeItem(
        binaryByteWriter: BinaryByteWriter
    ) {

        /*
         * The classic TIFF format declares the entry count in 16 bits.
         * Writing the low bits only would serialize all entries behind
         * a wrapped count - every consumer would then read the entries
         * as the next-IFD pointer, so the write fails instead of
         * emitting a corrupt file.
         */
        if (fields.size > MAX_DIRECTORY_ENTRY_COUNT)
            throw ImageWriteException(
                "Directory $type has ${fields.size} fields; a classic " +
                    "TIFF directory holds at most $MAX_DIRECTORY_ENTRY_COUNT."
            )

        /* Write directory field count. */
        binaryByteWriter.write2Bytes(fields.size)

        for (field in fields.sorted())
            field.writeField(binaryByteWriter)

        var nextDirectoryOffset = 0

        nextDirectory?.let {
            nextDirectoryOffset = it.offset
        }

        if (nextDirectoryOffset == UNDEFINED_VALUE)
            binaryByteWriter.write4Bytes(0)
        else
            binaryByteWriter.write4Bytes(nextDirectoryOffset)
    }

    /* Internal, because callers should use setThumbnailBytes() */
    internal fun setThumbnailBytes(thumbnailBytes: ByteArray?) {
        this.thumbnailBytes = thumbnailBytes
    }

    internal fun setTiffImageBytes(tiffImageBytes: ByteArray?) {
        this.tiffImageBytes = tiffImageBytes
    }

    override fun getItemLength(): Int =
        TIFF_ENTRY_LENGTH * fields.size + TIFF_DIRECTORY_HEADER_LENGTH + TIFF_DIRECTORY_FOOTER_LENGTH

    /**
     * Adds the field pair that points at a separate byte block: an
     * offset field with a placeholder value that the writer fills in
     * once the position of the data is known, and the length field with
     * the block size.
     *
     * Returns the offset field, so the caller can register the block
     * with it later.
     */
    private fun addOffsetAndLengthFields(
        offsetTag: Int,
        lengthTag: Int,
        byteCount: Int,
        byteOrder: ByteOrder
    ): TiffOutputField {

        val offsetField = TiffOutputField(
            offsetTag,
            FieldTypeLong, 1,
            ByteArray(TIFF_ENTRY_MAX_VALUE_LENGTH)
        )

        add(offsetField)

        val lengthValue = FieldTypeLong.writeData(
            byteCount,
            byteOrder
        )

        add(
            TiffOutputField(
                lengthTag,
                FieldTypeLong, 1, lengthValue
            )
        )

        return offsetField
    }

    internal fun getOutputItems(tiffOffsetItems: TiffOffsetItems): List<TiffOutputItem> {

        /* First remove old fields */
        removeField(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT)
        removeField(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)

        var thumbnailOffsetField: TiffOutputField? = null

        val thumbnailBytes = this.thumbnailBytes

        if (thumbnailBytes != null) {

            thumbnailOffsetField = addOffsetAndLengthFields(
                offsetTag = TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT.tag,
                lengthTag = TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH.tag,
                byteCount = thumbnailBytes.size,
                byteOrder = tiffOffsetItems.byteOrder
            )
        }

        var stripOffsetField: TiffOutputField? = null

        val tiffImageBytes = this.tiffImageBytes

        if (tiffImageBytes != null) {

            removeField(TiffTag.TIFF_TAG_STRIP_OFFSETS)
            removeField(TiffTag.TIFF_TAG_ROWS_PER_STRIP)
            removeField(TiffTag.TIFF_TAG_STRIP_BYTE_COUNTS)

            stripOffsetField = addOffsetAndLengthFields(
                offsetTag = TiffTag.TIFF_TAG_STRIP_OFFSETS.tag,
                lengthTag = TiffTag.TIFF_TAG_STRIP_BYTE_COUNTS.tag,
                byteCount = tiffImageBytes.size,
                byteOrder = tiffOffsetItems.byteOrder
            )

            /* Set to MAX value. We combine all strips into one block. */
            add(
                TiffOutputField(
                    TiffTag.TIFF_TAG_ROWS_PER_STRIP.tag,
                    FieldTypeLong, 1,
                    FieldTypeLong.writeData(
                        Int.MAX_VALUE,
                        tiffOffsetItems.byteOrder
                    )
                )
            )
        }

        removeField(TiffTag.TIFF_TAG_TILE_OFFSETS)
        removeField(TiffTag.TIFF_TAG_TILE_BYTE_COUNTS)
        removeField(TiffTag.TIFF_TAG_TILE_WIDTH)
        removeField(TiffTag.TIFF_TAG_TILE_LENGTH)

        val result = mutableListOf<TiffOutputItem>()

        result.add(this)

        for (field in fields.sorted()) {

            if (field.isLocalValue)
                continue

            /*
             * Fields with a separate value always have one once they are
             * written, so a missing value is an internal error.
             */
            val item = checkNotNull(field.separateValue)

            result.add(item)
        }

        if (thumbnailBytes != null) {

            val item: TiffOutputItem = TiffOutputValue(
                "thumbnailImageDataElement",
                thumbnailBytes
            )

            result.add(item)

            tiffOffsetItems.addOffsetItem(
                TiffOffsetItem(item, checkNotNull(thumbnailOffsetField))
            )
        }

        if (tiffImageBytes != null) {

            val item: TiffOutputItem = TiffOutputValue(
                "tiffImageDataElement",
                tiffImageBytes
            )

            result.add(item)

            tiffOffsetItems.addOffsetItem(
                TiffOffsetItem(item, checkNotNull(stripOffsetField))
            )
        }

        return result
    }

    override fun toString(): String =
        description(type)

    private companion object {

        /* The entry count field of the classic TIFF directory is 16 bits wide. */
        private const val MAX_DIRECTORY_ENTRY_COUNT: Int = 0xFFFF
    }
}
