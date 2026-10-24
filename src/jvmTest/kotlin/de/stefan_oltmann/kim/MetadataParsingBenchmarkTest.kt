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
package de.stefan_oltmann.kim

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Measurement workload for a photo-folder scan: parses the metadata of
 * every corpus image repeatedly. Each photo is read once and then kept
 * hot for its warmup and measurement passes, like an OS-cached folder;
 * only one photo's bytes are alive at a time, so the heap need stays at
 * the largest photo and the workload also runs on small CI runners. The
 * numbers land in the test output - run this class alone and grep for
 * "BENCHMARK".
 *
 * The walk also asserts the parse contract - exactly the known-broken
 * corpus files fail with an [ImageReadException], every other file
 * parses - so a format regression cannot hide behind the timing output.
 */
class MetadataParsingBenchmarkTest {

    @Test
    fun testCorpusParsesWhileBenchmarking() {

        val warmupPasses = 5

        val measuredPasses = 30

        val singlePhotoPasses = 20

        val mediaSizes = mutableListOf<Pair<Int, Long>>()

        var measured = Duration.ZERO

        for (id in 1..KimTestData.TEST_MEDIA_COUNT) {

            val bytes = KimTestData.getBytesOf(id)

            mediaSizes += id to bytes.size.toLong()

            /* Warmup: JIT compilation, so the measured passes see steady state. */
            repeat(warmupPasses) {
                runCatching { Kim.readMetadata(bytes) }
            }

            /*
             * Exactly the known-unreadable files must fail: the broken
             * JPEGs are rejected by the segment length validation, and
             * media_80 stores its metadata in brob containers that
             * cannot be read without brotli support (strict read policy).
             */
            if (id in KimTestData.brokenJpegIds || id == KimTestData.JXL_CONTAINER_COMPRESSED_INDEX)
                assertFailsWith<ImageReadException> {
                    Kim.readMetadata(bytes)
                }
            else
                assertNotNull(
                    Kim.readMetadata(bytes),
                    "media_$id no longer parses; a format parser may be broken."
                )

            val start = TimeSource.Monotonic.markNow()

            repeat(measuredPasses) {
                runCatching { Kim.readMetadata(bytes) }
            }

            measured += start.elapsedNow()
        }

        val totalBytes = mediaSizes.sumOf { (_, size) -> size }

        val photoCount = measuredPasses.toLong() * mediaSizes.size

        println(
            "BENCHMARK photos=$photoCount bytes=${totalBytes * measuredPasses} " +
                "totalMs=${measured.inWholeMilliseconds} " +
                "usPerPhoto=${measured.inWholeMicroseconds / photoCount} " +
                "mbytesPerSecond=${totalBytes * measuredPasses / 1_048_576.0 / (measured.inWholeMilliseconds / 1000.0)}"
        )

        for ((id, size) in mediaSizes.sortedByDescending { it.second }.take(5)) {

            val bytes = KimTestData.getBytesOf(id)

            val singleStart = TimeSource.Monotonic.markNow()

            repeat(singlePhotoPasses) {
                runCatching { Kim.readMetadata(bytes) }
            }

            val perPhoto = singleStart.elapsedNow().inWholeMicroseconds / singlePhotoPasses

            println("BENCHMARK largest media_$id size=$size usPerPhoto=$perPhoto")
        }
    }
}
