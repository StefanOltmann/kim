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
package de.stefan_oltmann.kim.format.jpeg

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.common.toUInt16
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.markerDescription
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Regression test for the JPEG segment analysis based on a fixed set of
 * test files.
 *
 * The segment list of each original and modified test image is committed as
 * a text dump in the test data folders. Missing or outdated dumps are
 * written to build/, so a golden can be refreshed from there after an
 * intentional change.
 */
class JpegSegmentAnalyzerTest {

    /**
     * Analyzes every test JPEG and compares each result with the committed
     * text dump.
     */
    @Test
    fun testFindSegmentInfos() {

        val problems = mutableListOf<String>()

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            /* Media 44, 45 and 47 are broken files without consistent segments. */
            if (brokenJpegIds.contains(index))
                continue

            val bytes = KimTestData.getBytesOf(index)

            compareWithGolden(
                resourcePath = RESOURCE_PATH,
                goldenFileName = "media_${index}_segments.txt",
                actualText = format(analyze(bytes)),
                problems = problems
            )
        }

        if (problems.isNotEmpty())
            fail(problems.joinToString("\n"))
    }

    /**
     * Analyzes every modified test JPEG and compares each result with the
     * committed text dump, detecting regressions in the rewrite pipeline.
     */
    @Test
    fun testFindSegmentInfosOnModifiedFiles() {

        val problems = mutableListOf<String>()

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            if (!KimTestData.hasModifiedBytesOf(index))
                continue

            /* The modified version of the broken file is broken as well. */
            if (brokenModifiedJpegIds.contains(index))
                continue

            val bytes = KimTestData.getModifiedBytesOf(index)

            compareWithGolden(
                resourcePath = MODIFIED_RESOURCE_PATH,
                goldenFileName = "media_${index}_modified_segments.txt",
                actualText = format(analyze(bytes)),
                problems = problems
            )
        }

        if (problems.isNotEmpty())
            fail(problems.joinToString("\n"))
    }

    /**
     * Analyzes the given JPEG bytes and checks the segment integrity.
     */
    private fun analyze(bytes: ByteArray): List<JpegSegmentAnalyzer.JpegSegmentInfo> {

        val segmentInfos = JpegSegmentAnalyzer.findSegmentInfos(ByteArrayByteReader(bytes))

        /* Integrity check to prevent persistance of wrong results. */
        assertEquals(
            expected = bytes.size - missingEoiBytes(bytes),
            actual = segmentInfos.sumOf { it.length },
            message = "Sum of lengths should match bytes size."
        )

        /* Check that markers are correct. */
        for ((offset, marker, _) in segmentInfos) {

            val markerAtOffset = byteArrayOf(
                bytes[offset],
                bytes[offset + 1]
            ).toUInt16(JPEG_BYTE_ORDER)

            assertEquals(
                expected = marker,
                actual = markerAtOffset,
                message = "Unexpected marker."
            )
        }

        return segmentInfos
    }

    /**
     * Formats the given segment infos as a text dump.
     */
    private fun format(segmentInfos: List<JpegSegmentAnalyzer.JpegSegmentInfo>): String =
        segmentInfos
            .joinToString("\n", transform = ::formatSegmentInfo)

    /**
     * Formats one segment line with the byte range and the marker description.
     */
    private fun formatSegmentInfo(segmentInfo: JpegSegmentAnalyzer.JpegSegmentInfo): String {

        val end = segmentInfo.offset + segmentInfo.length

        return "[${segmentInfo.offset.toString().padStart(8)} " +
            "- ${end.toString().padStart(8)} ] " +
            "${markerDescription(segmentInfo.marker)} " +
            "[${segmentInfo.length} bytes]"
    }

    /**
     * Compares the actual text dump with the committed golden, writing the
     * actual text to build/ and reporting problems instead of failing fast.
     */
    private fun compareWithGolden(
        resourcePath: String,
        goldenFileName: String,
        actualText: String,
        problems: MutableList<String>
    ) {

        val resource = Resource("$resourcePath/$goldenFileName")

        if (!resource.exists()) {

            writeActual(goldenFileName, actualText)

            problems.add("$goldenFileName does not exist.")

            return
        }

        val expectedText = resource.readBytes().decodeToString()

        if (expectedText != actualText) {

            writeActual(goldenFileName, actualText)

            problems.add("$goldenFileName is different.")
        }
    }

    /**
     * Writes the actual text dump to build/ for copying into the test data.
     */
    private fun writeActual(goldenFileName: String, actualText: String) {

        Path("build/$goldenFileName")
            .writeBytes(actualText.encodeToByteArray())
    }

    /**
     * Returns the number of trailing bytes when the file lacks an EOI marker.
     */
    private fun missingEoiBytes(bytes: ByteArray): Int {

        val lastBytes = bytes.copyOfRange(
            bytes.size - JpegConstants.EOI.size,
            bytes.size
        )

        return if (lastBytes.contentEquals(JpegConstants.EOI)) 0 else JpegConstants.EOI.size
    }

    private companion object {

        const val RESOURCE_PATH: String = "de/stefan_oltmann/kim/testdata/txt"

        /* The modified files and their dumps live in the modified test data folder. */
        const val MODIFIED_RESOURCE_PATH: String = "de/stefan_oltmann/kim/testdata/modified"

        /* Media 44, 45 and 47 are broken files. */
        val brokenJpegIds: Set<Int> = setOf(44, 45, 47)

        /* Media 45 is a broken modified file. */
        val brokenModifiedJpegIds: Set<Int> = setOf(45)
    }
}
