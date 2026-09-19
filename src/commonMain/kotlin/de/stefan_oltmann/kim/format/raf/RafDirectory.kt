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
package de.stefan_oltmann.kim.format.raf

/**
 * The section directory of the RAF header: where the embedded JPEG, the
 * CFA header and the CFA raw data block live inside the file.
 *
 * All values are byte offsets and lengths relative to the file start.
 * Every section is validated to point inside the file.
 *
 * ExifTool calls the CFA header the "RAF directory" and the CFA raw data
 * block the "FujiIFD", because it starts with a TIFF-style header.
 *
 * See http://fileformats.archiveteam.org/wiki/Fujifilm_RAF
 */
public data class RafDirectory(
    /** Offset of the embedded JPEG that carries the metadata. */
    val jpegImageOffset: Long,
    /** Length of the embedded JPEG. */
    val jpegImageLength: Long,
    /** Offset of the CFA header. */
    val cfaHeaderOffset: Long,
    /** Length of the CFA header. */
    val cfaHeaderLength: Long,
    /** Offset of the CFA raw data block. */
    val cfaOffset: Long,
    /** Length of the CFA raw data block. */
    val cfaLength: Long
)
