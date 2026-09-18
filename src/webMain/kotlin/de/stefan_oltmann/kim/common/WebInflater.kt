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

/**
 * The incremental inflater of the web platform, wrapped so the shared
 * decompression algorithm can drive it without knowing pako's interop.
 */
internal interface WebInflater {

    /** Whether the inflater ended the current zlib member. */
    val ended: Boolean

    /** The bytes the inflater produced for the current member so far. */
    val totalOut: Int

    /** The finished member payload, or NULL while the member is open. */
    val result: ByteArray?

    /** Feeds the next input chunk to the inflater. */
    fun push(chunk: ByteArray)

    /** Signals the end of input, which finalizes an open member. */
    fun pushEnd()
}

/**
 * The chunk size the input is fed in, so the accumulated output can be
 * checked between pushes.
 */
internal const val INFLATE_INPUT_CHUNK_SIZE: Int = 8192

/**
 * Decompresses concatenated zlib members with a hard output budget.
 *
 * The input is fed in bounded chunks, so the accumulated output can be
 * checked between pushes. A whole-buffer inflate grows the full output
 * in memory before any size check can run, which lets a few KB of
 * hostile input exhaust the memory on this target.
 *
 * Concatenated zlib members are legal: when pako ends a member while
 * input remains, a fresh inflater continues with the rest.
 *
 * Foreign inflate errors are not caught here; each target wraps them
 * into the ImageReadException its caller expects.
 */
internal fun decompressBytesWithBudget(
    byteArray: ByteArray,
    maxOutputByteCount: Int,
    newInflater: () -> WebInflater
): ByteArray {

    val collected = mutableListOf<ByteArray>()

    var budgetUsed = 0

    var offset = 0

    while (offset < byteArray.size) {

        val inflater = newInflater()

        var memberEnded = false

        while (offset < byteArray.size && !memberEnded) {

            val chunkLength = minOf(INFLATE_INPUT_CHUNK_SIZE, byteArray.size - offset)

            inflater.push(byteArray.copyOfRange(offset, offset + chunkLength))

            offset += chunkLength

            /*
             * The budget check uses the member's own counter: pako
             * resets it when it continues with a concatenated member,
             * so it can only under-count within a single push, whose
             * output is bounded by the chunk size. The exact total is
             * enforced at the member boundary below.
             */
            if (budgetUsed + inflater.totalOut > maxOutputByteCount)
                throw ImageReadException(
                    "Decompressed data exceeds $maxOutputByteCount bytes."
                )

            memberEnded = inflater.ended
        }

        if (!memberEnded) {

            /* The input is exhausted - finalize the member. */
            inflater.pushEnd()

            if (budgetUsed + inflater.totalOut > maxOutputByteCount)
                throw ImageReadException(
                    "Decompressed data exceeds $maxOutputByteCount bytes."
                )
        }

        val result = inflater.result
            ?: throw ImageReadException("Failed to decompress the data.")

        budgetUsed += result.size

        if (budgetUsed > maxOutputByteCount)
            throw ImageReadException(
                "Decompressed data exceeds $maxOutputByteCount bytes."
            )

        collected.add(result)
    }

    val rawBytes = ByteArray(budgetUsed)

    var position = 0

    for (member in collected) {

        member.copyInto(rawBytes, position)

        position += member.size
    }

    return rawBytes
}
