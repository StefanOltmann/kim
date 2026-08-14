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

        /* The trailer at the end of a binary property list. */
        val offsetSize = 0xFF and blob[blob.size - 26].toInt()
        val objectRefSize = 0xFF and blob[blob.size - 25].toInt()
        val numObjects = blob.toInt64(blob.size - 24)
        val topObject = blob.toInt64(blob.size - 16)
        val offsetTableOffset = blob.toInt64(blob.size - 8)

        if (numObjects <= 0 || numObjects > 1000 || offsetTableOffset >= blob.size)
            return

        fun readObjectRef(offset: Long): Long {

            var value = 0L

            for (index in 0 until objectRefSize)
                value = (value shl 8) or (0xFFL and blob[(offset + index).toInt()].toLong())

            return value
        }

        fun readOffset(index: Long): Long {

            var value = 0L

            for (i in 0 until offsetSize)
                value = (value shl 8) or
                    (0xFFL and blob[(offsetTableOffset + index * offsetSize + i).toInt()].toLong())

            return value
        }

        fun readInt(objectOffset: Long, byteCount: Int): ByteArray {

            val bytes = ByteArray(8)

            for (index in 0 until byteCount)
                bytes[8 - byteCount + index] = blob[(objectOffset + 1 + index).toInt()]

            return bytes
        }

        val topOffset = readOffset(topObject)

        if (topOffset >= blob.size)
            return

        /* The top object must be a dictionary. */
        val marker = 0xFF and blob[topOffset.toInt()].toInt()

        if (marker and 0xF0 != 0xD0)
            return

        var count = marker and 0x0F

        var pos = topOffset + 1

        if (count == 0x0F) {

            /* The extended count is stored as an integer object. */
            count = 0xFF and blob[pos.toInt()].toInt()

            pos++
        }

        val fields = mutableListOf<TiffField>()

        for (index in 0 until count) {

            val keyRef = readObjectRef(pos)
            val valueRef = readObjectRef(pos + objectRefSize)

            pos += objectRefSize * 2

            val keyOffset = readOffset(keyRef)
            val valueOffset = readOffset(valueRef)

            val keyMarker = 0xFF and blob[keyOffset.toInt()].toInt()
            val keyLength = keyMarker and 0x0F

            val key = blob.copyOfRange(
                (keyOffset + 1).toInt(),
                (keyOffset + 1 + keyLength).toInt()
            ).decodeToString()

            val valueMarker = 0xFF and blob[valueOffset.toInt()].toInt()

            if (valueMarker and 0xF0 != 0x10)
                continue

            val byteCount = valueMarker and 0x0F

            val valueBytes = readInt(valueOffset, byteCount)

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



