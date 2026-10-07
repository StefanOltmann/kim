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
package de.stefan_oltmann.kim.format.bmff

import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader

/**
 * The payload bytes of a box, materialized only when they are read.
 *
 * Payload that is already buffered inside a byte array is carried as a
 * window into that array, so the children of a container box parse from
 * the parent's buffer without copying the payload for every nesting
 * level - the payload array exists only if something actually reads it.
 */
internal class PayloadSource private constructor(
    private val bytes: ByteArray?,
    private val windowArray: ByteArray?,
    private val windowOffset: Int,
    val length: Int
) {

    /**
     * The payload bytes. For a window this copies the region out of the
     * parent buffer, like the eager read did before.
     */
    fun bytes(): ByteArray =
        bytes ?: windowArray!!.copyOfRange(windowOffset, windowOffset + length)

    /**
     * A reader over the payload. A window reads through the parent
     * buffer, so no copy happens for the child box walk.
     */
    fun reader(): ByteReader =
        bytes?.let { ByteArrayByteReader(it) }
            ?: ByteArrayByteReader(windowArray!!, windowOffset, windowOffset + length)

    companion object {

        fun of(bytes: ByteArray): PayloadSource =
            PayloadSource(bytes, null, 0, bytes.size)

        fun ofWindow(array: ByteArray, offset: Int, length: Int): PayloadSource =
            PayloadSource(null, array, offset, length)
    }
}
