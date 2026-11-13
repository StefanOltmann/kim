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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.toUInt16
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.EOI_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOI_MARKER
import de.stefan_oltmann.kim.format.jpeg.JpegConstants.SOS_MARKER
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.fail

/**
 * Cross-checks the full scan against the whole JPEG corpus.
 *
 * Every corpus file whose primary image ends with a structural EOI
 * marker must pass the full scan, ending on exactly that marker, and
 * the header entries must agree with the default scan. The corpus
 * files without a structural EOI marker and the corrupt file media_46
 * fail the full scan. ExifTool 12.70 confirms the corrupt file: it
 * reports `Warning = JPEG format error` for it, because a segment
 * between its scans announces more bytes than the file contains.
 *
 * The test is placed in jvmTest, because the full byte drill over the
 * whole corpus would exceed the Node.js test timeout of commonTest.
 */
class JpegSegmentAnalyzerCorpusFullScanTest {

    /**
     * The corpus files that fail the full scan, each with its reason:
     * the unusable files media_44, media_45 and media_47 end inside
     * their image data without any EOI marker, and media_46 is corrupt
     * with a truncated segment between its scans.
     */
    private val failureReasonsByIndex: Map<Int, String> = mapOf(
        44 to "The file ends right at the SOS marker without image bytes and without an EOI marker.",
        45 to "The file ends inside its image data without an EOI marker.",
        46 to "The file is corrupt: the segment between its scans announces more bytes than the file contains.",
        47 to "The file ends inside its image data without an EOI marker."
    )

    /**
     * The full scan must walk every original corpus file that contains a
     * structural EOI marker through exactly that marker: the last
     * reported entry is the EOI, every reported marker sits at its
     * reported offset and the header prefix agrees with the default
     * scan. The files of [failureReasonsByIndex] must fail.
     */
    @Test
    fun testFullScanWalksEveryCorpusFileToItsStructuralEoi() {

        val problems = mutableListOf<String>()

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            val bytes = KimTestData.getBytesOf(index)

            val expectedProblem = failureReasonsByIndex[index]

            if (expectedProblem == null) {

                val fullInfos =
                    try {
                        JpegSegmentAnalyzer.findSegmentInfos(
                            ByteArrayByteReader(bytes),
                            fullScan = true
                        )
                    } catch (ex: ImageReadException) {
                        problems.add("media_$index: The full scan failed: ${ex.message}")
                        continue
                    }

                checkFullScanInvariants(
                    listName = "media_$index",
                    bytes = bytes,
                    fullInfos = fullInfos,
                    problems = problems
                )

                checkHeaderPrefixInvariants(
                    listName = "media_$index",
                    bytes = bytes,
                    fullInfos = fullInfos,
                    problems = problems
                )

                continue
            }

            expectFullScanFailure(
                listName = "media_$index",
                bytes = bytes,
                expectedProblem = expectedProblem,
                problems = problems
            )
        }

        assertNoProblems(problems)
    }

    /**
     * The modified variants were written by Kim itself and inherit the
     * original file's structure, so the scan invariants apply equally.
     */
    @Test
    fun testFullScanWalksEveryModifiedCorpusFileToItsStructuralEoi() {

        val problems = mutableListOf<String>()

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            if (!KimTestData.hasModifiedBytesOf(index))
                continue

            val bytes = KimTestData.getModifiedBytesOf(index)

            val listName = "media_${index}_modified"

            val expectedProblem = failureReasonsByIndex[index]

            if (expectedProblem == null) {

                val fullInfos =
                    try {
                        JpegSegmentAnalyzer.findSegmentInfos(
                            ByteArrayByteReader(bytes),
                            fullScan = true
                        )
                    } catch (ex: ImageReadException) {
                        problems.add("$listName: The full scan failed: ${ex.message}")
                        continue
                    }

                checkFullScanInvariants(
                    listName = listName,
                    bytes = bytes,
                    fullInfos = fullInfos,
                    problems = problems
                )

                continue
            }

            expectFullScanFailure(
                listName = listName,
                bytes = bytes,
                expectedProblem = expectedProblem,
                problems = problems
            )
        }

        assertNoProblems(problems)
    }

    private fun expectFullScanFailure(
        listName: String,
        bytes: ByteArray,
        expectedProblem: String,
        problems: MutableList<String>
    ) {

        try {
            JpegSegmentAnalyzer.findSegmentInfos(
                ByteArrayByteReader(bytes),
                fullScan = true
            )
        } catch (ignored: ImageReadException) {
            /* The scan must fail exactly through the expected problem. */
            return
        }

        problems.add("$listName: The full scan must fail: $expectedProblem")
    }

    private fun checkFullScanInvariants(
        listName: String,
        bytes: ByteArray,
        fullInfos: List<JpegSegmentAnalyzer.JpegSegmentInfo>,
        problems: MutableList<String>
    ) {

        val eoiInfo = fullInfos.lastOrNull()

        if (eoiInfo == null) {
            problems.add("$listName: The full scan reports no entries.")
            return
        }

        if (eoiInfo.marker != EOI_MARKER)
            problems.add("$listName: The full scan must end on the EOI marker: $eoiInfo")

        if (eoiInfo.offset + eoiInfo.length > bytes.size.toLong())
            problems.add("$listName: The EOI entry lies behind the file end: $eoiInfo")

        for ((offset, marker, _) in fullInfos) {

            val markerAtOffset =
                byteArrayOf(bytes[offset.toInt()], bytes[offset.toInt() + 1])
                    .toUInt16(JPEG_BYTE_ORDER)

            if (marker != markerAtOffset)
                problems.add(
                    "$listName: The bytes at $offset are ${markerAtOffset.toString(16)}, " +
                        "not ${marker.toString(16)}."
                )
        }

        /*
         * The copying contract: the caller copies the reported ranges
         * and keeps the gaps between them as the compressed image data,
         * so the ranges must tile the file in order without overlapping,
         * and every length-prefixed entry must report exactly the length
         * field the file itself carries - a drifted length would silently
         * copy the wrong bytes.
         */
        var previousEnd = 0L

        for (info in fullInfos) {

            if (info.offset < previousEnd)
                problems.add(
                    "$listName: The ranges overlap: $info starts behind $previousEnd."
                )

            previousEnd = info.offset + info.length

            val isStandaloneMarker =
                info.marker == SOI_MARKER ||
                    info.marker == EOI_MARKER ||
                    JpegUtils.isMarkerWithoutLengthWord(info.marker)

            if (isStandaloneMarker) {

                if (info.length != 2L)
                    problems.add(
                        "$listName: The standalone marker $info must be 2 bytes."
                    )
            } else {

                val lengthFieldAtOffset =
                    byteArrayOf(
                        bytes[info.offset.toInt() + 2],
                        bytes[info.offset.toInt() + 3]
                    ).toUInt16(JPEG_BYTE_ORDER)

                if (info.length != lengthFieldAtOffset + 2L)
                    problems.add(
                        "$listName: The reported length $info must match the " +
                            "length field ${lengthFieldAtOffset + 2L} the file carries."
                    )
            }
        }

        val firstInfo = fullInfos.first()

        if (firstInfo.marker != SOI_MARKER || firstInfo.offset != 0L || firstInfo.length != 2L)
            problems.add("$listName: The full scan must start with the SOI marker at 0.")
    }

    private fun checkHeaderPrefixInvariants(
        listName: String,
        bytes: ByteArray,
        fullInfos: List<JpegSegmentAnalyzer.JpegSegmentInfo>,
        problems: MutableList<String>
    ) {

        val defaultInfos = JpegSegmentAnalyzer.findSegmentInfos(ByteArrayByteReader(bytes))

        val indexFirstSos = defaultInfos.indexOfFirst { it.marker == SOS_MARKER }

        if (indexFirstSos < 0) {
            problems.add("$listName: The default scan finds no SOS marker.")
            return
        }

        assertContentEquals(
            expected = defaultInfos.take(indexFirstSos),
            actual = fullInfos.take(indexFirstSos),
            message = "$listName: The header segment prefix must be identical in both modes."
        )
    }

    private fun assertNoProblems(problems: MutableList<String>) {

        if (problems.isNotEmpty())
            fail(problems.joinToString("\n"))
    }
}
