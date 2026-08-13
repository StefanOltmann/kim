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
package de.stefan_oltmann.kim.testdata

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.writeBytes
import kotlinx.io.files.Path
import kotlin.test.fail

/**
 * Verifies rewritten media bytes against the committed goldens in the
 * modified test data folder.
 *
 * Mismatching artifacts are written to build/, so a committed golden can be
 * refreshed from there after an intentional change.
 */
object ModifiedBytesVerifier {

    /**
     * Fails when the rewritten bytes or their metadata text dump differ from
     * the committed goldens, writing the actual artifacts to build/ for
     * inspection.
     *
     * The text dump is derived from the same bytes, but its committed golden
     * must stay in sync with the toString output, which this check enforces.
     */
    fun verify(index: Int, extension: String, actualBytes: ByteArray) {

        /*
         * Verify that the rewritten bytes match the committed golden.
         */

        val expectedBytes = KimTestData.getModifiedBytesOf(index)

        if (!expectedBytes.contentEquals(actualBytes)) {

            Path("build/media_${index}_modified.$extension")
                .writeBytes(actualBytes)

            fail("Bytes for test image #$index are different.")
        }

        /*
         * Verify that the rewritten metadata text dump matches the committed
         * golden.
         */

        val expectedToString = KimTestData.getModifiedToStringText(index)

        val actualToString = Kim.readMetadata(actualBytes).toString().encodeToByteArray()

        if (!expectedToString.contentEquals(actualToString)) {

            Path("build/media_${index}_modified.txt")
                .writeBytes(actualToString)

            fail("Text dump for test image #$index is different.")
        }
    }
}
