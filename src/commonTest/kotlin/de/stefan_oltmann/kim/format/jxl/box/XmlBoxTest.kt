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
package de.stefan_oltmann.kim.format.jxl.box

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.format.jxl.JxlReader
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class XmlBoxTest {

    /**
     * Binary garbage in an XML box fails the update path in
     * XMPMetaFactory, so it must fail the read as well
     * (read/update symmetry).
     */
    @Test
    fun testXmlBoxWithGarbagePayloadIsRejectedOnRead() {

        assertFailsWith<ImageReadException> {
            JxlReader.createMetadata(
                listOf(
                    XmlBox(
                        offset = 0,
                        size = 15,
                        largeSize = null,
                        payload = "this is not xmp".encodeToByteArray()
                    )
                )
            )
        }
    }

    /**
     * End to end: a JXL container whose XML box carries binary garbage
     * must fail Kim.readMetadata with the documented exception type.
     */
    @Test
    fun testKimReadMetadataRejectsGarbageXmlBox() {

        val byteWriter = ByteArrayByteWriter()

        /* JXL file signature. */
        byteWriter.write(
            byteArrayOf(0, 0, 0, 0x0C, 0x4A, 0x58, 0x4C, 0x20, 0x0D, 0x0A, 0x87.toByte(), 0x0A)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.FTYP,
            payload = "jxl ".encodeToByteArray() + byteArrayOf(0, 0, 0, 0) + "jxl ".encodeToByteArray()
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.JXLC,
            payload = byteArrayOf(0xFF.toByte(), 0x0A)
        )

        writeBox(
            byteWriter = byteWriter,
            type = BoxType.XML,
            payload = "this is not xmp".encodeToByteArray()
        )

        val exception = assertFailsWith<ImageReadException> {
            Kim.readMetadata(byteWriter.toByteArray())
        }

        assertTrue(exception.message?.contains("xmpmeta") == true)
    }

    private fun writeBox(
        byteWriter: ByteArrayByteWriter,
        type: BoxType,
        payload: ByteArray
    ) {

        val size = payload.size + 8

        byteWriter.write(
            byteArrayOf(
                (size ushr 24).toByte(),
                (size ushr 16).toByte(),
                (size ushr 8).toByte(),
                size.toByte()
            )
        )
        byteWriter.write(type.bytes)
        byteWriter.write(payload)
    }
}
