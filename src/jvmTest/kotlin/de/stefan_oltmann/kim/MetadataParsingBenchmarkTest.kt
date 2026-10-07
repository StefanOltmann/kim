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

import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.time.TimeSource

/**
 * Measurement workload for a photo-folder scan: parses the metadata of
 * every corpus image repeatedly with the bytes cached in memory, like
 * an OS-cached folder. The numbers land in the test output - run this
 * class alone and grep for "BENCHMARK".
 */
class MetadataParsingBenchmarkTest {

    @Test
    fun parseCorpusRepeatedly() {

        val corpus = (1..KimTestData.TEST_MEDIA_COUNT)
            .map { id -> id to KimTestData.getBytesOf(id) }
            .toTypedArray()

        val totalBytes = corpus.sumOf { (_, bytes) -> bytes.size.toLong() }

        /* Warmup: JIT compilation, so the measured passes see steady state. */
        repeat(5) {
            for ((_, bytes) in corpus)
                runCatching { Kim.readMetadata(bytes) }
        }

        val passes = 30

        val start = TimeSource.Monotonic.markNow()

        repeat(passes) {
            for ((_, bytes) in corpus)
                runCatching { Kim.readMetadata(bytes) }
        }

        val elapsed = start.elapsedNow()

        val photoCount = passes.toLong() * corpus.size

        println("BENCHMARK photos=$photoCount bytes=${totalBytes * passes} " +
            "totalMs=${elapsed.inWholeMilliseconds} " +
            "usPerPhoto=${elapsed.inWholeMicroseconds / photoCount} " +
            "mbytesPerSecond=${totalBytes * passes / 1_048_576.0 / (elapsed.inWholeMilliseconds / 1000.0)}")

        for ((id, bytes) in corpus.sortedByDescending { it.second.size }.take(5)) {
            val singleStart = TimeSource.Monotonic.markNow()
            repeat(20) {
                runCatching { Kim.readMetadata(bytes) }
            }
            val perPhoto = singleStart.elapsedNow().inWholeMicroseconds / 20
            println("BENCHMARK largest media_$id size=${bytes.size} usPerPhoto=$perPhoto")
        }
    }
}
