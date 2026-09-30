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
package de.stefan_oltmann.kim.testdata

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.TiffHeader
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldType
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeAscii
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo

/**
 * Builds a TIFF field with the neutral defaults the tests need: the
 * bytes are the field value, the field lives in IFD0 unless the tag or
 * [directoryType] says otherwise.
 *
 * [count] defaults to the byte count, which matches the string field
 * types whose [FieldType.size] is one byte.
 */
internal fun tiffField(
    tag: TagInfo,
    bytes: ByteArray,
    fieldType: FieldType<out Any> = FieldTypeAscii,
    count: Int = bytes.size,
    directoryType: Int = tag.directoryType?.typeId ?: TiffConstants.TIFF_DIRECTORY_TYPE_IFD0
): TiffField =
    tiffField(
        tag = tag.tag,
        fieldType = fieldType,
        bytes = bytes,
        count = count,
        directoryType = directoryType
    )

/**
 * The same builder for tests that work with raw tag ids instead of a
 * [TagInfo].
 *
 * [count] defaults to the byte count divided by the field type size,
 * which matches the numeric field types.
 */
internal fun tiffField(
    tag: Int,
    fieldType: FieldType<out Any>,
    bytes: ByteArray,
    count: Int = bytes.size / fieldType.size,
    directoryType: Int = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0
): TiffField = TiffField(
    offset = 0,
    tag = tag,
    directoryType = directoryType,
    fieldType = fieldType,
    count = count,
    localValue = null,
    valueOffset = 0,
    valueBytes = bytes,
    byteOrder = ByteOrder.BIG_ENDIAN,
    sortHint = 0
)

/**
 * Builds a directory with the neutral defaults the tests need: the
 * first IFD sits at the conventional offset 8.
 */
internal fun tiffDirectory(
    type: Int,
    entries: List<TiffField>,
    byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN,
    offset: Int = 8
): TiffDirectory = TiffDirectory(
    type = type,
    entries = entries,
    offset = offset,
    nextDirectoryOffset = 0,
    byteOrder = byteOrder
)

/**
 * Builds a TIFF structure with the given fields in IFD0 and - when any
 * field targets the EXIF directory - an EXIF directory behind it, so
 * tests can pass IFD0 and EXIF fields in one list.
 */
internal fun tiffContents(
    vararg entries: TiffField
): TiffContents {

    val directory = tiffDirectory(TiffConstants.TIFF_DIRECTORY_TYPE_IFD0, entries.toList())

    val exifDirectory = tiffDirectory(
        type = TiffConstants.TIFF_DIRECTORY_EXIF,
        entries = entries.filter { it.directoryType == TiffConstants.TIFF_DIRECTORY_EXIF },
        offset = 100
    )

    val directories = mutableListOf(directory)

    if (exifDirectory.entries.isNotEmpty())
        directories.add(exifDirectory)

    return TiffContents(
        header = TiffHeader(
            byteOrder = ByteOrder.BIG_ENDIAN,
            tiffVersion = 42,
            offsetToFirstIFD = 8
        ),
        directories = directories,
        makerNoteDirectory = null,
        makerNoteSubDirectories = emptyList(),
        geoTiffDirectory = null
    )
}

/**
 * A minimal but completely parseable little-endian TIFF: one IFD0 entry
 * and no next IFD, for tests that need Exif bytes a reader accepts.
 */
internal fun minimalTiffBytes(): ByteArray =
    convertHexStringToByteArray(
        "49492a0008000000" + "0100" + "01000100010000002a000000" + "00000000"
    )
