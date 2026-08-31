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
package de.stefan_oltmann.kim.format.tiff.makernote.apple

import de.stefan_oltmann.kim.common.ByteOrder
import de.stefan_oltmann.kim.format.tiff.TiffDirectory
import de.stefan_oltmann.kim.format.tiff.TiffField
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants.TIFF_MAKER_NOTE_APPLE_RUN_TIME
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeInt64
import de.stefan_oltmann.kim.format.tiff.makernote.MakerNoteHandler
import de.stefan_oltmann.kim.input.RandomAccessByteReader

/**
 * Reads the MakerNote of Apple cameras.
 */
@Suppress("MagicNumber")
internal object AppleMakerNoteHandler : MakerNoteHandler() {

    private const val APPLE_MAKER_NOTE_SIGNATURE = "Apple iOS\u0000\u0000\u0001"

    /**
     * A RunTime property list contains a handful of entries; anything
     * beyond this bound is treated as malformed instead of being walked.
     */
    private const val MAX_PLIST_OBJECTS: Int = 1000

    /**
     * Reads the MakerNote of an Apple camera.
     *
     * Apple MakerNotes start with a signature, a byte order marker
     * and then the IFD entries directly.
     */
    internal fun read(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        if (!readMakerNoteSignature(byteReader, makerNoteValueOffset, APPLE_MAKER_NOTE_SIGNATURE))
            return

        val byteOrder = readMakerNoteByteOrder(byteReader) ?: return

        val ifdOffset = APPLE_MAKER_NOTE_SIGNATURE.length + 2

        var makerNoteDirectory: TiffDirectory? = null

        readMakerNoteDirectory(
            byteReader = byteReader,
            directoryOffset = makerNoteValueOffset + ifdOffset,
            valueOffsetBase = makerNoteValueOffset,
            byteOrder = byteOrder,
            directoryType = TiffConstants.TIFF_MAKER_NOTE_APPLE,
            addDirectory = {
                makerNoteDirectory = it
                addDirectory(it)
            }
        )

        makerNoteDirectory?.let { directory ->

            readRunTime(
                directory = directory,
                byteOrder = byteOrder,
                addDirectory = addDirectory
            )
        }
    }

    /**
     * Reads the RunTime data of the Apple MakerNote.
     *
     * The data is a binary property list with the time the application
     * ran, its flags, the time scale and the epoch.
     *
     * The plist header fields of a corrupted blob are attacker-controlled,
     * so every index is bounds-checked before access. Any violation degrades
     * to an opaque block: the RunTime directory is simply not added instead
     * of throwing, so one broken tag cannot make the whole photo unreadable.
     */
    private fun readRunTime(
        directory: TiffDirectory,
        byteOrder: ByteOrder,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        val field = directory.entries.find { it.tag == 0x0003 } ?: return

        val blob = field.valueBytes

        if (blob.size < 40)
            return

        val blobSize = blob.size.toLong()

        /* Bounds-checked byte access; NULL marks the blob as malformed. */
        fun byteAt(index: Long): Int? {

            if (index < 0 || index >= blobSize)
                return null

            return 0xFF and blob[index.toInt()].toInt()
        }

        /* The trailer at the end of a binary property list. */
        val offsetSize = 0xFF and blob[blob.size - 26].toInt()
        val objectRefSize = 0xFF and blob[blob.size - 25].toInt()
        val numObjects = blob.toInt64(blob.size - 24)
        val topObject = blob.toInt64(blob.size - 16)
        val offsetTableOffset = blob.toInt64(blob.size - 8)

        if (numObjects <= 0 || numObjects > MAX_PLIST_OBJECTS)
            return

        if (offsetTableOffset < 0 || offsetTableOffset >= blobSize)
            return

        fun readObjectRef(offset: Long): Long? {

            var value = 0L

            for (index in 0 until objectRefSize) {

                val byte = byteAt(offset + index) ?: return null

                value = (value shl 8) or byte.toLong()
            }

            return value
        }

        fun readOffsetTableEntry(objectIndex: Long): Long? {

            var value = 0L

            for (byteIndex in 0 until offsetSize) {

                val byte = byteAt(offsetTableOffset + objectIndex * offsetSize + byteIndex)
                    ?: return null

                value = (value shl 8) or byte.toLong()
            }

            return value
        }

        fun readInt(objectOffset: Long, byteCount: Int): ByteArray? {

            val bytes = ByteArray(8)

            for (index in 0 until byteCount) {

                val byte = byteAt(objectOffset + 1 + index) ?: return null

                bytes[8 - byteCount + index] = byte.toByte()
            }

            return bytes
        }

        val topOffset = readOffsetTableEntry(topObject) ?: return

        /* The top object must lie inside the blob. */
        if (topOffset < 0 || topOffset >= blobSize)
            return

        /* The top object must be a dictionary. */
        val marker = byteAt(topOffset) ?: return

        if (marker and 0xF0 != 0xD0)
            return

        var count = marker and 0x0F

        var pos = topOffset + 1L

        if (count == 0x0F) {

            /* The extended count is stored as an integer object. */
            count = byteAt(pos) ?: return

            pos++
        }

        val fields = mutableListOf<TiffField>()

        for (index in 0 until count) {

            val keyRef = readObjectRef(pos) ?: return
            val valueRef = readObjectRef(pos + objectRefSize) ?: return

            pos += objectRefSize * 2L

            val keyOffset = readOffsetTableEntry(keyRef) ?: return
            val valueOffset = readOffsetTableEntry(valueRef) ?: return

            fun isValidObjectOffset(offset: Long): Boolean =
                offset >= 0 && offset < blobSize

            if (!isValidObjectOffset(keyOffset) || !isValidObjectOffset(valueOffset))
                return

            val keyMarker = byteAt(keyOffset) ?: return
            val keyLength = keyMarker and 0x0F

            val keyEnd = keyOffset + 1 + keyLength

            if (keyEnd > blobSize)
                return

            val valueMarker = byteAt(valueOffset) ?: return

            if (valueMarker and 0xF0 != 0x10)
                continue

            val byteCount = valueMarker and 0x0F

            val valueBytes = readInt(valueOffset, byteCount) ?: return

            val absoluteOffset = getAbsoluteValueOffset(field)

            fields.add(
                TiffField(
                    offset = absoluteOffset,
                    tag = index + 1,
                    directoryType = TIFF_MAKER_NOTE_APPLE_RUN_TIME,
                    fieldType = FieldTypeInt64,
                    count = 1,
                    localValue = null,
                    valueOffset = absoluteOffset,
                    valueBytes = valueBytes,
                    byteOrder = byteOrder,
                    sortHint = index
                )
            )
        }

        if (fields.isEmpty())
            return

        addDirectory(
            TiffDirectory(
                type = TIFF_MAKER_NOTE_APPLE_RUN_TIME,
                entries = fields,
                offset = getAbsoluteValueOffset(field),
                nextDirectoryOffset = 0,
                byteOrder = byteOrder
            )
        )
    }
}



