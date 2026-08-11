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
import de.stefan_oltmann.kim.model.ImageSize
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.write2BytesAsInt
import kotlin.jvm.JvmStatic

/**
 * The image descriptor chunk of a GIF file.
 */
public class GifChunkImageDescriptor(
    bytes: ByteArray
) : GifChunk(
    GifChunkType.IMAGE_DESCRIPTOR,
    bytes
) {

    public val leftPosition: Int
    public val topPosition: Int
    public val imageSize: ImageSize
    public val localColorTableFlag: Boolean
    public val interlaceFlag: Boolean
    public val sortFlag: Boolean
    public val localColorTableSize: Int

    init {

        if (bytes.size != IMAGE_DESCRIPTOR_LENGTH)
            throw ImageReadException("Invalid size for Image Descriptor chunk: ${bytes.size} bytes, expected 10 bytes.")

        val byteReader = ByteArrayByteReader(bytes)

        /* Read image separator */
        if (byteReader.readByte("image separator") != GifConstants.IMAGE_SEPARATOR)
            throw ImageReadException("Image descriptor did not start with image separator")

        /* Read image position and dimensions */
        leftPosition = byteReader.read2BytesAsInt("left position", GifConstants.GIF_BYTE_ORDER)
        topPosition = byteReader.read2BytesAsInt("top position", GifConstants.GIF_BYTE_ORDER)

        val width = byteReader.read2BytesAsInt("width", GifConstants.GIF_BYTE_ORDER)
        val height = byteReader.read2BytesAsInt("height", GifConstants.GIF_BYTE_ORDER)
        imageSize = ImageSize(width, height)

        val packed = byteReader.readByte("packed fields")
        localColorTableFlag = (packed.toInt() shr LOCAL_COLOR_TABLE_FLAG_BIT and 1) == 1
        interlaceFlag = (packed.toInt() shr INTERLACE_FLAG_BIT and 1) == 1
        sortFlag = (packed.toInt() shr SORT_FLAG_BIT and 1) == 1
        localColorTableSize = packed.toInt() and LOCAL_COLOR_TABLE_SIZE_MASK
    }

    public companion object {

        /* The image descriptor is 10 bytes */
        private const val IMAGE_DESCRIPTOR_LENGTH = 10

        /* Bit position of the local color table flag */
        private const val LOCAL_COLOR_TABLE_FLAG_BIT = 7

        /* Bit position of the interlace flag */
        private const val INTERLACE_FLAG_BIT = 6

        /* Bit position of the sort flag */
        private const val SORT_FLAG_BIT = 5

        /* Mask for the 3-bit local color table size */
        private const val LOCAL_COLOR_TABLE_SIZE_MASK = 0b00000111

        @JvmStatic
        public fun constructFromProperties(
            leftPosition: Int,
            topPosition: Int,
            imageSize: ImageSize,
            localColorTableFlag: Boolean,
            interlaceFlag: Boolean,
            sortFlag: Boolean,
            localColorTableSize: Int
        ): GifChunkImageDescriptor {

            val byteWriter = ByteArrayByteWriter()

            byteWriter.write(GifConstants.IMAGE_SEPARATOR)

            byteWriter.write2BytesAsInt(leftPosition, GifConstants.GIF_BYTE_ORDER)
            byteWriter.write2BytesAsInt(topPosition, GifConstants.GIF_BYTE_ORDER)
            byteWriter.write2BytesAsInt(imageSize.width, GifConstants.GIF_BYTE_ORDER)
            byteWriter.write2BytesAsInt(imageSize.height, GifConstants.GIF_BYTE_ORDER)

            val packed = ((if (localColorTableFlag) 1 else 0) shl LOCAL_COLOR_TABLE_FLAG_BIT) or
                ((if (interlaceFlag) 1 else 0) shl INTERLACE_FLAG_BIT) or
                ((if (sortFlag) 1 else 0) shl SORT_FLAG_BIT) or
                (localColorTableSize and LOCAL_COLOR_TABLE_SIZE_MASK)

            byteWriter.write(packed)

            return GifChunkImageDescriptor(byteWriter.toByteArray())
        }
    }
}
