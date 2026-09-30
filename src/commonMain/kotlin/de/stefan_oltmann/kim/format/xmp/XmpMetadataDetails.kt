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
package de.stefan_oltmann.kim.format.xmp

import de.stefan_oltmann.kim.model.MetadataSummary
import de.stefan_oltmann.xmp.XMPMeta

/**
 * The result of reading an XMP packet: the flattened [metadata] summary plus the facts
 * about the packet the summary cannot express.
 *
 * The summary flattens "the packet carries no keywords" and "the packet carries an empty
 * keyword list" into the same empty set, and an explicit written "not flagged" into the
 * same `false` as silence. Tools that merge a sidecar against other sources need that
 * distinction: a sidecar that says nothing about a field must not overwrite what another
 * source carries, while a sidecar that carries the field wins even when it is empty.
 */
public data class XmpMetadataDetails(

    /**
     * The flattened metadata of the packet, like [XmpReader.readMetadata] reports it.
     */
    val metadata: MetadataSummary,

    /**
     * The parsed packet the summary was built from, for readers that need facts outside
     * the summary - for example properties in application-specific namespaces.
     */
    val xmpMeta: XMPMeta,

    /**
     * True when the packet carries a flag property in any schema Kim reads the flag from
     * (xmpDM, ACDSee, Mylio, Narrative), even when it marks the photo as not flagged.
     * False when the packet says nothing about flagging.
     */
    val carriesFlag: Boolean,

    /**
     * True when the packet carries a dc:subject keyword list, even an empty one.
     * False when the packet says nothing about keywords.
     */
    val carriesKeywords: Boolean,

    /**
     * True when the packet carries an Iptc4xmpExt:PersonInImage list, even an empty one.
     * False when the packet says nothing about persons.
     */
    val carriesPersonsInImage: Boolean,

    /**
     * True when the packet carries an mwg-rs region list, even an empty one.
     * False when the packet says nothing about face regions.
     */
    val carriesFaces: Boolean
)
