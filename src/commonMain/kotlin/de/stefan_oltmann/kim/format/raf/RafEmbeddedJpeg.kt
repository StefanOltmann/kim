/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package de.stefan_oltmann.kim.format.raf

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.read4BytesAsInt
import de.stefan_oltmann.kim.input.readAndVerifyBytes
import de.stefan_oltmann.kim.input.skipBytes

/**
 * Navigation to the JPEG that RAF files embed for their metadata.
 *
 * See http://fileformats.archiveteam.org/wiki/Fujifilm_RAF
 */
internal object RafEmbeddedJpeg {

    internal const val REMAINING_HEADER_BYTE_COUNT = 68

    /**
     * Verifies the RAF magic and positions the reader at the start of
     * the embedded JPEG.
     */
    internal fun positionReaderAtJpeg(byteReader: ByteReader) =

        with(byteReader) {

            readAndVerifyBytes(
                "RAF magic number",
                MediaFormatMagicNumbers.raf.toByteArray()
            )

            skipBytes("68 header bytes", REMAINING_HEADER_BYTE_COUNT)

            val offset = read4BytesAsInt("JPEG offset", ByteOrder.BIG_ENDIAN)

            /*
             * A hostile offset cannot point into the file. Rejecting it
             * here beats the underflowing skip distance (and the wasteful
             * full-file scan) that the raw subtraction would produce.
             */
            if (offset <= 0 || offset > contentLength)
                throw ImageReadException("RAF JPEG offset out of range: $offset")

            @Suppress("MagicNumber")
            val remainingBytesToOffset =
                offset - (REMAINING_HEADER_BYTE_COUNT + MediaFormatMagicNumbers.raf.size + 4)

            skipBytes("Skip JPEG offset", remainingBytesToOffset)
        }
}
