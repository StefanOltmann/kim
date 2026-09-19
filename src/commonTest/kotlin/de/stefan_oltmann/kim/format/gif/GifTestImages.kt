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
package de.stefan_oltmann.kim.format.gif

/**
 * Extension introducer, unknown private label 0x99, one sub-block of
 * four bytes whose payload contains the image separator byte, block
 * terminator.
 */
internal val UNKNOWN_EXTENSION_BYTES: ByteArray =
    byteArrayOf(0x21, 0x99.toByte(), 4, 0x2C, 0x41, 0x42, 0x43, 0x00)

/**
 * Builds a GIF89a file with an extension of an unknown private label
 * before the first 1x1 frame. The unknown payload deliberately contains
 * the image separator byte, which exposed the stream desync.
 *
 * With [withXmp] the file also carries an XMP application extension.
 */
internal fun gif89aWithUnknownExtension(withXmp: Boolean): ByteArray {

    val bytes = mutableListOf<Byte>()

    /* Header */
    bytes.addAll("GIF89a".encodeToByteArray().toList())

    /* Logical screen descriptor: 1x1, no color table */
    bytes.addAll(byteArrayOf(1, 0, 1, 0, 0, 0, 0).toList())

    bytes.addAll(UNKNOWN_EXTENSION_BYTES.toList())

    if (withXmp) {

        /* Application extension with XMP data */
        val xmp = """<x:xmpmeta xmlns:x="adobe:ns:meta/"><rdf:RDF/></x:xmpmeta>"""
        val xmpBytes = xmp.encodeToByteArray()

        bytes.addAll(byteArrayOf(0x21, 0xFF.toByte(), 11).toList())
        bytes.addAll("XMP DataXMP".encodeToByteArray().toList())
        bytes.add(xmpBytes.size.toByte())
        bytes.addAll(xmpBytes.toList())
        bytes.add(0)
    }

    /* Image separator and descriptor: 1x1 image at 0,0 */
    bytes.add(0x2C)
    bytes.addAll(byteArrayOf(0, 0, 0, 0, 1, 0, 1, 0, 0).toList())

    /* Image data: LZW minimum code size 2, one sub chunk, terminator */
    bytes.addAll(byteArrayOf(2, 1, 5, 0).toList())

    /* Terminator */
    bytes.add(0x3B)

    return bytes.toByteArray()
}
