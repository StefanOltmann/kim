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
package de.stefan_oltmann.kim.format.tiff.makernote.nikon

/**
 * Values of the Nikon CropHiSpeed tag.
 */
public enum class NikonCropHiSpeed(
    public val value: Int,
    public val description: String
) {

    OFF(0, "Off"),
    CROP_1_3X(1, "1.3x Crop"),
    DX_CROP(2, "DX Crop"),
    CROP_5_4(3, "5:4 Crop"),
    CROP_3_2(4, "3:2 Crop"),
    CROP_16_9(6, "16:9 Crop"),
    CROP_2_7X(8, "2.7x Crop"),
    DX_MOVIE_16_9(9, "DX Movie 16:9 Crop"),
    CROP_1_3X_MOVIE(10, "1.3x Movie Crop"),
    FX_UNCROPPED(11, "FX Uncropped"),
    DX_UNCROPPED(12, "DX Uncropped"),
    CROP_2_8X_MOVIE(13, "2.8x Movie Crop"),
    CROP_1_4X_MOVIE(14, "1.4x Movie Crop"),
    CROP_1_5X_MOVIE(15, "1.5x Movie Crop"),
    FX_1_1(17, "FX 1:1 Crop"),
    DX_1_1(18, "DX 1:1 Crop");

    public companion object {

        public fun fromValue(value: Int): NikonCropHiSpeed? =
            entries.firstOrNull { it.value == value }
    }
}
