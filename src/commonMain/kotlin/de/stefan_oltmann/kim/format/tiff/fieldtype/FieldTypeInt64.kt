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
package de.stefan_oltmann.kim.format.tiff.fieldtype

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.common.toLongs
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_INT64_INDEX

/**
 * 64-bit (8-byte) unsigned integer, used by Apple MakerNotes.
 */
public data object FieldTypeInt64 : FieldType<LongArray> {

    override val type: Int = FIELD_TYPE_INT64_INDEX

    override val name: String = "Int64"

    override val size: Int = 8

    override fun getValue(bytes: ByteArray, byteOrder: ByteOrder): LongArray =
        bytes.toLongs(byteOrder)

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray =
        when (data) {
            is Long -> data.toBytes(byteOrder)
            is LongArray -> data.toBytes(byteOrder)
            else -> throw ImageWriteException("Unsupported type: $data")
        }
}
