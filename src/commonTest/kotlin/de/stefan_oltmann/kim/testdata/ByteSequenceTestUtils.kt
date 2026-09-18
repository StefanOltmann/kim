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

/**
 * Counts how often the needle bytes occur in the array. Overlapping
 * matches are not counted twice.
 */
internal fun ByteArray.countOccurrences(needle: String): Int {

    val needleBytes = needle.encodeToByteArray()

    var count = 0

    for (index in 0..size - needleBytes.size) {

        var matches = true

        for (needleIndex in needleBytes.indices)
            if (this[index + needleIndex] != needleBytes[needleIndex]) {

                matches = false

                break
            }

        if (matches)
            count++
    }

    return count
}
