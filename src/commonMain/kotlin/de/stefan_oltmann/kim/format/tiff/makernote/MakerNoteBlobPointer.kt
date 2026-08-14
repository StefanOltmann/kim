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
package de.stefan_oltmann.kim.format.tiff.makernote

import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo

/**
 * A sub-directory of a MakerNote that is stored as a binary blob.
 *
 * The fields are stored at tag * [byteOffsetMultiplier] within the
 * blob, where the multiplier is the size of the data type that the
 * vendor stores the fields in. [firstTag] and [offsetBase] shift the
 * field positions for tables whose entries do not start at the
 * beginning of the blob.
 */
public data class MakerNoteBlobPointer(
    public val tagId: Int,
    public val directoryType: Int,
    public val tagTable: List<TagInfo>,
    public val byteOffsetMultiplier: Int,
    public val versionTables: Map<String, MakerNoteBlobPointer> = emptyMap(),
    public val modelTables: Map<String, MakerNoteBlobPointer> = emptyMap(),
    public val firstTag: Int = 0,
    public val offsetBase: Int = 0,
    public val encrypted: Boolean = false,
    public val decryptStart: Int = 0,
    public val nestedBlobPointers: List<MakerNoteBlobPointer> = emptyList(),
    /**
     * Drops fields that ExifTool hides for this blob, for example the
     * Canon ShotInfo focus distance that is only meaningful when the
     * upper focus distance is non-zero.
     */
    public val fieldFilter: ((List<TiffField>) -> List<TiffField>)? = null
)
