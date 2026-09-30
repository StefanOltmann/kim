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
package de.stefan_oltmann.kim.kotlinx

import de.stefan_oltmann.kim.common.ImageReadException
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertFailsWith

class KimKotlinxTest {

    /**
     * A file that cannot be opened is an error, not "no metadata" - like
     * the JVM, Android and Apple facades, which throw for a missing file
     * instead of reporting an empty read result.
     */
    @Test
    fun testReadMetadataMissingFileThrows() {

        assertFailsWith<ImageReadException> {
            KimKotlinx.readMetadata(Path("does-not-exist.jpg"))
        }
    }
}
