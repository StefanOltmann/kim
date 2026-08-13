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
package de.stefan_oltmann.kim.testdata

import com.goncalossilva.resources.Resource

/**
 * This object extracts the bundled test
 * photo files to a specified directory.
 * Unit Tests can find the images there and
 * also benchmark operations can happen.
 */
@Suppress("TooManyFunctions")
object KimTestData {

    private const val RESOURCE_PATH: String = "de/stefan_oltmann/kim/testdata"

    const val TEST_MEDIA_COUNT: Int = 88
    const val HIGHEST_JPEG_INDEX: Int = 50

    const val PNG_TEST_IMAGE_INDEX: Int = 51
    const val PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX: Int = 52
    const val PNG_GIMP_TEST_IMAGE_INDEX: Int = 53
    const val TIFF_NONE_TEST_IMAGE_INDEX: Int = 54
    const val TIFF_ZIP_TEST_IMAGE_INDEX: Int = 55
    const val TIFF_LZW_TEST_IMAGE_INDEX: Int = 56
    const val CR2_TEST_IMAGE_INDEX: Int = 57
    const val RAF_TEST_IMAGE_INDEX: Int = 58
    const val WEBP_TEST_IMAGE_INDEX: Int = 59
    const val HEIC_TEST_IMAGE_INDEX: Int = 60
    const val GIF_TEST_IMAGE_INDEX: Int = 61
    const val NEF_TEST_IMAGE_INDEX: Int = 62
    const val ARW_TEST_IMAGE_INDEX: Int = 63
    const val RW2_TEST_IMAGE_INDEX: Int = 64
    const val ORF_TEST_IMAGE_INDEX: Int = 65
    const val DNG_CR2_TEST_IMAGE_INDEX: Int = 66
    const val DNG_RAF_TEST_IMAGE_INDEX: Int = 67
    const val DNG_NEF_TEST_IMAGE_INDEX: Int = 68
    const val DNG_ARW_TEST_IMAGE_INDEX: Int = 69
    const val DNG_RW2_TEST_IMAGE_INDEX: Int = 70
    const val DNG_ORF_TEST_IMAGE_INDEX: Int = 71
    const val HIF_TEST_IMAGE_INDEX: Int = 72
    const val HEIC_TEST_IMAGE_WITH_XMP_INDEX: Int = 73
    const val AVIF_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX: Int = 74
    const val HEIC_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX: Int = 75
    const val HEIC_TEST_IMAGE_FROM_JPG_USING_APPLE_INDEX: Int = 76
    const val HEIC_TEST_IMAGE_FROM_SAMSUNG_INDEX: Int = 77
    const val JXL_CONTAINER_DARKTABLE_INDEX: Int = 78
    const val JXL_CONTAINER_UNCOMPRESSED_INDEX: Int = 79
    const val JXL_CONTAINER_COMPRESSED_INDEX: Int = 80
    const val GEOTIFF_PIXEL_SCALING_INDEX: Int = 81
    const val GEOTIFF_AFFINE_TRANSFORM_INDEX: Int = 82
    const val CR3_TEST_IMAGE_INDEX: Int = 83
    const val MP4_TEST_VIDEO_INDEX: Int = 84
    const val MOV_TEST_VIDEO_INDEX: Int = 85
    const val ANIMATED_AVIF_TEST_IMAGE_INDEX: Int = 86
    const val ANIMATED_AVIF_TEST_IMAGE_WITH_LEGACY_ADOBE_XMP_INDEX: Int = 87
    const val ANIMATED_AVIF_TEST_IMAGE_WITH_ALT_LEGACY_ADOBE_XMP_INDEX: Int = 88

    @Suppress("MagicNumber")
    val mediaIdsWithExifThumbnail: Set<Int> = setOf(
        2, 3, 4, 5, 6, 7, 10, 12, 15, 16, 19, 20, 21,
        22, 24, 25, 27, 28, 29, 30, 31, 32, 33, 35, 37,
        38, 39, 40, 41, 42, 46, 48, 49, 50,
        PNG_TEST_IMAGE_INDEX,
        PNG_GIMP_TEST_IMAGE_INDEX,
        CR2_TEST_IMAGE_INDEX,
        RAF_TEST_IMAGE_INDEX,
        ARW_TEST_IMAGE_INDEX,
        RW2_TEST_IMAGE_INDEX,
        ORF_TEST_IMAGE_INDEX,
        AVIF_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX,
        HEIC_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX,

//        JXL_NAKED_BYTESTREAM_UNCOMPRESSED_INDEX,
//        JXL_CONTAINER_UNCOMPRESSED_INDEX,
//        JXL_CONTAINER_COMPRESSED_INDEX,

        // FIXME DNG have thumbnails. Extraction logic does not fit here.
//        DNG_CR2_TEST_IMAGE_INDEX,
//        DNG_RAF_TEST_IMAGE_INDEX,
//        DNG_NEF_TEST_IMAGE_INDEX,
//        DNG_ARW_TEST_IMAGE_INDEX,
//        DNG_RW2_TEST_IMAGE_INDEX,
//        DNG_ORF_TEST_IMAGE_INDEX
    )

    val pngPhotoIds: Set<Int> = setOf(
        PNG_TEST_IMAGE_INDEX,
        PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX,
        PNG_GIMP_TEST_IMAGE_INDEX
    )

    val jxlPhotoIds: Set<Int> = setOf(
        JXL_CONTAINER_DARKTABLE_INDEX,
        JXL_CONTAINER_UNCOMPRESSED_INDEX,
        JXL_CONTAINER_COMPRESSED_INDEX
    )

    val webpPhotoIds: Set<Int> = setOf(
        WEBP_TEST_IMAGE_INDEX
    )

    private fun getExtension(index: Int) = when (index) {
        GIF_TEST_IMAGE_INDEX -> "gif"
        WEBP_TEST_IMAGE_INDEX -> "webp"
        HEIC_TEST_IMAGE_INDEX -> "heic"
        HEIC_TEST_IMAGE_WITH_XMP_INDEX -> "heic"
        AVIF_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX -> "avif"
        HEIC_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX -> "heic"
        HEIC_TEST_IMAGE_FROM_JPG_USING_APPLE_INDEX -> "heic"
        HEIC_TEST_IMAGE_FROM_SAMSUNG_INDEX -> "heic"
        CR2_TEST_IMAGE_INDEX -> "cr2"
        RAF_TEST_IMAGE_INDEX -> "raf"
        TIFF_NONE_TEST_IMAGE_INDEX -> "tif"
        TIFF_ZIP_TEST_IMAGE_INDEX -> "tif"
        TIFF_LZW_TEST_IMAGE_INDEX -> "tif"
        PNG_TEST_IMAGE_INDEX -> "png"
        PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX -> "png"
        PNG_GIMP_TEST_IMAGE_INDEX -> "png"
        NEF_TEST_IMAGE_INDEX -> "nef"
        ARW_TEST_IMAGE_INDEX -> "arw"
        RW2_TEST_IMAGE_INDEX -> "rw2"
        ORF_TEST_IMAGE_INDEX -> "orf"
        DNG_CR2_TEST_IMAGE_INDEX -> "dng"
        DNG_RAF_TEST_IMAGE_INDEX -> "dng"
        DNG_NEF_TEST_IMAGE_INDEX -> "dng"
        DNG_ARW_TEST_IMAGE_INDEX -> "dng"
        DNG_RW2_TEST_IMAGE_INDEX -> "dng"
        DNG_ORF_TEST_IMAGE_INDEX -> "dng"
        HIF_TEST_IMAGE_INDEX -> "hif"
        JXL_CONTAINER_DARKTABLE_INDEX -> "jxl"
        JXL_CONTAINER_UNCOMPRESSED_INDEX -> "jxl"
        JXL_CONTAINER_COMPRESSED_INDEX -> "jxl"
        GEOTIFF_PIXEL_SCALING_INDEX -> "tif"
        GEOTIFF_AFFINE_TRANSFORM_INDEX -> "tif"
        CR3_TEST_IMAGE_INDEX -> "cr3"
        MP4_TEST_VIDEO_INDEX -> "mp4"
        MOV_TEST_VIDEO_INDEX -> "mov"
        ANIMATED_AVIF_TEST_IMAGE_INDEX -> "avif"
        ANIMATED_AVIF_TEST_IMAGE_WITH_LEGACY_ADOBE_XMP_INDEX -> "avif"
        ANIMATED_AVIF_TEST_IMAGE_WITH_ALT_LEGACY_ADOBE_XMP_INDEX -> "avif"
        else -> "jpg"
    }

    fun getFileName(index: Int): String = "media_$index.${getExtension(index)}"

    fun getBytesOf(index: Int): ByteArray =
        getBytesOf(getFileName(index))

    fun getBytesOf(fileName: String): ByteArray =
        Resource("$RESOURCE_PATH/full/$fileName").readBytes()

    fun getHeaderBytesOf(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/headers/media_${index}_header.${getExtension(index)}").readBytes()

    fun getModifiedBytesOf(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/modified/media_${index}_modified.${getExtension(index)}").readBytes()

    /**
     * Returns whether a modified version of the given media file is available.
     */
    fun hasModifiedBytesOf(index: Int): Boolean =
        Resource("$RESOURCE_PATH/modified/media_${index}_modified.${getExtension(index)}").exists()

    /**
     * Returns the committed metadata text dump of the modified media file.
     */
    fun getModifiedToStringText(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/modified/media_${index}_modified.txt").readBytes()

    fun getExifThumbnailBytesOf(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/exifthumbs/media_${index}_exifthumb.jpg").readBytes()

    fun getPreviewBytesOf(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/previews/media_${index}_preview.jpg").readBytes()

    fun getHeaderExifBytesOf(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/headers/media_${index}_header_exif.tif").readBytes()

    fun getHeaderTextFile(index: Int, identifier: String): String =
        Resource("$RESOURCE_PATH/headers/media_${index}_header_$identifier.txt").readBytes().decodeToString()

    fun getToStringText(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/txt/media_$index.txt").readBytes()

    fun getXmp(fileName: String): String =
        Resource("$RESOURCE_PATH/xmp/$fileName").readBytes().decodeToString()

    fun getOriginalXmp(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/xmp/media_$index.xmp").readBytes()

    fun getFormattedXmp(index: Int): String =
        Resource("$RESOURCE_PATH/xmp/media_${index}_formatted.xmp").readBytes().decodeToString()

    fun getMetadataCsvString(): String =
        Resource("$RESOURCE_PATH/metadata.csv").readBytes().decodeToString()
}
