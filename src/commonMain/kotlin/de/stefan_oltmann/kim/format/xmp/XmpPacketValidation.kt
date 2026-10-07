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
 * The element the recommended XMP packet envelope starts with.
 */
private const val XMP_PACKET_START_TAG = "<x:xmpmeta"

/**
 * The element the recommended XMP packet envelope ends with.
 */
private const val XMP_PACKET_END_TAG = "</x:xmpmeta>"

/**
 * A bare RDF root, which the XMP specification allows as an alternative
 * to the recommended envelope. xmpcore parses such packets, so they are
 * complete, readable content.
 */
private const val RDF_ROOT_START_TAG = "<rdf:RDF"

/**
 * The element a bare RDF packet ends with.
 */
private const val RDF_ROOT_END_TAG = "</rdf:RDF>"

/**
 * Fails the read when a chunk carries no packet or a truncated one.
 *
 * A packet must contain both the opening and the closing element of its
 * envelope form - the recommended `x:xmpmeta` wrapper or a bare
 * `rdf:RDF` root, which the XMP specification also allows and xmpcore
 * parses. A packet cut off between the opening and the closing element
 * is incomplete, and silently returning it would hand sidecar writers
 * metadata that only looks complete while the update path fails on the
 * broken bytes. Whether a complete packet parses as full XMP is decided
 * by the conversion and update layers - the raw bytes stay fully
 * available on the metadata object by design (see the
 * derived-projections section in the [de.stefan_oltmann.kim.Kim]
 * documentation).
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

    if (xmp == null)
        return null

    val hasEnvelope = xmp.contains(XMP_PACKET_START_TAG)

    /*
     * The start marker decides which envelope form the packet uses and
     * therefore which closing element completeness requires.
     */
    val startTag = if (hasEnvelope) XMP_PACKET_START_TAG else RDF_ROOT_START_TAG
    val endTag = if (hasEnvelope) XMP_PACKET_END_TAG else RDF_ROOT_END_TAG

    if (!xmp.contains(startTag))
        throw ImageReadException(
            "$sourceDescription has neither an <x:xmpmeta> envelope nor " +
                "a bare <rdf:RDF> root - it is not XMP."
        )

    if (!xmp.contains(endTag))
        throw ImageReadException(
            "$sourceDescription has a truncated XMP packet - " +
                "the closing element is missing."
        )

    return xmp
}
