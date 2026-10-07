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
package de.stefan_oltmann.kim.format.tiff

import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.DefaultRandomAccessByteReader
import de.stefan_oltmann.kim.input.RandomAccessByteReader
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The TIFF reader tolerates unreadable MakerNotes and thumbnail
 * directories, because those structures are non-essential or preserved
 * as opaque blocks. A cancellation is not an unreadable structure:
 * swallowing it would turn a cancelled call into a partial "success",
 * so it must propagate through every tolerance path.
 */
class CancellationPropagationTest {

    /**
     * A cancellation injected while the MakerNote handler re-reads its
     * region must fail the read, not degrade to a MakerNote-less result.
     */
    @Test
    fun testMakerNoteCancellationPropagatesThroughTheRead() {

        val bytes = convertHexStringToByteArray(
            /* Header: II, version 42, IFD0 at offset 8 */
            "49492A0008000000" +
                /* IFD0: 2 entries */
                "0200" +
                /* Make -> 38 */
                "0F0102000600000026000000" +
                /* ExifOffset -> 44 */
                "69870400010000002C000000" +
                /* No next directory */
                "00000000" +
                /* "Canon\0" */
                "43616E6F6E00" +
                /* ExifIFD: 1 entry */
                "0100" +
                /* MakerNote -> 62 */
                "7C920700200000003E000000" +
                /* No next directory */
                "00000000" +
                /* MakerNote blob */
                "41414141414141414141414141414141" +
                "41414141414141414141414141414141"
        )

        val reader = CancellationOnSecondRegionRead(
            delegate = DefaultRandomAccessByteReader(ByteArrayByteReader(bytes)),
            regionStartOffset = 66
        )

        assertFailsWith<CancellationException> {
            TiffReader.read(reader)
        }
    }

    /**
     * A cancellation injected while the chain IFD1 (the thumbnail
     * directory) is parsed must fail the read, not degrade to a
     * directory-chain that silently stops at IFD0.
     */
    @Test
    fun testIfd1CancellationPropagatesThroughTheRead() {

        val bytes = convertHexStringToByteArray(
            /* Header: II, version 42, IFD0 at offset 8 */
            "49492A0008000000" +
                /* IFD0: 1 entry */
                "0100" +
                /* ImageWidth = 4 */
                "000104000100000004000000" +
                /* Next IFD at offset 26 */
                "1A000000" +
                /* IFD1: 1 entry */
                "0100" +
                /* ImageWidth = 4 */
                "000104000100000004000000" +
                /* No next directory */
                "00000000"
        )

        val reader = CancellationFromOffsetOn(
            delegate = DefaultRandomAccessByteReader(ByteArrayByteReader(bytes)),
            triggerOffset = 28
        )

        assertFailsWith<CancellationException> {
            TiffReader.read(reader)
        }
    }

    /**
     * Serves all reads, but injects the cancellation on the second read
     * that starts inside the MakerNote region: the first one is the
     * field value capture, the second one is the handler's re-read.
     */
    private class CancellationOnSecondRegionRead(
        private val delegate: RandomAccessByteReader,
        private val regionStartOffset: Int
    ) : RandomAccessByteReader {

        private var position = 0

        private var regionAccessCount = 0

        override val contentLength: Long = delegate.contentLength

        override fun moveTo(position: Int) {

            this.position = position

            delegate.moveTo(position)
        }

        override fun readByte(): Byte? {

            maybeCancelAt(position)

            val byte = delegate.readByte()

            if (byte != null)
                position += 1

            return byte
        }

        override fun readBytes(count: Int): ByteArray {

            maybeCancelAt(position)

            val bytes = delegate.readBytes(count)

            position += bytes.size

            return bytes
        }

        override fun readBytes(offset: Int, length: Int): ByteArray {

            maybeCancelAt(offset)

            position = offset + length

            return delegate.readBytes(offset, length)
        }

        override fun close() = delegate.close()

        private fun maybeCancelAt(offset: Int) {

            if (offset < regionStartOffset)
                return

            regionAccessCount += 1

            if (regionAccessCount == 2)
                throw CancellationException("Injected cancellation in the MakerNote re-read.")
        }
    }

    /**
     * Serves all reads below the trigger offset and injects the
     * cancellation on the first read at or beyond it.
     */
    private class CancellationFromOffsetOn(
        private val delegate: RandomAccessByteReader,
        private val triggerOffset: Int
    ) : RandomAccessByteReader {

        private var position = 0

        override val contentLength: Long = delegate.contentLength

        override fun moveTo(position: Int) {

            this.position = position

            delegate.moveTo(position)
        }

        override fun readByte(): Byte? {

            if (position >= triggerOffset)
                throw CancellationException("Injected cancellation.")

            val byte = delegate.readByte()

            if (byte != null)
                position += 1

            return byte
        }

        override fun readBytes(count: Int): ByteArray {

            if (position >= triggerOffset)
                throw CancellationException("Injected cancellation.")

            val bytes = delegate.readBytes(count)

            position += bytes.size

            return bytes
        }

        override fun readBytes(offset: Int, length: Int): ByteArray {

            if (offset >= triggerOffset)
                throw CancellationException("Injected cancellation.")

            position = offset + length

            return delegate.readBytes(offset, length)
        }

        override fun close() = delegate.close()
    }
}
