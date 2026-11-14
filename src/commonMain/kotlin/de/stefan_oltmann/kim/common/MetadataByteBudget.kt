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
package de.stefan_oltmann.kim.common

/*
 * File-controlled metadata payloads buffer in memory during parsing, so
 * a payload beyond this limit is hostile input rather than a legitimate
 * file: no real photo or video carries metadata-sized chunks anywhere
 * near it. Every format reader enforces the same budget, so the paths
 * cannot drift apart.
 */
internal const val MAX_METADATA_BYTE_BUDGET: Int = 16 * 1024 * 1024

/*
 * The text chunk types are the only ones that occur multiple times per
 * file and still decompress, so their decompressed sizes accumulate
 * across the read: every zTXt and iTXt decompression of one read draws
 * from this budget, and the chunk that crosses it fails the read
 * mid-inflate. The single-occurrence decompressing chunks are bounded
 * by MAX_DECOMPRESSED_BYTE_COUNT alone.
 */
internal const val MAX_DECOMPRESSED_TEXT_BYTES: Int = 16 * 1024 * 1024
