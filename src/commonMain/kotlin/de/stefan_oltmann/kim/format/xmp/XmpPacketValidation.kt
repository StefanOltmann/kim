/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2026 Ramon Bouckaert
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
package de.stefan_oltmann.kim.format.xmp

import de.stefan_oltmann.kim.common.ImageReadException

/**
 * The element every XMP packet starts with. A packet without it cannot
 * be parsed by XMPMetaFactory, so it would fail the update path anyway.
 */
private const val XMP_PACKET_START_TAG = "<x:xmpmeta"

/**
 * The element every complete XMP packet ends with. A packet that has the
 * opening element but not the closing one is truncated.
 */
private const val XMP_PACKET_END_TAG = "</x:xmpmeta>"

/**
 * Fails the read when a chunk carries no packet or a truncated one.
 *
 * A packet must contain both the opening and the closing element: a
 * packet cut off between them is incomplete, and silently returning it
 * would hand sidecar writers metadata that only looks complete while
 * the update path fails on the broken bytes. Whether a complete packet
 * parses as full XMP is decided by the conversion and update layers -
 * the raw bytes stay fully available on the metadata object by design
 * (see the derived-projections section in the
 * [de.stefan_oltmann.kim.Kim] documentation).
 *
 * A NULL packet means the file has no XMP at all, which is fine.
 *
 * @param xmp The packet read from the file, or NULL when there is none.
 * @param sourceDescription Names the container the packet came from
 *        (for example "The WebP XMP chunk") in the error message.
 * @return The given packet, so callers can assign the result directly.
 */
internal fun requireValidXmpPacket(
    xmp: String?,
    sourceDescription: String
): String? {

    if (xmp != null && !xmp.contains(XMP_PACKET_START_TAG))
        throw ImageReadException("$sourceDescription has no <x:xmpmeta> element.")

    if (xmp != null && !xmp.contains(XMP_PACKET_END_TAG))
        throw ImageReadException(
            "$sourceDescription has a truncated <x:xmpmeta> packet - " +
                "the closing element is missing."
        )

    return xmp
}
