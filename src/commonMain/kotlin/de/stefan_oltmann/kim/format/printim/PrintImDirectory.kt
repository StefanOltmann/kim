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
package de.stefan_oltmann.kim.format.printim

import de.stefan_oltmann.kim.common.HEX_RADIX

/**
 * The Print Image Matching block: the version string plus the fixed
 * 6-byte tag/value entries that follow it.
 */
public data class PrintImDirectory(
    val version: String,
    val entries: List<PrintImEntry>
) {

    override fun toString(): String {

        val sb = StringBuilder()

        sb.appendLine("---- PrintIM ----")
        sb.appendLine("PrintIMVersion    : $version")

        for (entry in entries) {

            val paddedTag = entry.tag.toUInt()
                .toString(HEX_RADIX)
                .padStart(HEX_TAG_DIGITS, '0')

            sb.appendLine("PrintIM_0x$paddedTag    : ${entry.value}")
        }

        return sb.toString()
    }

    private companion object {

        /** The entry tags render as four hex digits, like ExifTool names them. */
        private const val HEX_TAG_DIGITS = 4
    }
}
