/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2026 Ramon Bouckaert
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
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import de.stefan_oltmann.kim.format.bmff.BMFFConstants.FLAGS_LENGTH
import de.stefan_oltmann.kim.format.bmff.BoxType
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.readByteAsInt
import de.stefan_oltmann.kim.input.readXBytesAtInt
import de.stefan_oltmann.kim.input.skipBytes
import kotlin.math.roundToInt

/**
 * EIC/ISO 14496-12 track header box.
 *
 * The Track Header Box appears within a Track Box and contains track
 * metadata, including the display size of a video track.
 */
public class TrackHeaderBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.TKHD, offset, size, largeSize, payload) {

    public val version: Int

    /** The display width of the track, rounded from a 16.16 fixed point value. */
    public val width: Int

    /** The display height of the track, rounded from a 16.16 fixed point value. */
    public val height: Int

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        byteReader.skipBytes("flags", FLAGS_LENGTH)

        /*
         * Version 1 stores the time and duration fields as 64-bit values,
         * which shifts every field behind them by 8 bytes. Higher versions
         * are not defined by ISO 14496-12 and fail the read instead of
         * being misinterpreted.
         */
        val sizeFieldOffset = when (version) {
            VERSION_32_BIT_FIELDS -> V0_SIZE_FIELD_OFFSET
            VERSION_64_BIT_FIELDS -> V1_SIZE_FIELD_OFFSET
            else -> throw ImageReadException("Unknown track header version: $version")
        }

        /* The version and flags bytes in front of it are already consumed. */
        byteReader.skipBytes("fields in front of the size", sizeFieldOffset - VERSION_FLAGS_LENGTH)

        width = readDisplaySize(byteReader, "width")

        height = readDisplaySize(byteReader, "height")
    }

    override fun toString(): String =
        "$type Box @$offset width=$width height=$height"

    /**
     * Reads one size field and converts its 16.16 fixed point encoding
     * into whole pixels.
     */
    private fun readDisplaySize(byteReader: ByteArrayByteReader, fieldName: String): Int {

        val rawValue = byteReader.readXBytesAtInt(fieldName, FIELD_LENGTH, BMFF_BYTE_ORDER)

        return (rawValue / FIXED_POINT_ONE).roundToInt()
    }

    private companion object {

        /** Version 0 stores the time and duration fields as 32-bit values. */
        const val VERSION_32_BIT_FIELDS: Int = 0

        /** Version 1 stores the time and duration fields as 64-bit values. */
        const val VERSION_64_BIT_FIELDS: Int = 1

        /* Payload offset of the width field for the respective version. */
        const val V0_SIZE_FIELD_OFFSET: Int = 76
        const val V1_SIZE_FIELD_OFFSET: Int = 88

        /* The version byte and the three flag bytes in front of the fields. */
        const val VERSION_FLAGS_LENGTH: Int = 4

        const val FIELD_LENGTH: Int = 4

        /** A 16.16 fixed point value stores the fraction in the low 16 bits. */
        const val FIXED_POINT_ONE: Double = 65536.0
    }
}
