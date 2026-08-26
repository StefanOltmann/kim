/*
 * Copyright 2026 Ramon Bouckaert
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
package de.stefan_oltmann.kim.model

import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaFormatTest {

    @Test
    fun testIsMetadataEmbeddable() {

        assertTrue(MediaFormat.JPEG.isMetadataEmbeddable())
        assertTrue(MediaFormat.PNG.isMetadataEmbeddable())
        assertTrue(MediaFormat.WEBP.isMetadataEmbeddable())
        assertTrue(MediaFormat.JXL.isMetadataEmbeddable())

        assertFalse(MediaFormat.GIF.isMetadataEmbeddable())
        assertFalse(MediaFormat.TIFF.isMetadataEmbeddable())
        assertFalse(MediaFormat.CR3.isMetadataEmbeddable())
        assertFalse(MediaFormat.MP4.isMetadataEmbeddable())
        assertFalse(MediaFormat.PDF.isMetadataEmbeddable())
    }

    @Test
    fun testHasPreview() {

        assertTrue(MediaFormat.CR2.hasPreview())
        assertTrue(MediaFormat.CR3.hasPreview())
        assertTrue(MediaFormat.RAF.hasPreview())
        assertTrue(MediaFormat.NEF.hasPreview())
        assertTrue(MediaFormat.ARW.hasPreview())
        assertTrue(MediaFormat.RW2.hasPreview())
        assertTrue(MediaFormat.ORF.hasPreview())
        assertTrue(MediaFormat.DNG.hasPreview())

        assertFalse(MediaFormat.JPEG.hasPreview())
        assertFalse(MediaFormat.PNG.hasPreview())
        assertFalse(MediaFormat.HEIC.hasPreview())
        assertFalse(MediaFormat.AVIF.hasPreview())
        assertFalse(MediaFormat.JXL.hasPreview())
    }

    @Test
    fun testHasPreviewForAllFormats() {

        /* Every format is either a preview-capable RAW or not. */
        for (format in MediaFormat.entries) {

            val isRawWithPreview = format in setOf(
                MediaFormat.CR2, MediaFormat.CR3, MediaFormat.RAF,
                MediaFormat.NEF, MediaFormat.ARW, MediaFormat.RW2,
                MediaFormat.ORF, MediaFormat.DNG
            )

            assertEquals(isRawWithPreview, format.hasPreview(), "${format.name} hasPreview() mismatch.")
        }
    }

    @Test
    fun testHasValidFileNameExtension() {

        assertTrue(MediaFormat.hasValidFileNameExtension("photo.jpg"))
        assertTrue(MediaFormat.hasValidFileNameExtension("photo.JPG"))
        assertTrue(MediaFormat.hasValidFileNameExtension("photo.cr3"))
        assertTrue(MediaFormat.hasValidFileNameExtension("photo.webp"))

        assertFalse(MediaFormat.hasValidFileNameExtension("photo.txt"))
        assertFalse(MediaFormat.hasValidFileNameExtension("photo"))
    }

    @Test
    fun testAllFileNameExtensions() {

        assertTrue("jpg" in MediaFormat.allFileNameExtensions)
        assertTrue("jpeg" in MediaFormat.allFileNameExtensions)
        assertTrue("tiff" in MediaFormat.allFileNameExtensions)
        assertTrue("cr3" in MediaFormat.allFileNameExtensions)
        assertTrue("mov" in MediaFormat.allFileNameExtensions)
        assertTrue("mp4" in MediaFormat.allFileNameExtensions)
        assertTrue("pdf" in MediaFormat.allFileNameExtensions)
    }

    @Test
    fun testDetectNameOrReturnHex() {

        assertEquals(
            expected = "JPEG",
            actual = MediaFormat.detectNameOrReturnHex(KimTestData.getBytesOf(1))
        )

        assertEquals(
            expected = "CR3",
            actual = MediaFormat.detectNameOrReturnHex(KimTestData.getBytesOf(KimTestData.CR3_TEST_IMAGE_INDEX))
        )

        /* Short arrays show the hex presentation. */
        val hexResult = MediaFormat.detectNameOrReturnHex(byteArrayOf(0x01, 0x02, 0x03))
        assertTrue(hexResult.isNotEmpty())
    }

    @Test
    fun testByFileNameExtensionAdditional() {

        assertEquals(MediaFormat.JXL, MediaFormat.byFileNameExtension("image.jxl"))
        assertEquals(MediaFormat.CR3, MediaFormat.byFileNameExtension("image.cr3"))
        assertEquals(MediaFormat.MOV, MediaFormat.byFileNameExtension("image.mov"))
        assertEquals(MediaFormat.MP4, MediaFormat.byFileNameExtension("image.mp4"))
        assertEquals(MediaFormat.PDF, MediaFormat.byFileNameExtension("image.pdf"))
        assertEquals(MediaFormat.AVIF, MediaFormat.byFileNameExtension("image.avif"))
    }

    @Test
    fun testDetect() {

        for (index in 1..KimTestData.TEST_MEDIA_COUNT) {

            val expectedFileType = when {
                index <= KimTestData.HIGHEST_JPEG_INDEX -> MediaFormat.JPEG
                index == KimTestData.GIF_TEST_IMAGE_INDEX -> MediaFormat.GIF
                index == KimTestData.WEBP_TEST_IMAGE_INDEX -> MediaFormat.WEBP
                index == KimTestData.PNG_TEST_IMAGE_INDEX -> MediaFormat.PNG
                index == KimTestData.TIFF_NONE_TEST_IMAGE_INDEX -> MediaFormat.TIFF
                index == KimTestData.TIFF_ZIP_TEST_IMAGE_INDEX -> MediaFormat.TIFF
                index == KimTestData.TIFF_LZW_TEST_IMAGE_INDEX -> MediaFormat.TIFF
                index == KimTestData.PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX -> MediaFormat.PNG
                index == KimTestData.PNG_GIMP_TEST_IMAGE_INDEX -> MediaFormat.PNG
                index == KimTestData.CR2_TEST_IMAGE_INDEX -> MediaFormat.CR2
                index == KimTestData.RAF_TEST_IMAGE_INDEX -> MediaFormat.RAF
                index == KimTestData.RW2_TEST_IMAGE_INDEX -> MediaFormat.RW2
                index == KimTestData.ORF_TEST_IMAGE_INDEX -> MediaFormat.ORF
                /* NEF, ARW and DNG do not have unique magic bytes and recognized as TIFF. */
                index == KimTestData.NEF_TEST_IMAGE_INDEX -> MediaFormat.TIFF
                index == KimTestData.ARW_TEST_IMAGE_INDEX -> MediaFormat.TIFF
                index == KimTestData.DNG_CR2_TEST_IMAGE_INDEX -> MediaFormat.TIFF
                index == KimTestData.DNG_RAF_TEST_IMAGE_INDEX -> MediaFormat.TIFF
                index == KimTestData.DNG_NEF_TEST_IMAGE_INDEX -> MediaFormat.TIFF
                index == KimTestData.DNG_ARW_TEST_IMAGE_INDEX -> MediaFormat.TIFF
                index == KimTestData.DNG_RW2_TEST_IMAGE_INDEX -> MediaFormat.TIFF
                index == KimTestData.DNG_ORF_TEST_IMAGE_INDEX -> MediaFormat.TIFF
                index == KimTestData.HEIC_TEST_IMAGE_INDEX -> MediaFormat.HEIC
                index == KimTestData.HIF_TEST_IMAGE_INDEX -> MediaFormat.HEIC
                index == KimTestData.HEIC_TEST_IMAGE_WITH_XMP_INDEX -> MediaFormat.HEIC
                index == KimTestData.AVIF_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX -> MediaFormat.AVIF
                index == KimTestData.HEIC_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX -> MediaFormat.HEIC
                index == KimTestData.HEIC_TEST_IMAGE_FROM_JPG_USING_APPLE_INDEX -> MediaFormat.HEIC
                index == KimTestData.HEIC_TEST_IMAGE_FROM_SAMSUNG_INDEX -> MediaFormat.HEIC
                index == KimTestData.JXL_CONTAINER_DARKTABLE_INDEX -> MediaFormat.JXL
                index == KimTestData.JXL_CONTAINER_UNCOMPRESSED_INDEX -> MediaFormat.JXL
                index == KimTestData.JXL_CONTAINER_COMPRESSED_INDEX -> MediaFormat.JXL
                index == KimTestData.GEOTIFF_PIXEL_SCALING_INDEX -> MediaFormat.TIFF
                index == KimTestData.GEOTIFF_AFFINE_TRANSFORM_INDEX -> MediaFormat.TIFF
                index == KimTestData.CR3_TEST_IMAGE_INDEX -> MediaFormat.CR3
                index == KimTestData.MP4_TEST_VIDEO_INDEX -> MediaFormat.MP4
                index == KimTestData.MOV_TEST_VIDEO_INDEX -> MediaFormat.MOV
                index == KimTestData.ANIMATED_AVIF_TEST_IMAGE_INDEX -> MediaFormat.AVIF
                index == KimTestData.ANIMATED_AVIF_TEST_IMAGE_WITH_LEGACY_ADOBE_XMP_INDEX -> MediaFormat.AVIF
                index == KimTestData.ANIMATED_AVIF_TEST_IMAGE_WITH_ALT_LEGACY_ADOBE_XMP_INDEX -> MediaFormat.AVIF
                else -> null
            }

            val bytes = KimTestData.getBytesOf(index)

            val actualFileType = MediaFormat.detect(bytes)

            assertEquals(expectedFileType, actualFileType, "Media $index has a different type.")
        }
    }

    /**
     * Regression test: the mp41 and iso2 brand constants must match their own
     * brands, not mp42 and isom.
     */
    @Test
    fun testDetectMp41AndIso2Brands() {

        val mp41Header = byteArrayOf(0, 0, 0, 24) +
            "ftypmp41".encodeToByteArray() +
            "mp41".encodeToByteArray()

        val iso2Header = byteArrayOf(0, 0, 0, 24) +
            "ftypiso2".encodeToByteArray() +
            "iso2".encodeToByteArray()

        assertEquals(MediaFormat.MP4, MediaFormat.detect(mp41Header))
        assertEquals(MediaFormat.MP4, MediaFormat.detect(iso2Header))
    }

    /**
     * The naked JXL codestream signature must be detected as JXL just
     * like the ISOBMFF container variant.
     */
    @Test
    fun testDetectJxlNakedCodestream() {

        val header = byteArrayOf(0xFF.toByte(), 0x0A.toByte()) +
            ByteArray(16)

        assertEquals(MediaFormat.JXL, MediaFormat.detect(header))
    }

    @Test
    fun testByMimeType() {

        assertNull(MediaFormat.byMimeType("invalid"))

        assertEquals(
            expected = MediaFormat.JPEG,
            actual = MediaFormat.byMimeType("image/jpeg")
        )

        assertEquals(
            expected = MediaFormat.GIF,
            actual = MediaFormat.byMimeType("image/gif")
        )

        assertEquals(
            expected = MediaFormat.PNG,
            actual = MediaFormat.byMimeType("image/png")
        )

        assertEquals(
            expected = MediaFormat.WEBP,
            actual = MediaFormat.byMimeType("image/webp")
        )

        assertEquals(
            expected = MediaFormat.TIFF,
            actual = MediaFormat.byMimeType("image/tiff")
        )

        assertEquals(
            expected = MediaFormat.HEIC,
            actual = MediaFormat.byMimeType("image/heic")
        )

        assertEquals(
            expected = MediaFormat.CR2,
            actual = MediaFormat.byMimeType("image/x-canon-cr2")
        )

        /* OneDrive returns this wrong mime type. */
        assertEquals(
            expected = MediaFormat.CR2,
            actual = MediaFormat.byMimeType("image/CR2")
        )

        assertEquals(
            expected = MediaFormat.RAF,
            actual = MediaFormat.byMimeType("image/x-fuji-raf")
        )

        assertEquals(
            expected = MediaFormat.NEF,
            actual = MediaFormat.byMimeType("image/x-nikon-nef")
        )

        assertEquals(
            expected = MediaFormat.ARW,
            actual = MediaFormat.byMimeType("image/x-sony-arw")
        )

        assertEquals(
            expected = MediaFormat.RW2,
            actual = MediaFormat.byMimeType("image/x-panasonic-rw2")
        )

        assertEquals(
            expected = MediaFormat.ORF,
            actual = MediaFormat.byMimeType("image/x-olympus-orf")
        )

        assertEquals(
            expected = MediaFormat.DNG,
            actual = MediaFormat.byMimeType("image/x-adobe-dng")
        )
    }

    @Test
    fun testByUniformTypeIdentifier() {

        assertNull(MediaFormat.byUniformTypeIdentifier("invalid"))

        assertEquals(
            expected = MediaFormat.JPEG,
            actual = MediaFormat.byUniformTypeIdentifier("public.jpeg")
        )

        assertEquals(
            expected = MediaFormat.GIF,
            actual = MediaFormat.byUniformTypeIdentifier("com.compuserve.gif")
        )

        assertEquals(
            expected = MediaFormat.PNG,
            actual = MediaFormat.byUniformTypeIdentifier("public.png")
        )

        assertEquals(
            expected = MediaFormat.WEBP,
            actual = MediaFormat.byUniformTypeIdentifier("org.webmproject.webp")
        )

        assertEquals(
            expected = MediaFormat.TIFF,
            actual = MediaFormat.byUniformTypeIdentifier("public.tiff")
        )

        assertEquals(
            expected = MediaFormat.HEIC,
            actual = MediaFormat.byUniformTypeIdentifier("public.heic")
        )

        assertEquals(
            expected = MediaFormat.CR2,
            actual = MediaFormat.byUniformTypeIdentifier("com.canon.cr2-raw-image")
        )

        assertEquals(
            expected = MediaFormat.RAF,
            actual = MediaFormat.byUniformTypeIdentifier("com.fuji.raw-image")
        )

        assertEquals(
            expected = MediaFormat.NEF,
            actual = MediaFormat.byUniformTypeIdentifier("com.nikon.raw-image")
        )

        assertEquals(
            expected = MediaFormat.ARW,
            actual = MediaFormat.byUniformTypeIdentifier("com.sony.raw-image")
        )

        assertEquals(
            expected = MediaFormat.RW2,
            actual = MediaFormat.byUniformTypeIdentifier("com.panasonic.raw-image")
        )

        assertEquals(
            expected = MediaFormat.ORF,
            actual = MediaFormat.byUniformTypeIdentifier("com.olympus.raw-image")
        )

        assertEquals(
            expected = MediaFormat.DNG,
            actual = MediaFormat.byUniformTypeIdentifier("com.adobe.raw-image")
        )
    }

    @Test
    fun testByFileNameExtension() {

        assertNull(MediaFormat.byFileNameExtension("invalid"))

        assertEquals(
            expected = MediaFormat.JPEG,
            actual = MediaFormat.byFileNameExtension("image.jpeg")
        )

        assertEquals(
            expected = MediaFormat.JPEG,
            actual = MediaFormat.byFileNameExtension("image.jpg")
        )

        assertEquals(
            expected = MediaFormat.JPEG,
            actual = MediaFormat.byFileNameExtension("image.JPG")
        )

        assertEquals(
            expected = MediaFormat.GIF,
            actual = MediaFormat.byFileNameExtension("image.gif")
        )

        assertEquals(
            expected = MediaFormat.PNG,
            actual = MediaFormat.byFileNameExtension("image.png")
        )

        assertEquals(
            expected = MediaFormat.WEBP,
            actual = MediaFormat.byFileNameExtension("image.webp")
        )

        assertEquals(
            expected = MediaFormat.TIFF,
            actual = MediaFormat.byFileNameExtension("image.tif")
        )

        assertEquals(
            expected = MediaFormat.TIFF,
            actual = MediaFormat.byFileNameExtension("image.tiff")
        )

        assertEquals(
            expected = MediaFormat.HEIC,
            actual = MediaFormat.byFileNameExtension("image.heic")
        )

        assertEquals(
            expected = MediaFormat.CR2,
            actual = MediaFormat.byFileNameExtension("image.cr2")
        )

        assertEquals(
            expected = MediaFormat.RAF,
            actual = MediaFormat.byFileNameExtension("image.raf")
        )

        assertEquals(
            expected = MediaFormat.NEF,
            actual = MediaFormat.byFileNameExtension("image.nef")
        )

        assertEquals(
            expected = MediaFormat.ARW,
            actual = MediaFormat.byFileNameExtension("image.arw")
        )

        assertEquals(
            expected = MediaFormat.RW2,
            actual = MediaFormat.byFileNameExtension("image.rw2")
        )

        assertEquals(
            expected = MediaFormat.ORF,
            actual = MediaFormat.byFileNameExtension("image.orf")
        )

        assertEquals(
            expected = MediaFormat.DNG,
            actual = MediaFormat.byFileNameExtension("image.dng")
        )
    }
}
