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
package de.stefan_oltmann.kim

import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.fail

class PreviewImageExtractionTest {

    val indicesWithPreviewImage: Set<Int> = setOf(
        KimTestData.CR2_TEST_IMAGE_INDEX,
        KimTestData.RAF_TEST_IMAGE_INDEX,
        KimTestData.NEF_TEST_IMAGE_INDEX,
        KimTestData.ARW_TEST_IMAGE_INDEX,
        KimTestData.RW2_TEST_IMAGE_INDEX,
        KimTestData.ORF_TEST_IMAGE_INDEX,
        KimTestData.CR3_TEST_IMAGE_INDEX,
        KimTestData.DNG_CR2_TEST_IMAGE_INDEX,
        KimTestData.DNG_RAF_TEST_IMAGE_INDEX,
        KimTestData.DNG_NEF_TEST_IMAGE_INDEX,
        KimTestData.DNG_ARW_TEST_IMAGE_INDEX,
        KimTestData.DNG_RW2_TEST_IMAGE_INDEX,
        KimTestData.DNG_ORF_TEST_IMAGE_INDEX
    )

    @Test
    fun testExtractPreviewImage() {

        @Suppress("LoopWithTooManyJumpStatements")
        for (index in indicesWithPreviewImage) {

            val bytes = KimTestData.getBytesOf(index)

            val previewImageBytes = Kim.extractPreviewImage(
                ByteArrayByteReader(bytes)
            )

            assertNotNull(previewImageBytes, "File #$index has no preview.")

            val expectedPreviewImageBytes = KimTestData.getPreviewBytesOf(index)

            val equals = expectedPreviewImageBytes.contentEquals(previewImageBytes)

            if (!equals) {

                Path("build/media_${index}_preview.jpg")
                    .writeBytes(previewImageBytes)

                fail("Media $index has not the expected bytes!")
            }
        }
    }

    /**
     * The preview fallback chain degrades extractor failures to NULL, so
     * one broken format does not hide the others. A cancellation is not a
     * broken format: swallowing it would turn a cancelled call into a
     * neutral "no preview", so it must propagate.
     */
    @Test
    fun testExtractPreviewPropagatesCancellation() {

        val reader = OverstatedLengthReader(realBytes = tiffWithDngPreview())

        assertFailsWith<CancellationException> {
            Kim.extractPreviewImage(reader)
        }
    }

    /**
     * The content length of a stream reader is only a hint that stream
     * sources may understate. A preview that lies fully within the real
     * data must still be extracted - the read itself decides, like every
     * other read in the TIFF family.
     */
    @Test
    fun testExtractPreviewIgnoresUnderstatedContentLengthHint() {

        val bytes = KimTestData.getBytesOf(KimTestData.CR2_TEST_IMAGE_INDEX)

        val honestPreview =
            assertNotNull(Kim.extractPreviewImage(ByteArrayByteReader(bytes)))

        /* The hint ends exactly where the preview starts. */
        val previewOffset = indexOf(bytes, honestPreview.copyOfRange(0, 16))

        val reader = UnderstatedHintReader(
            delegate = ByteArrayByteReader(bytes),
            hintedLength = previewOffset.toLong()
        )

        assertContentEquals(
            expected = honestPreview,
            actual = assertNotNull(Kim.extractPreviewImage(reader))
        )
    }

    /**
     * Returns the offset of the first occurrence of the needle, or -1.
     */
    private fun indexOf(bytes: ByteArray, needle: ByteArray): Int {

        for (index in 0..bytes.size - needle.size) {

            if (bytes.copyOfRange(index, index + needle.size).contentEquals(needle))
                return index
        }

        return -1
    }

    /**
     * Serves the real bytes and understates the content length, so a
     * read gated on the hint refuses ranges that are actually readable.
     */
    private class UnderstatedHintReader(
        private val delegate: ByteReader,
        private val hintedLength: Long
    ) : ByteReader {

        override val contentLength: Long = hintedLength

        override fun readByte(): Byte? = delegate.readByte()

        override fun readBytes(count: Int): ByteArray = delegate.readBytes(count)

        override fun close() = delegate.close()
    }

    /**
     * DNG fixture: IFD0 carries the DNGVersion tag, and IFD2 carries a
     * preview start that points beyond the real data, so only the DNG
     * preview extractor reads there - after the TIFF structure itself
     * was parsed successfully.
     */
    private fun tiffWithDngPreview(): ByteArray = convertHexStringToByteArray(
        /* Header: II, version 42, IFD0 at offset 8 */
        "49492a0008000000" +
            /* IFD0: 1 entry */
            "0100" +
            /* DNGVersion = 1.4 */
            "12c601000400000001040000" +
            /* Next IFD at offset 26 */
            "1a000000" +
            /* IFD1: no entries */
            "0000" +
            /* Next IFD at offset 32 */
            "20000000" +
            /* IFD2: 2 entries */
            "0200" +
            /* PreviewImageStart = 100 */
            "110104000100000064000000" +
            /* PreviewImageLength = 16 */
            "170104000100000010000000" +
            /* No next directory */
            "00000000"
    )

    /**
     * Serves the real bytes and overstates the content length, so the
     * preview validation passes and the read behind the real data is the
     * point where the cancellation is injected.
     */
    private class OverstatedLengthReader(
        private val realBytes: ByteArray
    ) : ByteReader {

        private var position = 0

        override val contentLength: Long = 1000L

        override fun readByte(): Byte {

            if (position >= realBytes.size)
                throw CancellationException("Injected cancellation.")

            return realBytes[position++]
        }

        override fun readBytes(count: Int): ByteArray {

            if (position + count > realBytes.size)
                throw CancellationException("Injected cancellation.")

            val result = realBytes.copyOfRange(position, position + count)

            position += count

            return result
        }

        override fun close() {
            /* Nothing to close. */
        }
    }
}
