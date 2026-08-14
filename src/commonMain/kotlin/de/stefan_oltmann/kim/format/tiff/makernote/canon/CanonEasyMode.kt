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
package de.stefan_oltmann.kim.format.tiff.makernote.canon

/**
 * Values of the Canon EasyMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonEasyMode(
    public val value: Int,
    public val displayName: String
) {

    FULL_AUTO(0, "Full auto"),
    MANUAL(1, "Manual"),
    LANDSCAPE(2, "Landscape"),
    FAST_SHUTTER(3, "Fast shutter"),
    SLOW_SHUTTER(4, "Slow shutter"),
    NIGHT(5, "Night"),
    GRAY_SCALE(6, "Gray Scale"),
    SEPIA(7, "Sepia"),
    PORTRAIT(8, "Portrait"),
    SPORTS(9, "Sports"),
    MACRO(10, "Macro"),
    BLACK_WHITE(11, "Black & White"),
    PAN_FOCUS(12, "Pan focus"),
    VIVID(13, "Vivid"),
    NEUTRAL(14, "Neutral"),
    FLASH_OFF(15, "Flash Off"),
    LONG_SHUTTER(16, "Long Shutter"),
    SUPER_MACRO(17, "Super Macro"),
    FOLIAGE(18, "Foliage"),
    INDOOR(19, "Indoor"),
    FIREWORKS(20, "Fireworks"),
    BEACH(21, "Beach"),
    UNDERWATER(22, "Underwater"),
    SNOW(23, "Snow"),
    KIDS_PETS(24, "Kids & Pets"),
    NIGHT_SNAPSHOT(25, "Night Snapshot"),
    DIGITAL_MACRO(26, "Digital Macro"),
    MY_COLORS(27, "My Colors"),
    MOVIE_SNAP(28, "Movie Snap"),
    SUPER_MACRO_2(29, "Super Macro 2"),
    COLOR_ACCENT(30, "Color Accent"),
    COLOR_SWAP(31, "Color Swap"),
    AQUARIUM(32, "Aquarium"),
    ISO_3200(33, "ISO 3200"),
    ISO_6400(34, "ISO 6400"),
    CREATIVE_LIGHT_EFFECT(35, "Creative Light Effect"),
    EASY(36, "Easy"),
    QUICK_SHOT(37, "Quick Shot"),
    CREATIVE_AUTO(38, "Creative Auto"),
    ZOOM_BLUR(39, "Zoom Blur"),
    LOW_LIGHT(40, "Low Light"),
    NOSTALGIC(41, "Nostalgic"),
    SUPER_VIVID(42, "Super Vivid"),
    POSTER_EFFECT(43, "Poster Effect"),
    FACE_SELF_TIMER(44, "Face Self-timer"),
    SMILE(45, "Smile"),
    WINK_SELF_TIMER(46, "Wink Self-timer"),
    FISHEYE_EFFECT(47, "Fisheye Effect"),
    MINIATURE_EFFECT(48, "Miniature Effect"),
    HIGH_SPEED_BURST(49, "High-speed Burst"),
    BEST_IMAGE_SELECTION(50, "Best Image Selection"),
    HIGH_DYNAMIC_RANGE(51, "High Dynamic Range"),
    HANDHELD_NIGHT_SCENE(52, "Handheld Night Scene"),
    MOVIE_DIGEST(53, "Movie Digest"),
    LIVE_VIEW_CONTROL(54, "Live View Control"),
    DISCREET(55, "Discreet"),
    BLUR_REDUCTION(56, "Blur Reduction"),
    MONOCHROME(57, "Monochrome"),
    TOY_CAMERA_EFFECT(58, "Toy Camera Effect"),
    SCENE_INTELLIGENT_AUTO(59, "Scene Intelligent Auto"),
    HIGH_SPEED_BURST_HQ(60, "High-speed Burst HQ"),
    SMOOTH_SKIN(61, "Smooth Skin"),
    SOFT_FOCUS(62, "Soft Focus"),
    FOOD(68, "Food"),
    HDR_ART_STANDARD(84, "HDR Art Standard"),
    HDR_ART_VIVID(85, "HDR Art Vivid"),
    HDR_ART_BOLD(93, "HDR Art Bold"),
    SPOTLIGHT(257, "Spotlight"),
    NIGHT_2(258, "Night 2"),
    NIGHT_3(259, "Night+"),
    SUPER_NIGHT(260, "Super Night"),
    SUNSET(261, "Sunset"),
    NIGHT_SCENE(263, "Night Scene"),
    SURFACE(264, "Surface"),
    LOW_LIGHT_2(265, "Low Light 2");

    public companion object {

        public fun fromValue(value: Int): CanonEasyMode? =
            entries.firstOrNull { it.value == value }
    }
}
