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
import de.stefan_oltmann.kim.common.toDoubles
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants

/**
 * Double precision (8-byte) IEEE format.
 */
public object FieldTypeDouble : FieldType<DoubleArray> {

    override val type: Int = TiffConstants.FIELD_TYPE_DOUBLE_INDEX

    override val name: String = "Double"

    override val size: Int = 8

    override fun getValue(bytes: ByteArray, byteOrder: ByteOrder): DoubleArray =
        bytes.toDoubles(byteOrder)

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray =
        when (data) {
            is Double -> data.toBytes(byteOrder)
            is DoubleArray -> data.toBytes(byteOrder)
            else -> throw ImageWriteException("Unsupported type: $data")
        }
}
