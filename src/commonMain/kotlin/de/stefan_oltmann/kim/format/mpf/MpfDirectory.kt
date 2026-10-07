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

/**
 * The Multi-Picture Format index: the format version and the number of
 * individual images the file bundles, like ExifTool reports them from
 * the MPF0 directory.
 */
public data class MpfDirectory(
    val version: String,
    val numberOfImages: Int
) {

    override fun toString(): String {

        val sb = StringBuilder()

        sb.appendLine("---- MPF ----")
        sb.appendLine("MPFVersion      : $version")
        sb.appendLine("NumberOfImages  : $numberOfImages")

        return sb.toString()
    }
}
