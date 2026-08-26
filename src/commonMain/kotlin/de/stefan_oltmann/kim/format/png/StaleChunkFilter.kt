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
package de.stefan_oltmann.kim.format.png

/**
 * Decides whether a trailing chunk behind the image data is a stale
 * duplicate of metadata that a rewrite has replaced or removed.
 *
 * Scoping by content keeps unrelated trailing chunks - user comments or
 * the modification time - intact on updates, matching the precise,
 * keyword-scoped removal that applies to the chunks before the image data.
 */
internal fun interface StaleChunkFilter {

    /**
     * Returns whether the chunk is stale and must not be copied to the
     * output. The keyword is only provided for text chunk types and is
     * NULL for everything else.
     */
    fun isStale(chunkType: PngChunkType, keyword: String?): Boolean

    companion object {

        /**
         * Drops every recognized metadata chunk regardless of content,
         * which is the semantics of deleteMetadata.
         */
        val ALL_METADATA: StaleChunkFilter = StaleChunkFilter { chunkType, _ ->
            isMetadataChunkType(chunkType)
        }

        fun isMetadataChunkType(chunkType: PngChunkType): Boolean =
            chunkType == PngChunkType.EXIF ||
                chunkType == PngChunkType.TEXT ||
                chunkType == PngChunkType.ZTXT ||
                chunkType == PngChunkType.ITXT ||
                chunkType == PngChunkType.TIME
    }
}
