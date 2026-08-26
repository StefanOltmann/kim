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

import de.stefan_oltmann.kim.common.startsWith
import de.stefan_oltmann.kim.common.startsWithNullable
import de.stefan_oltmann.kim.common.toSingleNumberHexes
import de.stefan_oltmann.kim.format.MediaFormatMagicNumbers
import kotlin.jvm.JvmStatic

/**
 * The media file formats supported by Kim.
 */
public enum class MediaFormat(
    public val type: MediaFormatType,
    public val mimeType: String,
    public val uniformTypeIdentifier: String,
    public val fileNameExtensions: Set<String>
) {

    /* Images */
    JPEG(MediaFormatType.IMAGE, "image/jpeg", "public.jpeg", setOf("jpg", "jpeg")),
    GIF(MediaFormatType.IMAGE, "image/gif", "com.compuserve.gif", setOf("gif")),
    PNG(MediaFormatType.IMAGE, "image/png", "public.png", setOf("png")),
    WEBP(MediaFormatType.IMAGE, "image/webp", "org.webmproject.webp", setOf("webp")),
    TIFF(MediaFormatType.IMAGE, "image/tiff", "public.tiff", setOf("tif", "tiff")),
    HEIC(MediaFormatType.IMAGE, "image/heic", "public.heic", setOf("heic")),
    AVIF(MediaFormatType.IMAGE, "image/avif", "public.avif", setOf("avif")),
    CR2(MediaFormatType.IMAGE, "image/x-canon-cr2", "com.canon.cr2-raw-image", setOf("cr2")),
    CR3(MediaFormatType.IMAGE, "image/x-canon-cr3", "com.canon.cr3", setOf("cr3")),
    RAF(MediaFormatType.IMAGE, "image/x-fuji-raf", "com.fuji.raw-image", setOf("raf")),
    NEF(MediaFormatType.IMAGE, "image/x-nikon-nef", "com.nikon.raw-image", setOf("nef")),
    ARW(MediaFormatType.IMAGE, "image/x-sony-arw", "com.sony.raw-image", setOf("arw")),
    RW2(MediaFormatType.IMAGE, "image/x-panasonic-rw2", "com.panasonic.raw-image", setOf("rw2")),
    ORF(MediaFormatType.IMAGE, "image/x-olympus-orf", "com.olympus.raw-image", setOf("orf")),
    DNG(MediaFormatType.IMAGE, "image/x-adobe-dng", "com.adobe.raw-image", setOf("dng")),
    JXL(MediaFormatType.IMAGE, "image/jxl", "public.jxl", setOf("jxl")),

    /* Videos */
    MOV(MediaFormatType.VIDEO, "video/quicktime", "com.apple.quicktime-movie", setOf("mov")),
    MP4(MediaFormatType.VIDEO, "video/mp4", "public.mpeg-4", setOf("mp4")),

    /* Documents */
    PDF(MediaFormatType.DOCUMENT, "application/pdf", "com.adobe.pdf", setOf("pdf"));

    /**
     * If true, KIM can embedd metadata into this kind of file.
     */
    public fun isMetadataEmbeddable(): Boolean =
        this == JPEG || this == PNG || this == WEBP || this == JXL

    /**
     * If true, this kind of file usually contains a preview image that we can extract.
     * Use Kim.extractPreviewImage() API for this.
     */
    public fun hasPreview(): Boolean =
        this == CR2 || this == CR3 || this == RAF || this == NEF ||
            this == ARW || this == RW2 || this == ORF || this == DNG

    public companion object {

        /**
         * RAF is the longest format that requires us to read 16 bytes to detect it.
         * Right after that we need 12 bytes to check for HEIC.
         */
        public const val REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION: Int = 16

        private val allMediaFormats = MediaFormat.entries

        public val allFileNameExtensions: Set<String> = computeAllFileNameExtensions()

        /*
         * OneDrive reports RAW files under wrong mime types
         */
        private const val CR2_ONEDRIVE_MIME_TYPE = "image/CR2"
        private const val CR3_ONEDRIVE_MIME_TYPE = "image/CR3"
        private const val RAF_ONEDRIVE_MIME_TYPE = "image/RAF"
        private const val NEF_ONEDRIVE_MIME_TYPE = "image/NEF"
        private const val ARW_ONEDRIVE_MIME_TYPE = "image/ARW"
        private const val RW2_ONEDRIVE_MIME_TYPE = "image/RW2"
        private const val ORF_ONEDRIVE_MIME_TYPE = "image/ORF"
        private const val DNG_ONEDRIVE_MIME_TYPE = "image/DNG"

        private fun computeAllFileNameExtensions(): MutableSet<String> {

            val fileNameExtensions = mutableSetOf<String>()

            for (fileType in allMediaFormats)
                for (extension in fileType.fileNameExtensions)
                    fileNameExtensions.add(extension)

            return fileNameExtensions
        }

        @JvmStatic
        public fun hasValidFileNameExtension(fileName: String): Boolean {

            for (extension in allFileNameExtensions)
                if (fileName.endsWith(".$extension", ignoreCase = true))
                    return true

            return false
        }

        @JvmStatic
        public fun byMimeType(mimeType: String): MediaFormat? {

            for (fileType in allMediaFormats)
                if (mimeType.contentEquals(fileType.mimeType, ignoreCase = true))
                    return fileType

            return when (mimeType) {
                CR2_ONEDRIVE_MIME_TYPE -> CR2
                CR3_ONEDRIVE_MIME_TYPE -> CR3
                RAF_ONEDRIVE_MIME_TYPE -> RAF
                NEF_ONEDRIVE_MIME_TYPE -> NEF
                ARW_ONEDRIVE_MIME_TYPE -> ARW
                RW2_ONEDRIVE_MIME_TYPE -> RW2
                ORF_ONEDRIVE_MIME_TYPE -> ORF
                DNG_ONEDRIVE_MIME_TYPE -> DNG
                else -> null
            }
        }

        @JvmStatic
        public fun byUniformTypeIdentifier(
            uniformTypeIdentifier: String
        ): MediaFormat? {

            for (fileType in allMediaFormats)
                if (uniformTypeIdentifier.contentEquals(fileType.uniformTypeIdentifier, ignoreCase = true))
                    return fileType

            return null
        }

        @JvmStatic
        public fun byFileNameExtension(fileName: String): MediaFormat? {

            for (fileType in allMediaFormats)
                for (extension in fileType.fileNameExtensions)
                    if (fileName.endsWith(".$extension", ignoreCase = true))
                        return fileType

            return null
        }

        /**
         * Detects JPEG, GIF, PNG, TIFF & WEBP files based on the header bytes.
         *
         * If the byte array is less than REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION
         * (for example empty) than the detection returns null.
         */
        @JvmStatic
        public fun detect(bytes: ByteArray): MediaFormat? {

            /*
             * If empty or not enough bytes we can't detect the format and will return NULL.
             * We don't want to throw an Exception, because we can't change the fact that
             * a file is too short to be an image and also we don't want Kotlin/Native to crash.
             */
            if (bytes.size < REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)
                return null

            /*
             * We want to exit this detection early, so we order the
             * detections in a way that the first checks most likely hit.
             */
            return when {

                /* JPG is the most common format. Check this first. */
                bytes.startsWith(MediaFormatMagicNumbers.jpeg) -> JPEG

                /* Check other common formats. */
                bytes.startsWith(MediaFormatMagicNumbers.png) -> PNG
                bytes.startsWithNullable(MediaFormatMagicNumbers.webP) -> WEBP

                /* Canon CR2 et al *must* be checked before TIFF, because they are based on TIFF */
                bytes.startsWith(MediaFormatMagicNumbers.cr2) -> CR2
                bytes.startsWith(MediaFormatMagicNumbers.rw2) -> RW2
                bytes.startsWith(MediaFormatMagicNumbers.orf_iiro) -> ORF
                bytes.startsWith(MediaFormatMagicNumbers.orf_mmor) -> ORF
                bytes.startsWith(MediaFormatMagicNumbers.orf_iirs) -> ORF
                bytes.startsWith(MediaFormatMagicNumbers.raf) -> RAF

                /* Check TIFF after the RAW files. */
                bytes.startsWith(MediaFormatMagicNumbers.tiffLittleEndian) -> TIFF
                bytes.startsWith(MediaFormatMagicNumbers.tiffBigEndian) -> TIFF

                /* Check JXL ISOBMFF */
                bytes.startsWith(MediaFormatMagicNumbers.jxl) -> JXL

                /* JXL naked codestream (without ISOBMFF container) */
                bytes.startsWith(MediaFormatMagicNumbers.jxlCodeStream) -> JXL

                /* Check HEIC variants */
                bytes.startsWithNullable(MediaFormatMagicNumbers.heic) -> HEIC
                bytes.startsWithNullable(MediaFormatMagicNumbers.mif1) -> HEIC
                bytes.startsWithNullable(MediaFormatMagicNumbers.msf1) -> HEIC
                bytes.startsWithNullable(MediaFormatMagicNumbers.heix) -> HEIC
                bytes.startsWithNullable(MediaFormatMagicNumbers.hevc) -> HEIC
                bytes.startsWithNullable(MediaFormatMagicNumbers.hevx) -> HEIC

                /* Check AVIF */
                bytes.startsWithNullable(MediaFormatMagicNumbers.avif) -> AVIF
                bytes.startsWithNullable(MediaFormatMagicNumbers.avis) -> AVIF
                /* Check CR3 */
                bytes.startsWithNullable(MediaFormatMagicNumbers.cr3) -> CR3

                /* Check GIF and other unlikely formats... */
                bytes.startsWith(MediaFormatMagicNumbers.gif87a) -> GIF
                bytes.startsWith(MediaFormatMagicNumbers.gif89a) -> GIF

                /* Check MP4 video formats */
                bytes.startsWithNullable(MediaFormatMagicNumbers.mp42) -> MP4
                bytes.startsWithNullable(MediaFormatMagicNumbers.mp41) -> MP4
                bytes.startsWithNullable(MediaFormatMagicNumbers.isom) -> MP4
                bytes.startsWithNullable(MediaFormatMagicNumbers.iso2) -> MP4
                bytes.startsWithNullable(MediaFormatMagicNumbers.avc1) -> MP4

                /* Check QuickTime video format */
                bytes.startsWithNullable(MediaFormatMagicNumbers.qt) -> MOV

                /* Check document formats */
                bytes.startsWith(MediaFormatMagicNumbers.pdf) -> PDF

                else -> null
            }
        }

        /**
         * Method that helps with finding problems with file types.
         * It translates to a readable name or returns a hex presentation of the bytes.
         */
        @JvmStatic
        public fun detectNameOrReturnHex(byteArray: ByteArray): String =
            detect(byteArray)?.name ?: byteArray
                .take(REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)
                .toByteArray()
                .toSingleNumberHexes()
    }
}
