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

/**
 * Fuzzes the public API with deterministic single-byte mutations of real
 * corpus files and asserts the exception contract from the [Kim]
 * documentation: only [ImageReadException] and [ImageWriteException]
 * escape - never a raw platform exception type, never a cancellation,
 * and never a [StackOverflowError] from deeply nested hostile input.
 *
 * The seed is fixed, so the mutated inputs are identical on every run
 * and every target: a violation here is always reproducible. Each
 * candidate runs in its own test function, because one test covering
 * all candidates needs more than the default runner timeout on
 * JavaScript.
 */
class ApiContractFuzzTest {

    /*
     * media_54 (plain TIFF) is not a candidate: at 2.9 MB it exceeds the
     * size cap, so the old single-test fuzz silently skipped it, too. The
     * two small GeoTIFFs cover the TIFF parse chain.
     */

    @Test
    fun testFuzzGeoTiffPixelScaling() =
        fuzzCandidate(KimTestData.GEOTIFF_PIXEL_SCALING_INDEX)

    @Test
    fun testFuzzGeoTiffAffineTransform() =
        fuzzCandidate(KimTestData.GEOTIFF_AFFINE_TRANSFORM_INDEX)

    @Test
    fun testFuzzJpegWithVariantExifLayout() =

        /* JPEG whose EXIF offset field is stored with a variant type. */
        fuzzCandidate(46)

    @Test
    fun testFuzzJpeg() =
        fuzzCandidate(43)

    @Test
    fun testFuzzPng() =
        fuzzCandidate(KimTestData.PNG_TEST_IMAGE_INDEX)

    @Test
    fun testFuzzGif() =
        fuzzCandidate(KimTestData.GIF_TEST_IMAGE_INDEX)

    @Test
    fun testFuzzWebP() =
        fuzzCandidate(KimTestData.WEBP_TEST_IMAGE_INDEX)

    @Test
    fun testFuzzJxl() =
        fuzzCandidate(KimTestData.JXL_CONTAINER_UNCOMPRESSED_INDEX)

    @Test
    fun testFuzzHeic() =
        fuzzCandidate(KimTestData.HEIC_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX)

    @Test
    fun testFuzzAvif() =
        fuzzCandidate(KimTestData.AVIF_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX)


    /**
     * Fuzzes one corpus file with [MUTATIONS_PER_FILE] deterministic
     * mutations of the five public API entry points.
     *
     * One test function per candidate: a single test covering all
     * candidates needs more than the default runner timeout on
     * JavaScript, because the mutation loops each rewrite
     * multi-megabyte files several times.
     */
    private fun fuzzCandidate(index: Int) {

        val original = KimTestData.getBytesOf(index)

        /*
         * Large files multiply the runtime of every mutation; every
         * candidate above stays below the cap.
         */
        check(original.size <= MAX_CANDIDATE_BYTE_COUNT) {
            "Fuzz candidate media_$index exceeds the size cap."
        }

        val rng = Random(FUZZ_SEED + index)

        for (mutation in 0 until MUTATIONS_PER_FILE) {

            val mutant = mutate(original, rng)

            checkReadContract("media_$index mutation #$mutation", mutant)
        }
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
    @Suppress("SwallowedException")
    /* Accepting the image exceptions is the oracle's purpose. */
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

        private const val MAX_CANDIDATE_BYTE_COUNT: Int = 2 * 1000 * 1000
    }
}
