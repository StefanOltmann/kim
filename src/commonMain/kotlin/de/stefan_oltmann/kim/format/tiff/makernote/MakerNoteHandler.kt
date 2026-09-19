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

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.common.toInt
import de.stefan_oltmann.kim.common.toLong
import de.stefan_oltmann.kim.common.toUInt16
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.makernote.nikon.NikonDecryptor
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.input.RandomAccessByteReader
import de.stefan_oltmann.kim.input.readByteAsInt
import de.stefan_oltmann.kim.input.readBytes
import de.stefan_oltmann.kim.input.skipBytes

/**
 * The shared machinery for reading the MakerNote directories.
 *
 * The vendor specific handlers extend this class to read the MakerNote
 * directories of their cameras, including the binary blob
 * sub-directories and the IFD-style sub-directories.
 *
 * Like ExifTool, a MakerNote that cannot be parsed completely is kept
 * as an opaque binary block: unreadable sub-directories are skipped
 * while the rest of the MakerNote is still read, and the file is never
 * rejected for that reason. ExifTool prints a warning in this case
 * ("Maker notes could not be parsed"), but the MakerNote survives the
 * rewrite unchanged either way, because the writer keeps the whole
 * MakerNote value byte for byte at its original offset. A file is only
 * rejected when the MakerNote field itself cannot be read, which matches
 * ExifTool's fatal error for that case.
 */
@Suppress("MagicNumber")
internal open class MakerNoteHandler {

    /**
     * Reads the IFD of a MakerNote at the given offset.
     *
     * The [valueOffsetBase] resolves the MakerNote value offsets
     * against the start of the TIFF bytes, so the stored fields always
     * carry absolute offsets.
     *
     * Every read directory is passed to [addDirectory]. The read
     * directory is also returned, so callers can read its
     * sub-directories without capturing it from the callback.
     */
    protected fun readMakerNoteDirectory(
        byteReader: RandomAccessByteReader,
        directoryOffset: Int,
        valueOffsetBase: Int,
        byteOrder: ByteOrder,
        directoryType: Int,
        addDirectory: (TiffDirectory) -> Unit
    ): TiffDirectory? {

        var readDirectory: TiffDirectory? = null

        TiffReader.readDirectory(
            byteReader = byteReader,
            byteOrder = byteOrder,
            directoryOffset = directoryOffset,
            directoryType = directoryType,
            visitedOffsets = hashSetOf(),
            readTiffImageBytes = false,
            addDirectory = {
                readDirectory = it
                addDirectory(it)
            },
            valueOffsetBase = valueOffsetBase,
            followNextDirectory = false
        )

        return readDirectory
    }

    /**
     * Checks if the MakerNote data starts with the given signature.
     */
    protected fun readMakerNoteSignature(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        signature: String
    ): Boolean {

        byteReader.reset()
        byteReader.skipBytes("MakerNote signature", makerNoteValueOffset)

        val signatureBytes = byteReader.readBytes(
            fieldName = "MakerNote signature",
            count = signature.length
        )

        return signatureBytes.decodeToString() == signature
    }

    /**
     * Reads the byte order marker ('II' or 'MM') from the current
     * position and returns the corresponding [ByteOrder].
     */
    protected fun readMakerNoteByteOrder(byteReader: RandomAccessByteReader): ByteOrder? {

        val firstByte = byteReader.readByteAsInt()
        val secondByte = byteReader.readByteAsInt()

        if (firstByte != secondByte)
            return null

        return when (firstByte) {
            'I'.code -> ByteOrder.LITTLE_ENDIAN
            'M'.code -> ByteOrder.BIG_ENDIAN
            else -> null
        }
    }

    /**
     * Reads the IFD sub-directories referenced by the given MakerNote
     * directory. The pointer values are offsets relative to the
     * start of the MakerNote data.
     *
     * Like ExifTool, a sub-directory that cannot be read is skipped
     * instead of rejecting the file: the MakerNote is kept as an
     * opaque binary block and survives the rewrite unchanged, because
     * the writer keeps the whole MakerNote value at its original offset.
     */
    protected fun readMakerNoteIfdSubDirectories(
        byteReader: RandomAccessByteReader,
        directory: TiffDirectory,
        valueOffsetBase: Int,
        byteOrder: ByteOrder,
        subIfdPointers: List<Pair<TagInfo, Int>>,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        for ((pointerTag, directoryType) in subIfdPointers) {

            val offset = directory.findField(pointerTag)?.toInt() ?: continue

            if (offset == 0)
                continue

            try {

                readMakerNoteDirectory(
                    byteReader = byteReader,
                    directoryOffset = valueOffsetBase + offset,
                    valueOffsetBase = valueOffsetBase,
                    byteOrder = byteOrder,
                    directoryType = directoryType,
                    addDirectory = addDirectory
                )

            } catch (_: Exception) {
                /*
                 * Skip the unreadable sub-directory.
                 *
                 * Like ExifTool, the MakerNote is kept as an opaque
                 * binary block in this case, and the rewrite preserves
                 * it byte for byte.
                 */
            }
        }
    }

    /**
     * Reads the binary sub-directories referenced by the given MakerNote
     * directory. The pointer values are offsets relative to the start
     * of the MakerNote data.
     *
     * Like ExifTool, a sub-directory that cannot be read is skipped
     * instead of rejecting the file: the MakerNote is kept as an
     * opaque binary block and survives the rewrite unchanged, because
     * the writer keeps the whole MakerNote value at its original offset.
     */
    protected fun readMakerNoteBlobSubDirectories(
        directory: TiffDirectory,
        byteOrder: ByteOrder,
        blobPointers: List<MakerNoteBlobPointer>,
        addDirectory: (TiffDirectory) -> Unit,
        serialKey: Int? = null,
        countKey: Int? = null,
        model: String? = null
    ) {

        for (pointer in blobPointers) {

            val field = directory.entries.find { it.tag == pointer.tagId } ?: continue

            val blobLength = field.count * field.fieldType.size

            if (blobLength <= 0 || field.valueBytes.size != blobLength)
                continue

            try {

                /* The blob layout may depend on the version bytes or the camera model. */
                val version = field.valueBytes.copyOfRange(0, minOf(4, field.valueBytes.size))
                    .decodeToString()

                val effectivePointer = pointer.versionTables[version]
                    ?: pointer.modelTables.entries.firstOrNull { (key, _) ->
                        model?.contains(key) == true
                    }?.value
                    ?: pointer

                val blobBytes = if (effectivePointer.encrypted)
                    NikonDecryptor.decrypt(
                        data = field.valueBytes,
                        serialKey = serialKey ?: continue,
                        count = countKey ?: continue,
                        start = effectivePointer.decryptStart
                    )
                else
                    field.valueBytes

                val subDirectory = readMakerNoteBlobSubDirectory(
                    blobBytes = blobBytes,
                    blobOffset = getAbsoluteValueOffset(field),
                    blobLength = blobLength,
                    byteOrder = byteOrder,
                    pointer = effectivePointer,
                    addDirectory = addDirectory
                )

                readNestedMakerNoteBlobSubDirectories(
                    parentDirectory = subDirectory,
                    parentBlobBytes = blobBytes,
                    byteOrder = byteOrder,
                    nestedBlobPointers = effectivePointer.nestedBlobPointers,
                    addDirectory = addDirectory
                )

            } catch (_: Exception) {
                /*
                 * Skip the unreadable sub-directory.
                 *
                 * Like ExifTool, the MakerNote is kept as an opaque
                 * binary block in this case, and the rewrite preserves
                 * it byte for byte.
                 */
            }
        }
    }

    /**
     * Reads the sub-directories that are nested inside the given blob.
     *
     * Like ExifTool, a sub-directory that cannot be read is skipped
     * instead of rejecting the file: the MakerNote is kept as an
     * opaque binary block and survives the rewrite unchanged, because
     * the writer keeps the whole MakerNote value at its original offset.
     */
    private fun readNestedMakerNoteBlobSubDirectories(
        parentDirectory: TiffDirectory,
        parentBlobBytes: ByteArray,
        byteOrder: ByteOrder,
        nestedBlobPointers: List<MakerNoteBlobPointer>,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        for (nestedPointer in nestedBlobPointers) {

            try {

                val field = parentDirectory.entries.find { it.tag == nestedPointer.tagId }

                val nestedBlobBytes = field?.valueBytes
                    ?: parentBlobBytes.copyOfRange(
                        nestedPointer.tagId,
                        parentBlobBytes.size
                    )

                val nestedBlobLength = field?.count?.times(field.fieldType.size)
                    ?: nestedBlobBytes.size

                if (nestedBlobLength <= 0)
                    continue

                readMakerNoteBlobSubDirectory(
                    blobBytes = nestedBlobBytes,
                    blobOffset = field?.let { getAbsoluteValueOffset(it) }
                        ?: (parentDirectory.offset + nestedPointer.tagId),
                    blobLength = nestedBlobLength,
                    byteOrder = byteOrder,
                    pointer = nestedPointer,
                    addDirectory = addDirectory
                )

            } catch (_: Exception) {
                /*
                 * Skip the unreadable sub-directory.
                 *
                 * Like ExifTool, the MakerNote is kept as an opaque
                 * binary block in this case, and the rewrite preserves
                 * it byte for byte. The lookup and copy of the blob bytes
                 * run inside this per-pointer try as well, so one hostile
                 * pointer cannot abort the remaining pointers of the blob.
                 */
            }
        }
    }

    /**
     * Reads a MakerNote sub-directory that is stored as a binary blob
     * with the fields at fixed offsets, for example the Canon and
     * Nikon sub-directories. The directory layout is taken from the
     * given blob pointer.
     */
    protected fun readMakerNoteBlobSubDirectory(
        blobBytes: ByteArray,
        blobOffset: Int,
        blobLength: Int,
        byteOrder: ByteOrder,
        pointer: MakerNoteBlobPointer,
        addDirectory: (TiffDirectory) -> Unit
    ): TiffDirectory =
        readMakerNoteBlobSubDirectory(
            blobBytes = blobBytes,
            blobOffset = blobOffset,
            blobLength = blobLength,
            byteOrder = byteOrder,
            directoryType = pointer.directoryType,
            tagTable = pointer.tagTable,
            firstTag = pointer.firstTag,
            offsetBase = pointer.offsetBase,
            byteOffsetMultiplier = pointer.byteOffsetMultiplier,
            fieldFilter = pointer.fieldFilter,
            addDirectory = addDirectory
        )

    /**
     * Reads a MakerNote sub-directory that is stored as a binary blob
     * with the fields at fixed offsets, for example the Canon and
     * Nikon sub-directories.
     */
    protected fun readMakerNoteBlobSubDirectory(
        blobBytes: ByteArray,
        blobOffset: Int,
        blobLength: Int,
        byteOrder: ByteOrder,
        directoryType: Int,
        tagTable: List<TagInfo>,
        firstTag: Int,
        offsetBase: Int,
        byteOffsetMultiplier: Int,
        fieldFilter: ((List<TiffField>) -> List<TiffField>)? = null,
        addDirectory: (TiffDirectory) -> Unit
    ): TiffDirectory {

        val fields = mutableListOf<TiffField>()

        for (tagInfo in tagTable.sortedBy { it.tag }) {

            val count = tagInfo.length

            if (count <= 0)
                continue

            val valueLength = count * tagInfo.fieldType.size

            val offset = (tagInfo.tag - firstTag) * byteOffsetMultiplier + offsetBase

            if (offset < 0 || offset + valueLength > blobLength)
                continue

            val valueBytes = blobBytes.copyOfRange(offset, offset + valueLength)

            fields.add(
                TiffField(
                    offset = blobOffset + offset,
                    tag = tagInfo.tag,
                    directoryType = directoryType,
                    fieldType = tagInfo.fieldType,
                    count = count,
                    localValue = null,
                    valueOffset = blobOffset + offset,
                    valueBytes = valueBytes,
                    byteOrder = byteOrder,
                    sortHint = tagInfo.tag,
                    tagInfoOverride = tagInfo
                )
            )
        }

        val directory = TiffDirectory(
            type = directoryType,
            entries = fieldFilter?.invoke(fields) ?: fields,
            offset = blobOffset,
            nextDirectoryOffset = 0,
            byteOrder = byteOrder
        )

        addDirectory(directory)

        return directory
    }

    /**
     * Returns the absolute offset of the value bytes of the given
     * field within the TIFF data, which is the in-entry value slot
     * when the value is stored locally.
     */
    protected fun getAbsoluteValueOffset(field: TiffField): Int =
        field.valueOffset ?: (field.offset + TiffConstants.TIFF_ENTRY_VALUE_OFFSET)

    /**
     * Reads a 16-bit integer from the given position.
     */
    protected fun ByteArray.toInt16(offset: Int, byteOrder: ByteOrder): Int =
        toUInt16(offset, byteOrder)

    /**
     * Reads a 32-bit integer from the given position.
     */
    protected fun ByteArray.toInt32(offset: Int, byteOrder: ByteOrder): Int =
        toInt(offset, byteOrder)

    /**
     * Reads a 64-bit big-endian integer from the given position,
     * as used by the Apple RunTime property list.
     */
    protected fun ByteArray.toInt64(offset: Int): Long =
        toLong(offset, ByteOrder.BIG_ENDIAN)
}
