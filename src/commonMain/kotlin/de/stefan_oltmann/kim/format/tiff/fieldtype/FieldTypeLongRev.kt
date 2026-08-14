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
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.common.toInts
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_LONG_INDEX

/**
 * 32-bit (4-byte) unsigned integer stored in the reversed byte order.
 */
public data object FieldTypeLongRev : FieldType<IntArray> {

    override val type: Int = FIELD_TYPE_LONG_INDEX

    override val name: String = "LongRev"

    override val size: Int = 4

    override fun getValue(bytes: ByteArray, byteOrder: ByteOrder): IntArray =
        bytes.toInts(byteOrder.reversed())

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray =
        when (data) {
            is Int -> data.toBytes(byteOrder.reversed())
            is IntArray -> data.toBytes(byteOrder.reversed())
            else -> throw ImageWriteException("Unsupported type: $data")
        }
}
