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
package de.stefan_oltmann.kim.format.tiff.fieldtype

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_ASCII_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_BYTE_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_DOUBLE_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_FLOAT_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_IFD_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_INT64_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_LONG_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_RATIONAL_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_SBYTE_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_SHORT_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_SLONG_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_SRATIONAL_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_SSHORT_INDEX
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_UNDEFINED_INDEX

/**
 * The type of a TIFF field value.
 */
public interface FieldType<T> {

    public val type: Int

    public val name: String

    public val size: Int

    public fun getValue(bytes: ByteArray, byteOrder: ByteOrder): T

    public fun writeData(data: Any, byteOrder: ByteOrder): ByteArray

    public companion object {

        @kotlin.jvm.JvmStatic
        public fun getFieldType(type: Int): FieldType<out Any> =
            when (type) {
                FIELD_TYPE_BYTE_INDEX -> FieldTypeByte
                FIELD_TYPE_ASCII_INDEX -> FieldTypeAscii
                FIELD_TYPE_SHORT_INDEX -> FieldTypeShort
                FIELD_TYPE_LONG_INDEX -> FieldTypeLong
                FIELD_TYPE_RATIONAL_INDEX -> FieldTypeRational
                FIELD_TYPE_SBYTE_INDEX -> FieldTypeSByte
                FIELD_TYPE_UNDEFINED_INDEX -> FieldTypeUndefined
                FIELD_TYPE_SSHORT_INDEX -> FieldTypeSShort
                FIELD_TYPE_SLONG_INDEX -> FieldTypeSLong
                FIELD_TYPE_SRATIONAL_INDEX -> FieldTypeSRational
                FIELD_TYPE_FLOAT_INDEX -> FieldTypeFloat
                FIELD_TYPE_DOUBLE_INDEX -> FieldTypeDouble
                FIELD_TYPE_IFD_INDEX -> FieldTypeIfd
                FIELD_TYPE_INT64_INDEX -> FieldTypeInt64
                else -> throw ImageReadException("Unknown field type $type")
            }
    }
}
