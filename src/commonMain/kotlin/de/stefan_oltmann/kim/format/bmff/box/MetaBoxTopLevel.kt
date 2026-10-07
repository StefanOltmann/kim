/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2026 Ramon Bouckaert
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
package de.stefan_oltmann.kim.format.bmff.box

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.MetadataOffset
import de.stefan_oltmann.kim.common.MetadataType
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.format.bmff.PayloadSource
import de.stefan_oltmann.kim.format.bmff.BMFFConstants
import de.stefan_oltmann.kim.format.bmff.Extent
import de.stefan_oltmann.kim.format.bmff.MetadataItem

/**
 * ISO/IEC 14496-12 meta box
 *
 * The Meta Box is a container for several metadata boxes. This class represents a top-level Meta
 * Box that is not a sub-box of some other box.
 */
public class MetaBoxTopLevel internal constructor(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payloadSource: PayloadSource,
    depth: Int = 0
) : MetaBox(offset, size, largeSize, payloadSource, depth), BoxContainer {

    /* Mandatory boxes in top-level META */
    public val primaryItemBox: PrimaryItemBox =
        boxes.filterIsInstance<PrimaryItemBox>().firstOrNull()
            ?: throw ImageReadException("Illegal ISOBMFF: meta has no pitm box.")

    public val itemInfoBox: ItemInformationBox =
        boxes.filterIsInstance<ItemInformationBox>().firstOrNull()
            ?: throw ImageReadException("Illegal ISOBMFF: meta has no iinf box.")

    public val itemLocationBox: ItemLocationBox =
        boxes.filterIsInstance<ItemLocationBox>().firstOrNull()
            ?: throw ImageReadException("Illegal ISOBMFF: meta has no iloc box.")

    /*
     * Extents with an idat-relative construction method cannot be
     * resolved, because the idat box is not supported. Interpreting
     * them as absolute offsets would misread image bytes.
     */
    private val resolvableExtents: List<Extent>
        get() = itemLocationBox.extents.filter { it.constructionMethod == 0 }

    public val referencesXmp: Boolean
        get() = findMetadataItems().any { it.type == MetadataType.XMP }

    /**
     * Returns the resolvable metadata items with all of their extents.
     *
     * An item may be fragmented into several extents, so the returned
     * groups must be read extent by extent and concatenated before they
     * can be parsed as one stream. Items are ordered by position, and
     * the extents of each item are ordered by position as well.
     */
    public constructor(
        offset: Long,
        size: Long,
        largeSize: Long?,
        payload: ByteArray,
        depth: Int = 0
    ) : this(offset, size, largeSize, PayloadSource.of(payload), depth)
    public fun findMetadataItems(): List<MetadataItem> {

        /*
         * An infe-declared EXIF or XMP item without resolvable extents
         * is real metadata in a layout this reader cannot resolve.
         * Per the strict read policy it must fail the read instead of
         * silently vanishing from the result - sidecar writers would
         * otherwise report "no metadata" for files that carry it.
         */
        for (itemInfo in itemInfoBox.map.values) {

            val isMetadataType = itemInfo.itemType == BMFFConstants.ITEM_TYPE_EXIF ||
                itemInfo.itemType == BMFFConstants.ITEM_TYPE_MIME

            if (!isMetadataType)
                continue

            val hasResolvableExtent = resolvableExtents.any { it.itemId == itemInfo.itemId }

            if (!hasResolvableExtent)
                throw ImageReadException(
                    "The ${itemInfo.itemType} item ${itemInfo.itemId} has no " +
                        "resolvable extents (idat-relative or missing)."
                )
        }

        /* Preserves the file order of the items. */
        val extentsByItemId = LinkedHashMap<Int, MutableList<MetadataOffset>>()

        for (extent in resolvableExtents) {

            val itemInfo = itemInfoBox.map[extent.itemId] ?: continue

            val type = when (itemInfo.itemType) {

                BMFFConstants.ITEM_TYPE_EXIF -> MetadataType.EXIF

                BMFFConstants.ITEM_TYPE_MIME -> MetadataType.XMP

                else -> continue
            }

            extentsByItemId.getOrPut(extent.itemId) { mutableListOf() }.add(
                MetadataOffset(
                    type = type,
                    offset = extent.offset,
                    length = extent.length
                )
            )
        }

        return extentsByItemId.values.map { extents ->

            val sortedExtents = extents.sortedBy { it.offset }

            MetadataItem(
                type = sortedExtents.first().type,
                extents = sortedExtents
            )
        }.sortedBy { item -> item.extents.first().offset }
    }

    /**
     * The file positions of all metadata extents, sorted by position,
     * regardless of which item they belong to.
     */
    public fun findMetadataOffsets(): List<MetadataOffset> =
        findMetadataItems().flatMap { item -> item.extents }.sortedBy { it.offset }

    override fun toString(): String =
        "$type Box version=$version flags=${flags.toHex()} boxes=${boxes.map { it.type }}"

}
