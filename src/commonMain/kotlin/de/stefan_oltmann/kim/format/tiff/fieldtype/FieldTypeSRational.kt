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
import de.stefan_oltmann.kim.common.RationalNumbers
import de.stefan_oltmann.kim.common.toBytes
import de.stefan_oltmann.kim.common.toRationals
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants

/**
 * Two SLONG’s: the first represents the numerator of a
 * fraction, the second the denominator.
 */
public object FieldTypeSRational : FieldType<RationalNumbers> {

    override val type: Int = TiffConstants.FIELD_TYPE_SRATIONAL_INDEX

    override val name: String = "SRational"

    override val size: Int = 8

    override fun getValue(bytes: ByteArray, byteOrder: ByteOrder): RationalNumbers =
        bytes.toRationals(unsignedType = false, byteOrder)

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray =
        (data as RationalNumbers).toBytes(byteOrder)
}
