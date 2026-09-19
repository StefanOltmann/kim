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
package de.stefan_oltmann.kim.format.webp.chunk

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.webp.WebPChunkType
import de.stefan_oltmann.kim.format.webp.WebPConstants
import de.stefan_oltmann.kim.format.webp.WebPConstants.VP8X_PAYLOAD_LENGTH
import de.stefan_oltmann.kim.model.ImageSize

/**
 * The extended VP8X container chunk of a WebP file.
 *
 * https://developers.google.com/speed/webp/docs/riff_container#extended_file_format
 */
@Suppress("MagicNumber")
public class WebPChunkVP8X(
    bytes: ByteArray
) : WebPChunk(WebPChunkType.VP8X, bytes), ImageSizeAware {

    /** Whether the file carries an ICC color profile chunk. */
    public val hasIcc: Boolean

    /** Whether the image carries an alpha channel. */
    public val hasAlpha: Boolean

    /** Whether the file carries an EXIF chunk. */
    public val hasExif: Boolean

    /** Whether the file carries an XMP chunk. */
    public val hasXmp: Boolean

    /** Whether the image is an animation of multiple frames. */
    public val hasAnimation: Boolean

    override val imageSize: ImageSize

    init {

        if (bytes.size != VP8X_PAYLOAD_LENGTH)
            throw ImageReadException("VP8X chunk must be 10 bytes long, but was ${bytes.size}.")

        val mark: Int = bytes[0].toInt() and 0xFF

        hasIcc = mark and ICC_FLAG != 0
        hasAlpha = mark and ALPHA_FLAG != 0
        hasExif = mark and EXIF_FLAG != 0
        hasXmp = mark and XMP_FLAG != 0
        hasAnimation = mark and ANIMATION_FLAG != 0

        val canvasWidth = (bytes[4].toInt() and 0xFF) +
            (bytes[5].toInt() and 0xFF shl 8) +
            (bytes[6].toInt() and 0xFF shl 16) + 1

        val canvasHeight = (bytes[7].toInt() and 0xFF) +
            (bytes[8].toInt() and 0xFF shl 8) +
            (bytes[9].toInt() and 0xFF shl 16) + 1

        imageSize = ImageSize(
            width = canvasWidth,
            height = canvasHeight
        )

        if (imageSize.longestSide > WebPConstants.MAX_SIDE_LENGTH)
            throw ImageReadException("Illegal dimensions: $imageSize")
    }

    override fun toString(): String =
        super.toString() +
            " hasIcc=$hasIcc hasAlpha=$hasAlpha hasExif=$hasExif" +
            " hasXmp=$hasXmp hasAnimation=$hasAnimation" +
            " imageSize=$imageSize"

    public companion object {

        /* The flag bits of the mark byte, per the RIFF container spec. */
        private const val ICC_FLAG: Int = 32
        private const val ALPHA_FLAG: Int = 16
        private const val EXIF_FLAG: Int = 8
        private const val XMP_FLAG: Int = 4
        private const val ANIMATION_FLAG: Int = 2

        /**
         * Builds the 10-byte VP8X payload with the given format flags
         * and the canvas size.
         */
        public fun createBytes(
            hasIcc: Boolean,
            hasAlpha: Boolean,
            hasExif: Boolean,
            hasXmp: Boolean,
            hasAnimation: Boolean,
            imageSize: ImageSize
        ): ByteArray {

            if (imageSize.longestSide > WebPConstants.MAX_SIDE_LENGTH)
                throw ImageReadException("Illegal dimensions: $imageSize")

            val byteArray = ByteArray(VP8X_PAYLOAD_LENGTH)

            /* Set the mark byte based on flags */
            var mark = 0

            if (hasIcc)
                mark = mark or ICC_FLAG

            if (hasAlpha)
                mark = mark or ALPHA_FLAG

            if (hasExif)
                mark = mark or EXIF_FLAG

            if (hasXmp)
                mark = mark or XMP_FLAG

            if (hasAnimation)
                mark = mark or ANIMATION_FLAG

            byteArray[0] = mark.toByte()

            /* Set canvas width */
            val canvasWidth = imageSize.width - 1
            byteArray[4] = canvasWidth.toByte()
            byteArray[5] = (canvasWidth shr 8).toByte()
            byteArray[6] = (canvasWidth shr 16).toByte()

            /* Set canvas height */
            val canvasHeight = imageSize.height - 1
            byteArray[7] = canvasHeight.toByte()
            byteArray[8] = (canvasHeight shr 8).toByte()
            byteArray[9] = (canvasHeight shr 16).toByte()

            return byteArray
        }
    }
}
