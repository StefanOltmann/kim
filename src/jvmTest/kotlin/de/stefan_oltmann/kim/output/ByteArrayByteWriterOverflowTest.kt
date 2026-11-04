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
import kotlin.test.fail

/**
 * Regression test: the growth arithmetic of the in-memory writer ran in
 * Int space, so a buffer that grew past ~1.5 GiB computed a negative
 * capacity and crashed with NegativeArraySizeException - the arithmetic
 * must run in Long space (jvmTest only: the allocation scale is beyond
 * what the JS and native targets can carry).
 */
class ByteArrayByteWriterOverflowTest {

    @Test
    fun testGrowthPastTwoGiBDoesNotOverflow() {

        /*
         * An initial capacity of 1_065_741_824 plus an 8 MB write drives
         * the growth computation (current + required) * 2 to exactly 2^31
         * - one past the Int maximum. The position is advanced to the
         * capacity minus the write size first, so the write actually
         * triggers the growth.
         */
        val writer = ByteArrayByteWriter(initialCapacity = 1_065_741_824)

        val chunk = ByteArray(1_000_000)

        repeat(1057) { writer.write(chunk) }

        writer.write(ByteArray(741_825))

        assertEquals(1_057_741_825, writer.writtenByteCount)

        /*
         * This write needs 1_057_741_825 + 8_000_000 bytes of capacity -
         * one byte more than the initial capacity, so it triggers the
         * growth whose arithmetic overflowed before the fix.
         */
        try {
            writer.write(ByteArray(8_000_000))
        } catch (ex: NegativeArraySizeException) {
            fail("The growth arithmetic overflowed the Int range.", ex)
        }

        assertEquals(1_065_741_825, writer.writtenByteCount)
    }
}
