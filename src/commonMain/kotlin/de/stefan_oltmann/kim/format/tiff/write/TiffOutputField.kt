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
import de.stefan_oltmann.kim.common.HEX_RADIX
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_ENTRY_MAX_VALUE_LENGTH
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldType
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.output.BinaryByteWriter

/**
 * A TIFF field to be written.
 */
public class TiffOutputField(
    public val tag: Int,
    public val fieldType: FieldType<out Any>,
    public val count: Int,
    private var bytes: ByteArray
) : Comparable<TiffOutputField> {

    public val tagFormatted: String =
        "0x" + tag.toString(HEX_RADIX).padStart(4, '0')

    public val isLocalValue: Boolean = bytes.size <= TIFF_ENTRY_MAX_VALUE_LENGTH

    public val separateValue: TiffOutputValue? =
        if (isLocalValue) null else TiffOutputValue("Value of $this", bytes)

    /**
     * The offset of this field's value in the original EXIF block, or
     * NULL when the field was added by an update.
     *
     * The writer keeps the MakerNote value at this offset, because some
     * MakerNotes store absolute offsets inside their data that would
     * break when the MakerNote is moved.
     */
    internal var originalOffset: Int? = null

    public var sortHint: Int = -1

    internal fun writeField(byteWriter: BinaryByteWriter) {

        byteWriter.write2Bytes(tag)
        byteWriter.write2Bytes(fieldType.type)
        byteWriter.write4Bytes(count)

        if (isLocalValue) {

            if (separateValue != null)
                throw ImageWriteException("Unexpected separate value item.")

            if (bytes.size > TIFF_ENTRY_MAX_VALUE_LENGTH)
                throw ImageWriteException("Local value has invalid length: " + bytes.size)

            byteWriter.write(bytes)

            /* Fill the empty space with zeros */
            repeat(TIFF_ENTRY_MAX_VALUE_LENGTH - bytes.size) {
                byteWriter.write(0)
            }

        } else {

            if (separateValue == null)
                throw ImageWriteException("Missing separate value item.")

            byteWriter.write4Bytes(separateValue.offset)
        }
    }

    internal fun bytesAsHex(): String =
        bytes.toHex()

    internal fun bytesEqual(data: ByteArray): Boolean =
        bytes.contentEquals(data)

    internal fun setBytes(bytes: ByteArray) {

        if (this.bytes.size != bytes.size)
            throw ImageWriteException("Cannot change size of value.")

        this.bytes = bytes

        separateValue?.updateValue(bytes)
    }

    override fun toString(): String =
        "TiffOutputField $tagFormatted"

    override fun compareTo(other: TiffOutputField): Int {

        if (tag != other.tag)
            return tag - other.tag

        return sortHint - other.sortHint
    }

    internal companion object {

        internal fun createOffsetField(tagInfo: TagInfo, byteOrder: ByteOrder): TiffOutputField =
            TiffOutputField(tagInfo.tag, FieldTypeLong, 1, FieldTypeLong.writeData(0, byteOrder))
    }
}
