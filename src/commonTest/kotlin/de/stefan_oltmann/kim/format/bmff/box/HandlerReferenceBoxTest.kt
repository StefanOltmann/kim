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
package de.stefan_oltmann.kim.format.bmff.box

import de.stefan_oltmann.kim.common.ImageReadException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests reading the handler type and the name field of the handler
 * reference box in both flavors that exist in the wild.
 */
class HandlerReferenceBoxTest {

    @Test
    fun testQuickTimePascalNameIsRead() {

        /*
         * QuickTime writes the name as a Pascal string: one length byte
         * plus that many bytes, without a terminator. This is the form
         * ffmpeg writes, and an unterminated name failed the whole read.
         */
        val payload = ByteArray(NAME_FIELD_OFFSET + 1 + "VideoHandler".length)

        putHandlerType(payload, "vide")

        payload[NAME_FIELD_OFFSET] = "VideoHandler".length.toByte()

        "VideoHandler".encodeToByteArray().copyInto(payload, NAME_FIELD_OFFSET + 1)

        val box = HandlerReferenceBox(
            offset = 0,
            size = (payload.size + BOX_HEADER_LENGTH).toLong(),
            largeSize = null,
            payload = payload
        )

        assertEquals("vide", box.handlerType)
        assertEquals("VideoHandler", box.name)
    }

    @Test
    fun testIsoNullTerminatedNameIsRead() {

        /* ISO/IEC 14496-12 writes the name NUL-terminated. */
        val payload = ByteArray(NAME_FIELD_OFFSET + "abc".length + 1)

        putHandlerType(payload, "pict")

        "abc".encodeToByteArray().copyInto(payload, NAME_FIELD_OFFSET)

        val box = HandlerReferenceBox(
            offset = 0,
            size = (payload.size + BOX_HEADER_LENGTH).toLong(),
            largeSize = null,
            payload = payload
        )

        assertEquals("pict", box.handlerType)
        assertEquals("abc", box.name)
    }

    @Test
    fun testEmptyZeroPaddedNameIsRead() {

        /* Real files often carry a name field of zero bytes only. */
        val payload = ByteArray(NAME_FIELD_OFFSET + 4)

        putHandlerType(payload, "soun")

        val box = HandlerReferenceBox(
            offset = 0,
            size = (payload.size + BOX_HEADER_LENGTH).toLong(),
            largeSize = null,
            payload = payload
        )

        assertEquals("soun", box.handlerType)
        assertEquals("", box.name)
    }

    @Test
    fun testUnterminatedIsoNameStillFailsTheRead() {

        /*
         * Only the QuickTime flavor may lack the terminator - an ISO name
         * without one is a broken box and fails the strict read.
         */
        val payload = ByteArray(NAME_FIELD_OFFSET + 2)

        putHandlerType(payload, "vide")

        "xy".encodeToByteArray().copyInto(payload, NAME_FIELD_OFFSET)

        assertFailsWith<ImageReadException> {
            HandlerReferenceBox(
                offset = 0,
                size = (payload.size + BOX_HEADER_LENGTH).toLong(),
                largeSize = null,
                payload = payload
            )
        }
    }

    /**
     * Writes the four-character handler type behind version, flags and
     * the pre-defined field.
     */
    private fun putHandlerType(payload: ByteArray, handlerType: String) {
        handlerType.encodeToByteArray().copyInto(payload, 1 + FLAGS_LENGTH + PRE_DEFINED_LENGTH)
    }

    private companion object {

        /* The 8 bytes of size and box type in front of the payload. */
        const val BOX_HEADER_LENGTH: Int = 8

        /* The flags field is 3 bytes */
        const val FLAGS_LENGTH: Int = 3

        /* The pre-defined field is 4 bytes */
        const val PRE_DEFINED_LENGTH: Int = 4

        /* The handler type is 4 bytes */
        const val HANDLER_TYPE_LENGTH: Int = 4

        /* The reserved field is 12 bytes */
        const val RESERVED_LENGTH: Int = 12

        /* Version, flags, pre-defined, handler type and reserved. */
        const val NAME_FIELD_OFFSET: Int =
            1 + FLAGS_LENGTH + PRE_DEFINED_LENGTH + HANDLER_TYPE_LENGTH + RESERVED_LENGTH
    }
}
