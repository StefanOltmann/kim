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
import de.stefan_oltmann.kim.output.write2BytesAsInt
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

    /**
     * Builds a hdlr box - the mandatory child of a meta box, or part of
     * a media box for the "vide" handler. The name is empty unless
     * given.
     */
    fun hdlrBox(
        name: String = "",
        handlerType: String = "pict"
    ): ByteArray {

        val payload = ByteArrayByteWriter()

        /* Version & flags */
payload.write(byteArrayOf(0, 0, 0, 0))
        /* Pre-defined */
payload.write(byteArrayOf(0, 0, 0, 0))
        /* Handler type */
payload.write(handlerType.encodeToByteArray())
        /* Reserved */
payload.write(ByteArray(12))
        payload.write(name.encodeToByteArray())
        /* Name terminator */
payload.write(0)

        return box(BoxType.HDLR, payload.toByteArray())
    }

    /** Builds a pitm box of version 0 with the given 2-byte item id. */
    fun pitmBox(itemId: Int): ByteArray {

        val payload = ByteArrayByteWriter()

        /* Version & flags */
payload.write(byteArrayOf(0, 0, 0, 0))
        payload.write2BytesAsInt(itemId, BMFFConstants.BMFF_BYTE_ORDER)

        return box(BoxType.PITM, payload.toByteArray())
    }

    /**
     * One item declaration for the [iinfBox] builder. The infe entry is
     * always version 2 with an empty item name - the only form the
     * parser supports.
     */
    data class InfeEntry(
        val itemId: Int,
        val itemType: Int
    )

    /**
     * Builds an iinf box of the given version with one version-2 infe
     * box per entry. Version 0 writes a 2-byte entry count, every other
     * version the 4-byte one.
     */
    fun iinfBox(
        version: Int = 0,
        entries: List<InfeEntry>
    ): ByteArray {

        val payload = ByteArrayByteWriter()

        payload.write(version)
        /* Flags */
payload.write(byteArrayOf(0, 0, 0))

        if (version == 0)
            payload.write2BytesAsInt(entries.size, BMFFConstants.BMFF_BYTE_ORDER)
        else
            payload.writeInt(entries.size, BMFFConstants.BMFF_BYTE_ORDER)

        for (entry in entries) {

            val infePayload = ByteArrayByteWriter()

            /* The only supported infe version. */
infePayload.write(2)
            /* Flags */
infePayload.write(byteArrayOf(0, 0, 0))
            infePayload.write2BytesAsInt(entry.itemId, BMFFConstants.BMFF_BYTE_ORDER)
            /* Item protection index */
infePayload.write2BytesAsInt(0, BMFFConstants.BMFF_BYTE_ORDER)
            infePayload.writeInt(entry.itemType, BMFFConstants.BMFF_BYTE_ORDER)
            /* Empty item name */
infePayload.write(0)

            writeBox(payload, BoxType.INFE, infePayload.toByteArray())
        }

        val bytes = ByteArrayByteWriter()

        writeBox(bytes, BoxType.IINF, payload.toByteArray())

        return bytes.toByteArray()
    }
}
