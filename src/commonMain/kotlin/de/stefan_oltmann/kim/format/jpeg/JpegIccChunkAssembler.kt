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
package de.stefan_oltmann.kim.format.jpeg

import de.stefan_oltmann.kim.common.getRemainingBytes
import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.toUInt8
import de.stefan_oltmann.kim.format.jpeg.jfif.JFIFPieceSegment
import de.stefan_oltmann.kim.output.ByteArrayByteWriter

/**
 * The single source of the ICC chunk-assembly rules, so the read and
 * the delete decision cannot drift apart: the parser maps a failure to
 * an ImageReadException, the delete path maps it to "drop".
 *
 * A chunk sequence restarts at index 1: a tool that appended its own
 * profile without removing the first leaves a second complete run in
 * the file. Only the first run is returned - like ExifTool, which
 * reads the first profile - and a run beyond it is foreign-but-
 * preserved content the rewrite transfers byte-exact.
 */
internal object JpegIccChunkAssembler {

    /**
     * Walks the segments and returns the chunks of the first complete
     * ICC chunk run, an empty list when the file carries no ICC chunk,
     * or NULL when a segment's chunk header is structurally corrupt or
     * the first run is incomplete: the caller decides between failing
     * the read and dropping the profile.
     */
    fun collectFirstRun(
        segments: List<JFIFPieceSegment>
    ): List<JpegIccChunk>? {

        val runs = mutableListOf<MutableList<JpegIccChunk>>()

        var currentRun = mutableListOf<JpegIccChunk>()

        for ((position, segment) in segments.withIndex()) {

            if (segment.marker != JpegConstants.JPEG_APP2_MARKER)
                continue

            val segmentBytes = segment.segmentBytes

            if (!segmentBytes.startsWith(JpegConstants.ICC_PROFILE_IDENTIFIER))
                continue

            /*
             * The chunk header is the identifier plus the two sequence
             * bytes; a segment that ends inside it is corrupt metadata.
             */
            if (segmentBytes.size <
                JpegConstants.ICC_IDENTIFIER_LENGTH + JpegConstants.ICC_SEQUENCE_BYTE_COUNT
            )
                return null

            val index = segmentBytes[JpegConstants.ICC_CHUNK_SEQUENCE_OFFSET].toUInt8()
            val total = segmentBytes[JpegConstants.ICC_CHUNK_SEQUENCE_OFFSET + 1].toUInt8()

            if (total < 1 || index < 1 || index > total)
                return null

            if (index == 1 && currentRun.isNotEmpty()) {
                runs.add(currentRun)
                currentRun = mutableListOf()
            }

            currentRun.add(
                JpegIccChunk(
                    segmentPosition = position,
                    index = index,
                    total = total,
                    payload = segmentBytes.getRemainingBytes(
                        JpegConstants.ICC_IDENTIFIER_LENGTH + JpegConstants.ICC_SEQUENCE_BYTE_COUNT
                    )
                )
            )
        }

        if (currentRun.isNotEmpty())
            runs.add(currentRun)

        val firstRun = runs.firstOrNull() ?: return emptyList()

        val total = firstRun.first().total

        /*
         * The chunk sequence must be complete and consistent: a missing,
         * duplicated or out-of-range chunk means corrupt metadata.
         */
        if (firstRun.size != total ||
            firstRun.map { chunk -> chunk.index }.sorted() != (1..total).toList()
        )
            return null

        return firstRun
    }

    /**
     * Assembles the run's payloads in chunk order.
     */
    fun assemble(chunks: List<JpegIccChunk>): ByteArray {

        val profileBytes = ByteArrayByteWriter()

        chunks.sortedBy { chunk -> chunk.index }
            .forEach { chunk -> profileBytes.write(chunk.payload) }

        return profileBytes.toByteArray()
    }
}
