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
package de.stefan_oltmann.kim.format.jpeg.xmp

/**
 * The result of splitting an oversized XMP packet for Adobe extended XMP
 * writing: a main packet that stays within one APP1 segment and references
 * the extended data by GUID, plus ready-made payloads for the extension
 * segments.
 */
internal data class PartitionedXmp(
    val mainPacketXml: String,
    val extensionSegmentPayloads: List<ByteArray>
)
