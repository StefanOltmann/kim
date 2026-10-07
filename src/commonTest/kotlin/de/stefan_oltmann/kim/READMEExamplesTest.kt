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

import de.stefan_oltmann.kim.common.convertToSummary
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffTag
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Compiles and executes the README usage snippets against a real test
 * file, so a public API change that breaks the documented code fails
 * the build instead of shipping non-compiling examples.
 */
class READMEExamplesTest {

    /**
     * The "Read metadata" snippet: unknown input yields null, so the
     * documented code handles the nullable return before dereferencing.
     */
    @Test
    fun testReadMetadataSnippet() {

        /* The README's `loadBytes()`: a real test media file. */
        val bytes: ByteArray = KimTestData.getBytesOf(2)

        val metadata = Kim.readMetadata(bytes) ?: error("Not a supported image file.")

        /* The toString() is similar to the output of ExifTool. */
        assertFalse(metadata.toString().isEmpty())

        /*
         * The documented call shapes. A file may lack either value, so
         * only the execution - not a concrete value - is asserted here.
         */
        metadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)

        metadata.findStringValue(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
    }

    /**
     * The summary snippet: `convertToSummary()` is an extension on
     * non-null [MediaMetadata], so the documented code chains through
     * the nullable read result.
     */
    @Test
    fun testSummarySnippet() {

        val bytes: ByteArray = KimTestData.getBytesOf(2)

        val summary =
            Kim.readMetadata(bytes)?.convertToSummary() ?: error("Not a supported image file.")

        assertEquals(MediaFormat.JPEG, summary.mediaFormat)
    }

    /**
     * The null contract the snippets rely on: unknown bytes yield null.
     */
    @Test
    fun testUnknownInputYieldsNull() {

        assertNull(
            Kim.readMetadata("This is not an image file at all".encodeToByteArray())
        )
    }
}
