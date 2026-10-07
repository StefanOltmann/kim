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

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Compares the ICC profiles parsed from every corpus image that carries
 * one against the independent ExifTool dumps of the same files: the
 * header fields, the tag names, the tag signatures and the decoded
 * values must match field by field - like ExifTool decodes them.
 */
class IccExifToolDumpComparisonTest {

    @Test
    fun testIccProfilesMatchTheExifToolDumps() {

        var comparedFiles = 0
        var comparedEntries = 0

        for (id in 1..KimTestData.TEST_MEDIA_COUNT) {

            val dump = iccDumpSectionOf(id) ?: continue

            comparedFiles++

            val metadata = assertNotNull(
                Kim.readMetadata(KimTestData.getBytesOf(id)),
                "media_$id must be readable"
            )

            val profile = assertNotNull(
                metadata.iccProfile,
                "media_$id carries an ICC profile in the ExifTool dump"
            )

            val header = parseHeaderFields(dump)

            assertEquals(
                expected = header["ProfileCMMType"]!!.ifEmpty { null },
                actual = profile.cmmType,
                message = "media_$id ProfileCMMType"
            )

            assertEquals(
                expected = header["ProfileClass"],
                actual = profile.profileClass,
                message = "media_$id ProfileClass"
            )

            assertEquals(
                expected = header["ColorSpaceData"],
                actual = profile.colorSpace,
                message = "media_$id ColorSpaceData"
            )

            assertEquals(
                expected = header["ProfileConnectionSpace"],
                actual = profile.connectionSpace,
                message = "media_$id ProfileConnectionSpace"
            )

            assertEquals(
                expected = header["PrimaryPlatform"]!!.ifEmpty { null },
                actual = profile.primaryPlatform,
                message = "media_$id PrimaryPlatform"
            )

            assertEquals(
                expected = header["RenderingIntent"]!!.trim().toInt(),
                actual = profile.renderingIntent,
                message = "media_$id RenderingIntent"
            )

            assertEquals(
                expected = iccVersionOf(header["ProfileVersion"]!!),
                actual = profile.version,
                message = "media_$id ProfileVersion"
            )

            val tagEntries = parseTagEntries(dump)

            assertEquals(
                expected = tagEntries.size,
                actual = profile.entries.size,
                message = "media_$id entry count"
            )

            for (tagEntry in tagEntries) {

                val kimEntry = assertNotNull(
                    profile.findEntry(tagEntry.name),
                    "media_$id misses the ${tagEntry.name} entry"
                )

                assertEquals(
                    expected = tagEntry.signature,
                    actual = kimEntry.signature,
                    message = "media_$id ${tagEntry.name} signature"
                )

                comparedEntries++

                compareValue(id, tagEntry, kimEntry.value)
            }
        }

        assertTrue(
            comparedFiles >= 30,
            "Only $comparedFiles corpus files carry an ICC dump"
        )

        assertTrue(
            comparedEntries >= 300,
            "Only $comparedEntries ICC entries were compared"
        )
    }

    private data class TagEntry(
        val name: String,
        val signature: String,
        val type: String,
        val value: String?,
        val isSubDirectory: Boolean
    )

    /**
     * Returns the text of every `ICC_Profile chunk` section of the
     * dump, or NULL when the file carries no ICC profile.
     */
    private fun iccDumpSectionOf(id: Int): String? {

        val resource = Resource("de/stefan_oltmann/kim/testdata/exiftool/media_$id.txt")

        if (!resource.exists())
            return null

        val sections = mutableListOf<String>()
        var current: StringBuilder? = null

        for (line in resource.readText().split('\n')) {

            val isTopLevel = line.isNotEmpty() && !line.startsWith(' ')

            if (isTopLevel) {

                if (line.startsWith("ICC_Profile chunk")) {

                    current = StringBuilder()

                } else if (current != null) {

                    sections.add(current.toString())
                    current = null
                }

            } else if (current != null) {

                current.append(line).append('\n')
            }
        }

        if (current != null)
            sections.add(current.toString())

        return sections.singleOrNull()
    }

    /**
     * Collects the `| | Name = value` lines of the 128-byte profile
     * header.
     */
    private fun parseHeaderFields(section: String): Map<String, String> =

        section.lineSequence()
            .mapNotNull { headerRegex.find(it) }
            .associate { it.groupValues[1] to it.groupValues[2] }

    /**
     * Collects the numbered tag table entries, each paired with the
     * `- Tag 'sig' (... type '...')` line that follows it.
     */
    private fun parseTagEntries(section: String): List<TagEntry> {

        val entries = mutableListOf<TagEntry>()

        val lines = section.split('\n')

        for ((index, line) in lines.withIndex()) {

            /*
             * Entries with a decoded value, or a SubDirectory entry
             * whose value ExifTool renders as nested structure.
             */
            val valueMatch = entryRegex.find(line)

            val subDirectoryMatch = valueMatch ?: subDirectoryRegex.find(line)

            if (valueMatch == null && subDirectoryMatch == null)
                continue

            /* The signature line follows the value line directly. */
            val tagMatch = tagRegex.find(lines[index + 1])

            val tag = assertNotNull(tagMatch, "The entry '$line' has no Tag line.")

            if (valueMatch != null) {

                entries.add(
                    TagEntry(
                        name = valueMatch.groupValues[1],
                        signature = tag.groupValues[1],
                        type = tag.groupValues[2],
                        value = valueMatch.groupValues[2],
                        isSubDirectory = false
                    )
                )

            } else {

                entries.add(
                    TagEntry(
                        name = subDirectoryMatch!!.groupValues[1],
                        signature = tag.groupValues[1],
                        type = tag.groupValues[2],
                        value = null,
                        isSubDirectory = true
                    )
                )
            }
        }

        return entries
    }

    private fun compareValue(
        id: Int,
        tagEntry: TagEntry,
        kimValue: String?
    ) {

        val field = "media_$id ${tagEntry.name} (${tagEntry.type})"

        /*
         * SubDirectory entries carry structured data Kim reports as a
         * single undecoded entry.
         */
        if (tagEntry.isSubDirectory) {

            assertEquals(
                expected = null,
                actual = kimValue,
                message = "$field must stay undecoded"
            )

            return
        }

        when (tagEntry.type) {

            "text", "desc", "mluc", "sig" ->
                assertEquals(
                    expected = tagEntry.value,
                    actual = kimValue,
                    message = field
                )

            "XYZ ", "sf32" -> {

                val expected = tagEntry.value!!.split(' ').map { it.trim() }

                val actual = assertNotNull(
                    kimValue,
                    "$field must be decoded"
                ).split(' ').map { component -> toExifToolStyle(component.toDouble()) }

                assertEquals(
                    expected = expected,
                    actual = actual,
                    message = field
                )
            }

            "date" -> {

                val fields = tagEntry.value!!.split(' ').map { it.trim().toInt() }

                val expected = fields[0].toString().padStart(4, '0') + "-" +
                    fields[1].toString().padStart(2, '0') + "-" +
                    fields[2].toString().padStart(2, '0') + " " +
                    fields[3].toString().padStart(2, '0') + ":" +
                    fields[4].toString().padStart(2, '0') + ":" +
                    fields[5].toString().padStart(2, '0')

                assertEquals(
                    expected = expected,
                    actual = kimValue,
                    message = field
                )
            }

            else ->
                assertEquals(
                    expected = null,
                    actual = kimValue,
                    message = "$field must stay undecoded"
                )
        }
    }

    /**
     * Renders the number the way ExifTool renders ICC fixed-point
     * values: rounded to five decimal places, trailing zeros stripped.
     */
    private fun toExifToolStyle(value: Double): String {

        val rounded = (value * 100_000.0).roundToLong()

        val negative = rounded < 0

        val absolute = abs(rounded)

        val whole = absolute / 100_000L

        val fraction = (absolute % 100_000L).toString().padStart(5, '0').trimEnd('0')

        val digits = if (fraction.isEmpty()) "$whole" else "$whole.$fraction"

        return if (negative) "-$digits" else digits
    }

    /**
     * Renders the BCD-coded version short: 528 = 0x0210 is "2.1" and
     * 0x0430 is "4.3" - the trailing zero patch nibble is omitted.
     */
    private fun iccVersionOf(raw: String): String {

        val version = raw.trim().toInt()

        val major = (version shr 8) and 0xF
        val minor = (version shr 4) and 0xF
        val patch = version and 0xF

        return if (patch == 0) "$major.$minor" else "$major.$minor.$patch"
    }

    private companion object {

        private val headerRegex = Regex("""^\s*\|\s*\|\s*([A-Za-z]+) = (.*)$""")

        private val entryRegex = Regex("""^\s*\|\s*\d+\)\s+(\S+) = (.*)$""")

        private val subDirectoryRegex = Regex("""^\s*\|\s*\d+\)\s+(\S+) \(SubDirectory\) -->$""")

        private val tagRegex = Regex("""^\s*\|\s*- Tag '([^']*)' \(\d+ bytes, type '([^']*)'\)""")
    }
}
