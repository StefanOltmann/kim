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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.model.TiffOrientation
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Fuzzes the public API with deterministic single-byte mutations of real
 * corpus files and asserts the exception contract from the [Kim]
 * documentation: only [ImageReadException] and [ImageWriteException]
 * escape - never a raw platform exception type, never a cancellation,
 * and never a [StackOverflowError] from deeply nested hostile input.
 *
 * The seed is fixed, so the mutated inputs are identical on every run
 * and every target: a violation here is always reproducible.
 */
class ApiContractFuzzTest {

    /**
     * One representative per parser family: the small GeoTIFFs drive the
     * TiffPreviewExtractor chain, the two JPEGs cover the segment scan
     * (including an unusual EXIF layout), and the remaining files cover
     * the PNG, GIF, WebP, JXL, HEIC and AVIF parsers. All candidates are
     * small, so the fuzz stays fast enough for the slowest target.
     */
    private val candidateIndices: List<Int> = listOf(
        KimTestData.TIFF_NONE_TEST_IMAGE_INDEX,
        KimTestData.GEOTIFF_PIXEL_SCALING_INDEX,
        KimTestData.GEOTIFF_AFFINE_TRANSFORM_INDEX,
        46, /* JPEG whose EXIF offset field is stored with a variant type */
        43, /* JPEG */
        KimTestData.PNG_TEST_IMAGE_INDEX,
        KimTestData.GIF_TEST_IMAGE_INDEX,
        KimTestData.WEBP_TEST_IMAGE_INDEX,
        KimTestData.JXL_CONTAINER_UNCOMPRESSED_INDEX,
        KimTestData.HEIC_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX,
        KimTestData.AVIF_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX
    ).filter { it in 1..KimTestData.TEST_MEDIA_COUNT }

    @Test
    fun testMutatedInputsRespectTheExceptionContract() {

        val rng = Random(FUZZ_SEED)

        var checkedMutations = 0

        for (index in candidateIndices) {

            val original = KimTestData.getBytesOf(index)

            if (original.size > MAX_CANDIDATE_BYTE_COUNT)
                continue

            for (mutation in 0 until MUTATIONS_PER_FILE) {

                val mutant = mutate(original, rng)

                checkReadContract("media_$index mutation #$mutation", mutant)

                checkedMutations++
            }
        }

        /*
         * The corpus is fixed, so the mutation count is deterministic.
         * This guards against the candidate filter silently excluding
         * everything and the fuzz degrading to a no-op.
         */
        assertTrue(
            checkedMutations >= MIN_MUTATION_COUNT,
            "The fuzz checked only $checkedMutations mutations - " +
                "the candidate list did not survive the size filter."
        )
    }

    /**
     * Reads the mutated bytes and, when the parser accepted them, applies
     * an update as well - the write path is fuzzed with files the read
     * step classified as writable.
     */
    private fun checkReadContract(context: String, bytes: ByteArray) {

        checkedCall(context) {
            Kim.readMetadata(ByteArrayByteReader(bytes))
        }

        checkedCall(context) {
            Kim.extractMetadataBytes(ByteArrayByteReader(bytes))
        }

        checkedCall(context) {
            Kim.extractPreviewImage(ByteArrayByteReader(bytes))
        }

        /*
         * Run against every mutant: unknown formats and unparseable files
         * are rejected with ImageWriteException, which the contract
         * allows.
         */
        checkedCall(context) {
            Kim.update(
                bytes = bytes,
                updates = setOf(MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT))
            )
        }

        checkedCall(context) {
            Kim.deleteMetadata(bytes)
        }
    }

    /**
     * Runs the call and turns every exception type that may not escape
     * the public API into a test failure. Image exceptions and JVM
     * errors like OutOfMemoryError are part of the contract.
     *
     * The stack overflow check classifies by name, because the error
     * type itself only exists on the JVM target - deeply nested hostile
     * input must fail the read there, never blow the stack.
     */
    private inline fun checkedCall(context: String, call: () -> Unit) {

        try {
            call()

        } catch (ex: ImageReadException) {
            /* Documented read failure. */

        } catch (ex: ImageWriteException) {
            /* Documented write failure. */

        } catch (ex: CancellationException) {
            throw AssertionError(
                "Exception contract violated for $context: cancellation escaped.",
                ex
            )

        } catch (ex: Exception) {
            throw AssertionError(
                "Exception contract violated for $context: " +
                    "${ex::class.simpleName}: ${ex.message}",
                ex
            )

        } catch (ex: Throwable) {

            if (ex::class.simpleName == "StackOverflowError")
                throw AssertionError(
                    "Exception contract violated for $context: " +
                        "stack overflow from hostile input.",
                    ex
                )

            /* Other errors like OutOfMemoryError are part of the contract. */
        }
    }

    /**
     * Alternates a single-byte flip and a truncation, so both corruption
     * styles are covered with a bounded runtime.
     */
    private fun mutate(original: ByteArray, rng: Random): ByteArray {

        val flip = rng.nextBoolean()

        return if (flip || original.size < 2) {

            val mutant = original.copyOf()

            val position = rng.nextInt(mutant.size)

            mutant[position] = (mutant[position].toInt() xor (1 shl rng.nextInt(8))).toByte()

            mutant

        } else {

            val keptLength = 1 + rng.nextInt(original.size - 1)

            original.copyOfRange(0, keptLength)
        }
    }

    private companion object {

        private const val FUZZ_SEED: Int = 0xC0FFEE

        private const val MUTATIONS_PER_FILE: Int = 64

        private const val MIN_MUTATION_COUNT: Int = 600

        private const val MAX_CANDIDATE_BYTE_COUNT: Int = 2 * 1000 * 1000
    }
}
