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
package de.stefan_oltmann.kim.format.mpf

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.getRemainingBytes
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import kotlin.jvm.JvmStatic

/**
 * Parses the Multi-Picture Format index of a JPEG APP2 segment: the
 * "MPF\0" identifier followed by a complete TIFF structure whose first
 * directory carries the MPFVersion and NumberOfImages tags.
 */
public object MpfParser {

    private val SIGNATURE: ByteArray = byteArrayOf(
        0x4D, 0x50, 0x46, 0x00
    )

    private const val MPF_VERSION_TAG = 0xB000

    private const val NUMBER_OF_IMAGES_TAG = 0xB001

    @JvmStatic
    public fun parse(bytes: ByteArray): MpfDirectory =

        tryWithImageReadException {

            if (!bytes.startsWith(SIGNATURE))
                throw ImageReadException("The MPF segment lacks the 'MPF' identifier.")

            /*
             * Behind the identifier the MPF carries a complete TIFF
             * structure - byte order marker, version, first IFD offset -
             * so the directory walk of the TIFF reader handles the
             * bounds of the hostile-input case.
             */
            val contents = TiffReader.read(
                ByteArrayByteReader(bytes.getRemainingBytes(SIGNATURE.size))
            )

            val ifd0 = contents.directories.firstOrNull()

            val versionBytes = ifd0
                ?.entries
                ?.firstOrNull { field -> field.tag == MPF_VERSION_TAG }
                ?.valueBytes

            val numberOfImages = ifd0
                ?.entries
                ?.firstOrNull { field -> field.tag == NUMBER_OF_IMAGES_TAG }
                ?.toInt()

            MpfDirectory(
                version = versionBytes?.decodeToString().orEmpty(),
                numberOfImages = numberOfImages ?: 0
            )
        }
}
