/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ramon Bouckaert
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

package de.stefan_oltmann.kim.format.gif.chunk

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.gif.GifChunkType
import de.stefan_oltmann.kim.format.gif.GifConstants
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.read2BytesAsInt
import de.stefan_oltmann.kim.input.readByte
import de.stefan_oltmann.kim.input.readByteAsInt
import de.stefan_oltmann.kim.model.ImageSize
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.write2BytesAsInt
import kotlin.jvm.JvmStatic

/**
 * The logical screen descriptor chunk of a GIF file.
 */
public class GifChunkLogicalScreenDescriptor(
    bytes: ByteArray
) : GifChunk(
    GifChunkType.LOGICAL_SCREEN_DESCRIPTOR,
    bytes
) {

    public val canvasSize: ImageSize
    public val globalColorTableFlag: Boolean
    public val colorResolution: Int
    public val sortFlag: Boolean
    public val globalColorTableSize: Int
    public val backgroundColorIndex: Int
    public val pixelAspectRatio: Int

    init {

        if (bytes.size != LOGICAL_SCREEN_DESCRIPTOR_LENGTH)
            throw ImageReadException(
                "Invalid size for logical screen descriptor: ${bytes.size} bytes, expected 7 bytes."
            )

        val byteReader = ByteArrayByteReader(bytes)

        /* Read canvas width and height */
        val canvasWidth = byteReader.read2BytesAsInt("canvas width", GifConstants.GIF_BYTE_ORDER)
        val canvasHeight = byteReader.read2BytesAsInt("canvas height", GifConstants.GIF_BYTE_ORDER)
        canvasSize = ImageSize(canvasWidth, canvasHeight)

        /* Read packed data */
        val packed = byteReader.readByte("packed fields").toInt()
        globalColorTableFlag = (packed shr GLOBAL_COLOR_TABLE_FLAG_BIT and 1) == 1
        colorResolution = (packed shr COLOR_RESOLUTION_SHIFT) and COLOR_RESOLUTION_MASK
        sortFlag = (packed shr SORT_FLAG_BIT and 1) == 1
        globalColorTableSize = packed and GLOBAL_COLOR_TABLE_SIZE_MASK

        /* Read background color index */
        backgroundColorIndex = byteReader.readByteAsInt()

        /* Read pixel aspect ratio */
        pixelAspectRatio = byteReader.readByteAsInt()
    }

    public companion object {

        /* The logical screen descriptor is 7 bytes */
        private const val LOGICAL_SCREEN_DESCRIPTOR_LENGTH = 7

        /* Bit position of the global color table flag */
        private const val GLOBAL_COLOR_TABLE_FLAG_BIT = 7

        /* Bit position of the color resolution */
        private const val COLOR_RESOLUTION_SHIFT = 4

        /* Mask for the 3-bit color resolution */
        private const val COLOR_RESOLUTION_MASK = 0b111

        /* Bit position of the sort flag */
        private const val SORT_FLAG_BIT = 3

        /* Mask for the 3-bit global color table size */
        private const val GLOBAL_COLOR_TABLE_SIZE_MASK = 0b111

        @JvmStatic
        public fun constructFromProperties(
            canvasSize: ImageSize,
            globalColorTableFlag: Boolean,
            colorResolution: Int,
            sortFlag: Boolean,
            globalColorTableSize: Int,
            backgroundColorIndex: Int,
            pixelAspectRatio: Int
        ): GifChunkLogicalScreenDescriptor {

            val byteWriter = ByteArrayByteWriter()

            byteWriter.write2BytesAsInt(canvasSize.width, GifConstants.GIF_BYTE_ORDER)
            byteWriter.write2BytesAsInt(canvasSize.height, GifConstants.GIF_BYTE_ORDER)

            val packed = (
                ((if (globalColorTableFlag) 1 else 0) shl GLOBAL_COLOR_TABLE_FLAG_BIT) or
                    ((colorResolution and COLOR_RESOLUTION_MASK) shl COLOR_RESOLUTION_SHIFT) or
                    ((if (sortFlag) 1 else 0) shl SORT_FLAG_BIT) or
                    (globalColorTableSize and GLOBAL_COLOR_TABLE_SIZE_MASK)
                ).toByte()

            byteWriter.write(packed)

            byteWriter.write(backgroundColorIndex)
            byteWriter.write(pixelAspectRatio)

            return GifChunkLogicalScreenDescriptor(byteWriter.toByteArray())
        }
    }
}
