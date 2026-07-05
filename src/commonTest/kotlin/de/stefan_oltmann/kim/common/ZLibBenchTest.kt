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
package de.stefan_oltmann.kim.common

import com.goncalossilva.resources.Resource
import kotlin.test.Test
import kotlin.time.measureTimedValue

class ZLibBenchTest {

    @Test
    fun testRoundtripWithLongText() {

        val testString = Resource(RESOURCE_PATH).readBytes().decodeToString()

        val (compressed2, compressDuration2) = measureTimedValue { compress(testString) }

        val (_, decompressDuration2) = measureTimedValue { decompress(compressed2) }

        val (compressed1, compressDuration1) = measureTimedValue { zlibCompress(testString) }

        val (_, decompressDuration1) = measureTimedValue { zlibDecompress(compressed1) }

        println("""
            Native inflate: ${compressDuration2.inWholeMilliseconds}
            Native deflate: ${decompressDuration2.inWholeMilliseconds}
            Kompress inflate: ${compressDuration1.inWholeMilliseconds}
            Kompress deflate: ${decompressDuration1.inWholeMilliseconds}
        """.trimIndent())
    }

    companion object {

        private const val RESOURCE_PATH: String = "de/stefan_oltmann/kim/testdata/alice_in_wonderland.txt"
    }
}
