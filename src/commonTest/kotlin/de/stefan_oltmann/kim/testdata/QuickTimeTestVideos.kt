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

/**
 * Builds minimal synthetic QuickTime containers for the unit tests, so the
 * metadata paths are testable without multi-megabyte video files.
 */
internal object QuickTimeTestVideos {

    /* language=XML */
    val xmpPacket = """
        <?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>
        <x:xmpmeta xmlns:x="adobe:ns:meta/">
        <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
        <rdf:Description rdf:about=""
        xmlns:dc="http://purl.org/dc/elements/1.1/"
        xmlns:xmp="http://ns.adobe.com/xap/1.0/">
        <dc:title>
        <rdf:Alt>
        <rdf:li xml:lang="x-default">Video title</rdf:li>
        </rdf:Alt>
        </dc:title>
        <dc:subject>
        <rdf:Bag>
        <rdf:li>holiday</rdf:li>
        <rdf:li>beach</rdf:li>
        </rdf:Bag>
        </dc:subject>
        <xmp:Rating>4</xmp:Rating>
        </rdf:Description>
        </rdf:RDF>
        </x:xmpmeta>
        <?xpacket end="w"?>
    """.trimIndent()

    fun box(type: String, payload: ByteArray): ByteArray {

        val size = payload.size + 8

        return byteArrayOf(
            (size shr 24).toByte(),
            (size shr 16).toByte(),
            (size shr 8).toByte(),
            size.toByte()
        ) + type.encodeToByteArray() + payload
    }

    fun uuidBytes(hexString: String): ByteArray =
        ByteArray(hexString.length / 2) { index ->
            hexString.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }

    fun ftypBox(): ByteArray =
        box(
            type = "ftyp",
            payload = "qt  ".encodeToByteArray() + "0000".encodeToByteArray()
        )

    fun mdatBox(): ByteArray = box(type = "mdat", payload = byteArrayOf(1, 2, 3, 4))

    fun xmpUuidPayload(xmpBytes: ByteArray): ByteArray =
        uuidBytes(BMFFConstants.XMP_UUID) + xmpBytes

    /** ftyp - moov[uuid(XMP)] - mdat. */
    fun movWithXmpInMoov(xmpBytes: ByteArray): ByteArray =
        ftypBox() + box("moov", box("uuid", xmpUuidPayload(xmpBytes))) + mdatBox()

    /** ftyp - moov[udta[XMP_(XMP)]] - mdat, like ExifTool writes XMP into MOV videos. */
    fun movWithXmpUnderscoreInUdta(xmpBytes: ByteArray): ByteArray =
        ftypBox() +
            box("moov", box("udta", box("XMP_", xmpBytes))) +
            mdatBox()

    /** ftyp - moov[] - uuid(XMP) - mdat. */
    fun movWithXmpAtTopLevel(xmpBytes: ByteArray): ByteArray =
        ftypBox() +
            box("moov", ByteArray(0)) +
            box("uuid", xmpUuidPayload(xmpBytes)) +
            mdatBox()

    /** ftyp - moov[uuid(XMP)] - moof - mdat, like a fragmented recording. */
    fun fragmentedVideoWithXmpInMoov(xmpBytes: ByteArray): ByteArray =
        ftypBox() +
            box("moov", box("uuid", xmpUuidPayload(xmpBytes))) +
            box("moof", ByteArray(0)) +
            mdatBox()

    /**
     * ftyp - moov[] - mdat with the size field 0, which per spec means the
     * box extends to the end of the file, so nothing may be appended
     * behind it.
     */
    fun videoWithMdatExtendingToFileEnd(xmpBytes: ByteArray? = null): ByteArray {

        val moovChildren = if (xmpBytes != null)
            box("uuid", xmpUuidPayload(xmpBytes))
        else
            ByteArray(0)

        val head = ftypBox() + box("moov", moovChildren)

        val mdatHeader = byteArrayOf(
            0, 0, 0, 0,
            'm'.code.toByte(), 'd'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte()
        ) + byteArrayOf(1, 2, 3, 4)

        return head + mdatHeader
    }

    /**
     * ftyp - moov[uuid(XMP)] - a mdat header declaring 1 MiB that is
     * followed by only four bytes, like an interrupted recording.
     */
    fun truncatedVideoWithXmpInMoov(xmpBytes: ByteArray): ByteArray {

        val head = ftypBox() + box("moov", box("uuid", xmpUuidPayload(xmpBytes)))

        val mdatHeader = byteArrayOf(
            0x00, 0x10, 0x00, 0x00,
            'm'.code.toByte(), 'd'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte()
        ) + byteArrayOf(1, 2, 3, 4)

        return head + mdatHeader
    }

    /** ftyp - moov[udta[MVTG]] - mdat, the Fujifilm metadata container. */
    fun videoWithMvtgPayload(mvtgPayload: ByteArray): ByteArray {

        val mvtgBox = box(type = "MVTG", payload = mvtgPayload)

        val moovBox = box(type = "moov", payload = box("udta", mvtgBox))

        return ftypBox() + moovBox + mdatBox()
    }

    /**
     * A track with a track header and the given handler type in its media
     * handler box.
     */
    fun trakBox(handlerType: String, tkhdPayload: ByteArray = ByteArray(84)): ByteArray =
        box(
            type = "trak",
            payload = box("tkhd", tkhdPayload) +
                box("mdia", box("hdlr", hdlrPayload(handlerType)))
        )

    /**
     * The payload of a handler box: version and flags, the QuickTime
     * component type, the handler type, the reserved bytes and the null
     * terminated name.
     */
    fun hdlrPayload(handlerType: String): ByteArray =
        ByteArray(8) + handlerType.encodeToByteArray() + ByteArray(13)

    /**
     * A version 0 track header payload whose size fields describe the
     * given display resolution.
     */
    fun tkhdPayload(widthPx: Int, heightPx: Int): ByteArray {

        val payload = ByteArray(84)

        putFixedPointDisplaySize(payload, offset = 76, pixels = widthPx)
        putFixedPointDisplaySize(payload, offset = 80, pixels = heightPx)

        return payload
    }

    /**
     * Writes one size field of a track header as a 16.16 fixed point value
     * in big endian, like the ISO base media file format stores it.
     */
    private fun putFixedPointDisplaySize(payload: ByteArray, offset: Int, pixels: Int) {

        val value = pixels shl 16

        payload[offset] = (value shr 24).toByte()
        payload[offset + 1] = (value shr 16).toByte()
        payload[offset + 2] = (value shr 8).toByte()
        payload[offset + 3] = value.toByte()
    }
}
