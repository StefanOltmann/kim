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
package de.stefan_oltmann.kim.output

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression test: the growth arithmetic of the in-memory writer ran in
 * Int space, so a buffer that grew past ~1.5 GiB computed a negative
 * capacity and crashed with NegativeArraySizeException - the arithmetic
 * must run in Long space and cap at the largest array size the JVM
 * guarantees.
 */
class ByteArrayByteWriterOverflowTest {

    @Test
    fun testGrowthPastTwoGiBDoesNotOverflow() {

        /*
         * 1_065_741_824 + 8_000_000 doubles to exactly 2^31 - one past the
         * Int maximum. The pre-fix Int arithmetic wrapped that to a negative
         * capacity and threw NegativeArraySizeException, while the Long-space
         * computation must return the cap instead. Testing the computation
         * directly keeps this guard free of the ~3 GB allocation the writer
         * itself would need to cross the boundary.
         */
        assertEquals(
            expected = Int.MAX_VALUE - 2,
            actual = computeGrownCapacity(1_065_741_824, 8_000_000)
        )
    }
}
