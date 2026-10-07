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
package de.stefan_oltmann.kim.format.icc

/**
 * An ICC color profile: the identity fields of the 128-byte header plus
 * the decoded entries of the tag table that follows it.
 *
 * Like ExifTool, the profile description ([IccEntry.name] =
 * "ProfileDescription") and the color space are the fields consumers
 * query most - they name the actual color space an image claims.
 */
public data class IccProfile(
    val size: Int,
    val cmmType: String,
    val version: String,
    val profileClass: String,
    val colorSpace: String,
    val connectionSpace: String,
    val primaryPlatform: String?,
    val renderingIntent: Int,
    val entries: List<IccEntry>
) {

    /** Returns the profile description, or NULL when the profile carries none. */
    public val description: String?
        get() = findEntry("ProfileDescription")?.value

    /** Returns the entry with the given registry name, or NULL when absent. */
    public fun findEntry(name: String): IccEntry? =
        entries.firstOrNull { entry -> entry.name == name }

    override fun toString(): String {

        val sb = StringBuilder()

        sb.appendLine("---- ICC ----")
        sb.appendLine("ProfileSize        : $size")
        sb.appendLine("ProfileCMMType     : $cmmType")
        sb.appendLine("ProfileVersion     : $version")
        sb.appendLine("ProfileClass       : $profileClass")
        sb.appendLine("ColorSpaceData     : $colorSpace")
        sb.appendLine("ProfileConnectionSpace : $connectionSpace")
        sb.appendLine("PrimaryPlatform    : ${primaryPlatform.orEmpty()}")
        sb.appendLine("RenderingIntent    : $renderingIntent")

        for (entry in entries) {

            val name = entry.name ?: entry.signature

            val value = entry.value

            if (value != null)
                sb.appendLine("$name = $value")
            else
                sb.appendLine("$name (${entry.signature})")
        }

        return sb.toString()
    }
}
