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
 * Fails the read when a chunk claims to carry XMP but holds no packet.
 *
 * A packet that starts correctly is accepted here even when it is
 * truncated or otherwise corrupt: the raw bytes stay fully available on
 * the metadata object by design (see the derived-projections section in
 * the [de.stefan_oltmann.kim.Kim] documentation), the update path fails
 * loudly when it cannot parse them, and the summary conversion decides
 * per call whether broken XMP is an error or an omission.
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

    return xmp
}
