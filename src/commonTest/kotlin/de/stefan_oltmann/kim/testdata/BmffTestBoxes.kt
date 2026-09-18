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
package de.stefan_oltmann.kim.testdata

import de.stefan_oltmann.kim.format.bmff.BMFFConstants
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.writeInt

/**
 * Builds ISOBMFF test boxes, so the container tests assemble synthetic
 * files instead of carrying binary fixtures.
 */
internal object BmffTestBoxes {

    /**
     * Builds a complete box: a big-endian 32-bit size covering header
     * and payload, the four type bytes, then the payload.
     */
    fun box(type: String, payload: ByteArray): ByteArray =
        box(type.encodeToByteArray(), payload)

    fun box(type: BoxType, payload: ByteArray): ByteArray =
        box(type.bytes, payload)

    fun box(type: ByteArray, payload: ByteArray): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        writeBox(byteWriter, type, payload)

        return byteWriter.toByteArray()
    }

    /**
     * Writes a complete box to the given writer, so several boxes can be
     * concatenated into one synthetic file.
     */
    fun writeBox(
        byteWriter: ByteArrayByteWriter,
        type: BoxType,
        payload: ByteArray
    ) {
        writeBox(byteWriter, type.bytes, payload)
    }

    fun writeBox(
        byteWriter: ByteArrayByteWriter,
        type: ByteArray,
        payload: ByteArray
    ) {
        byteWriter.writeInt(payload.size + 8, BMFFConstants.BMFF_BYTE_ORDER)
        byteWriter.write(type)
        byteWriter.write(payload)
    }
}
