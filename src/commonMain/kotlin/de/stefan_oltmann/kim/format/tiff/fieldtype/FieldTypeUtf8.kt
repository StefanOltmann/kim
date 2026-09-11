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
import de.stefan_oltmann.kim.common.indexOfNullTerminator
import de.stefan_oltmann.kim.common.slice

/**
 * UTF-8 string that is terminated with NUL, added by the EXIF 3.0
 * specification as type 129.
 */
public data object FieldTypeUtf8 : FieldType<String> {

    override val type: Int = 129

    override val name: String = "UTF8"

    override val size: Int = 1

    override fun getValue(bytes: ByteArray, byteOrder: ByteOrder): String {

        val nullTerminatorIndex = bytes.indexOfNullTerminator()

        val length = if (nullTerminatorIndex > -1)
            nullTerminatorIndex
        else
            bytes.size

        if (length == 0)
            return ""

        return bytes.slice(
            startIndex = 0,
            count = length
        ).decodeToString()
    }

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray {

        if (data !is String)
            throw ImageWriteException("UTF8 Data must be String")

        val bytes = data.encodeToByteArray()

        val result = bytes.copyOf(bytes.size + 1)

        result[bytes.size] = 0

        return result
    }
}
