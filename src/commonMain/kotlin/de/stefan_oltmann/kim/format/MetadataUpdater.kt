/*
 * Copyright 2026 Stefan Oltmann
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
package de.stefan_oltmann.kim.format

import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteWriter

/**
 * Updates the metadata of a media file.
 *
 * Every update of the given set is applied to all formats that can represent
 * it, so EXIF, IPTC and XMP can be updated simultaneously in one call.
 */
public interface MetadataUpdater {

    @Throws(ImageWriteException::class)
    public fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    )

    /**
     * Removes all metadata of the file, keeping the ICC chunks that affect
     * how the image is displayed.
     */
    @Throws(ImageWriteException::class)
    public fun deleteMetadata(
        byteReader: ByteReader,
        byteWriter: ByteWriter
    )

    /**
     * Replaces the embedded thumbnail of the file with the given JPEG bytes.
     *
     * Attention: The thumbnail is embedded into the EXIF data, which must
     * fit into a single JPEG APP1 segment of about 65 KB. Thumbnails that
     * exceed this limit are rejected with an [ImageWriteException].
     */
    @Throws(ImageWriteException::class)
    public fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray

}
