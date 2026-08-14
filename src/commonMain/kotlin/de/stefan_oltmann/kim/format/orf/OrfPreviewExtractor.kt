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
package de.stefan_oltmann.kim.format.orf

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.TiffPreviewExtractor
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.TiffConstants
import de.stefan_oltmann.kim.format.tiff.makernote.olympus.OlympusCameraSettingsTag
import de.stefan_oltmann.kim.input.RandomAccessByteReader

/**
 * Extracts the preview image of Olympus ORF files.
 *
 * The preview offset and length are stored in the CameraSettings
 * sub-directory of the Olympus MakerNote. The offset is relative
 * to the start of the MakerNote data.
 */
public object OrfPreviewExtractor : TiffPreviewExtractor {

    @Throws(ImageReadException::class)
    override fun extractPreviewImage(
        tiffContents: TiffContents,
        randomAccessByteReader: RandomAccessByteReader
    ): ByteArray? = tryWithImageReadException {

        val cameraSettingsDirectory = tiffContents.findMakerNoteSubDirectory(
            TiffConstants.TIFF_MAKER_NOTE_OLYMPUS_CAMERA_SETTINGS
        ) ?: return null

        if (cameraSettingsDirectory.findField(OlympusCameraSettingsTag.PREVIEW_IMAGE_VALID)?.toInt() != 1)
            return null

        val makerNoteStart =
            tiffContents.findTiffField(ExifTag.EXIF_TAG_MAKER_NOTE)?.valueOffset ?: return null

        val previewImageStart =
            cameraSettingsDirectory.findField(OlympusCameraSettingsTag.PREVIEW_IMAGE_START)?.toInt() ?: return null

        val previewImageLength =
            cameraSettingsDirectory.findField(OlympusCameraSettingsTag.PREVIEW_IMAGE_LENGTH)?.toInt() ?: return null

        if (previewImageLength == 0)
            return null

        randomAccessByteReader.moveTo(makerNoteStart + previewImageStart)

        return@tryWithImageReadException randomAccessByteReader.readBytes(previewImageLength)
    }
}
