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
package de.stefan_oltmann.kim.format.jpeg.iptc

internal object IptcConstants {

    const val IPTC_NON_EXTENDED_RECORD_MAXIMUM_SIZE = 32_767

    /*
     * Marker value of the 2-byte dataset length field for the IPTC
     * extended-length encoding. The actual size follows as a 4-byte value.
     */
    const val IPTC_EXTENDED_RECORD_LENGTH_MARKER = 0x8000

    /** The extended length field is 4 bytes, like ExifTool writes it. */
    const val IPTC_EXTENDED_LENGTH_FIELD_SIZE = 4

    /** The length field size is stored in the low 15 bits of the length word. */
    const val IPTC_EXTENDED_LENGTH_SIZE_MASK = 0x7FFF

    /** The maximum number of bytes the extended length field may use. */
    const val IPTC_MAX_EXTENDED_LENGTH_FIELD_SIZE = 8

    /** IPTC data consists of 32-bit words. */
    const val IPTC_WORD_SIZE = 4

    /* The IPTC block type must fit in 2 bytes */
    const val MAX_IPTC_BLOCK_TYPE = 0xFFFF

    /* The IPTC record type must fit in 1 byte */
    const val MAX_IPTC_RECORD_TYPE = 0xFF

    /*
     * The Photoshop image resource block types that Kim reads and
     * writes: the IPTC records themselves and the digest that ties
     * them to the XMP.
     */
    const val IMAGE_RESOURCE_BLOCK_IPTC_DATA = 0x0404
    const val IMAGE_RESOURCE_BLOCK_IPTC_DIGEST = 0x0425
    const val IPTC_RECORD_TAG_MARKER = 0x1c
    const val IPTC_ENVELOPE_RECORD_NUMBER = 0x01
    const val IPTC_APPLICATION_2_RECORD_NUMBER = 0x02
    const val IPTC_RECORD_VERSION_VALUE = 4
}
