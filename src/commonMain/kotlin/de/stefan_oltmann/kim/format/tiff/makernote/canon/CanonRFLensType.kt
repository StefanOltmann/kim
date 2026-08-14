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
 * Values of the Canon RFLensType tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonRFLensType(
    public val value: Int,
    public val displayName: String
) {

    N_A(0, "n/a"),
    CANON_RF_50MM_F1_2_L_USM(257, "Canon RF 50mm F1.2L USM"),
    CANON_RF_24_105MM_F4_L_IS_USM(258, "Canon RF 24-105mm F4L IS USM"),
    CANON_RF_28_70MM_F2_L_USM(259, "Canon RF 28-70mm F2L USM"),
    CANON_RF_35MM_F1_8_MACRO_IS_STM(260, "Canon RF 35mm F1.8 MACRO IS STM"),
    CANON_RF_85MM_F1_2_L_USM(261, "Canon RF 85mm F1.2L USM"),
    CANON_RF_85MM_F1_2_L_USM_DS(262, "Canon RF 85mm F1.2L USM DS"),
    CANON_RF_24_70MM_F2_8_L_IS_USM(263, "Canon RF 24-70mm F2.8L IS USM"),
    CANON_RF_15_35MM_F2_8_L_IS_USM(264, "Canon RF 15-35mm F2.8L IS USM"),
    CANON_RF_24_240MM_F4_6_3_IS_USM(265, "Canon RF 24-240mm F4-6.3 IS USM"),
    CANON_RF_70_200MM_F2_8_L_IS_USM(266, "Canon RF 70-200mm F2.8L IS USM"),
    CANON_RF_85MM_F2_MACRO_IS_STM(267, "Canon RF 85mm F2 MACRO IS STM"),
    CANON_RF_600MM_F11_IS_STM(268, "Canon RF 600mm F11 IS STM"),
    CANON_RF_600MM_F11_IS_STM_RF1_4X(269, "Canon RF 600mm F11 IS STM + RF1.4x"),
    CANON_RF_600MM_F11_IS_STM_RF2X(270, "Canon RF 600mm F11 IS STM + RF2x"),
    CANON_RF_800MM_F11_IS_STM(271, "Canon RF 800mm F11 IS STM"),
    CANON_RF_800MM_F11_IS_STM_RF1_4X(272, "Canon RF 800mm F11 IS STM + RF1.4x"),
    CANON_RF_800MM_F11_IS_STM_RF2X(273, "Canon RF 800mm F11 IS STM + RF2x"),
    CANON_RF_24_105MM_F4_7_1_IS_STM(274, "Canon RF 24-105mm F4-7.1 IS STM"),
    CANON_RF_100_500MM_F4_5_7_1_L_IS_USM(275, "Canon RF 100-500mm F4.5-7.1L IS USM"),
    CANON_RF_100_500MM_F4_5_7_1_L_IS_USM_RF1_4X(276, "Canon RF 100-500mm F4.5-7.1L IS USM + RF1.4x"),
    CANON_RF_100_500MM_F4_5_7_1_L_IS_USM_RF2X(277, "Canon RF 100-500mm F4.5-7.1L IS USM + RF2x"),
    CANON_RF_70_200MM_F4_L_IS_USM(278, "Canon RF 70-200mm F4L IS USM"),
    CANON_RF_100MM_F2_8_L_MACRO_IS_USM(279, "Canon RF 100mm F2.8L MACRO IS USM"),
    CANON_RF_50MM_F1_8_STM(280, "Canon RF 50mm F1.8 STM"),
    CANON_RF_14_35MM_F4_L_IS_USM(281, "Canon RF 14-35mm F4L IS USM"),
    CANON_RF_S_18_45MM_F4_5_6_3_IS_STM(282, "Canon RF-S 18-45mm F4.5-6.3 IS STM"),
    CANON_RF_100_400MM_F5_6_8_IS_USM(283, "Canon RF 100-400mm F5.6-8 IS USM"),
    CANON_RF_100_400MM_F5_6_8_IS_USM_RF1_4X(284, "Canon RF 100-400mm F5.6-8 IS USM + RF1.4x"),
    CANON_RF_100_400MM_F5_6_8_IS_USM_RF2X(285, "Canon RF 100-400mm F5.6-8 IS USM + RF2x"),
    CANON_RF_S_18_150MM_F3_5_6_3_IS_STM(286, "Canon RF-S 18-150mm F3.5-6.3 IS STM"),
    CANON_RF_24MM_F1_8_MACRO_IS_STM(287, "Canon RF 24mm F1.8 MACRO IS STM"),
    CANON_RF_16MM_F2_8_STM(288, "Canon RF 16mm F2.8 STM"),
    CANON_RF_400MM_F2_8_L_IS_USM(289, "Canon RF 400mm F2.8L IS USM"),
    CANON_RF_400MM_F2_8_L_IS_USM_RF1_4X(290, "Canon RF 400mm F2.8L IS USM + RF1.4x"),
    CANON_RF_400MM_F2_8_L_IS_USM_RF2X(291, "Canon RF 400mm F2.8L IS USM + RF2x"),
    CANON_RF_600MM_F4_L_IS_USM(292, "Canon RF 600mm F4L IS USM"),
    CANON_RF_600MM_F4_L_IS_USM_RF1_4X(293, "Canon RF 600mm F4L IS USM + RF1.4x"),
    CANON_RF_600MM_F4_L_IS_USM_RF2X(294, "Canon RF 600mm F4L IS USM + RF2x"),
    CANON_RF_800MM_F5_6_L_IS_USM(295, "Canon RF 800mm F5.6L IS USM"),
    CANON_RF_800MM_F5_6_L_IS_USM_RF1_4X(296, "Canon RF 800mm F5.6L IS USM + RF1.4x"),
    CANON_RF_800MM_F5_6_L_IS_USM_RF2X(297, "Canon RF 800mm F5.6L IS USM + RF2x"),
    CANON_RF_1200MM_F8_L_IS_USM(298, "Canon RF 1200mm F8L IS USM"),
    CANON_RF_1200MM_F8_L_IS_USM_RF1_4X(299, "Canon RF 1200mm F8L IS USM + RF1.4x"),
    CANON_RF_1200MM_F8_L_IS_USM_RF2X(300, "Canon RF 1200mm F8L IS USM + RF2x"),
    CANON_RF_5_2MM_F2_8_L_DUAL_FISHEYE_3_D_VR(301, "Canon RF 5.2mm F2.8L Dual Fisheye 3D VR"),
    CANON_RF_15_30MM_F4_5_6_3_IS_STM(302, "Canon RF 15-30mm F4.5-6.3 IS STM"),
    CANON_RF_135MM_F1_8_L_IS_USM(303, "Canon RF 135mm F1.8 L IS USM"),
    CANON_RF_24_50MM_F4_5_6_3_IS_STM(304, "Canon RF 24-50mm F4.5-6.3 IS STM"),
    CANON_RF_S_55_210MM_F5_7_1_IS_STM(305, "Canon RF-S 55-210mm F5-7.1 IS STM"),
    CANON_RF_100_300MM_F2_8_L_IS_USM(306, "Canon RF 100-300mm F2.8L IS USM"),
    CANON_RF_100_300MM_F2_8_L_IS_USM_RF1_4X(307, "Canon RF 100-300mm F2.8L IS USM + RF1.4x"),
    CANON_RF_100_300MM_F2_8_L_IS_USM_RF2X(308, "Canon RF 100-300mm F2.8L IS USM + RF2x"),
    CANON_RF_200_800MM_F6_3_9_IS_USM(309, "Canon RF 200-800mm F6.3-9 IS USM"),
    CANON_RF_200_800MM_F6_3_9_IS_USM_RF1_4X(310, "Canon RF 200-800mm F6.3-9 IS USM + RF1.4x"),
    CANON_RF_200_800MM_F6_3_9_IS_USM_RF2X(311, "Canon RF 200-800mm F6.3-9 IS USM + RF2x"),
    CANON_RF_10_20MM_F4_L_IS_STM(312, "Canon RF 10-20mm F4 L IS STM"),
    CANON_RF_28MM_F2_8_STM(313, "Canon RF 28mm F2.8 STM"),
    CANON_RF_24_105MM_F2_8_L_IS_USM_Z(314, "Canon RF 24-105mm F2.8 L IS USM Z"),
    CANON_RF_S_10_18MM_F4_5_6_3_IS_STM(315, "Canon RF-S 10-18mm F4.5-6.3 IS STM"),
    CANON_RF_35MM_F1_4_L_VCM(316, "Canon RF 35mm F1.4 L VCM"),
    CANON_RF_S_3_9MM_F3_5_STM_DUAL_FISHEYE(317, "Canon RF-S 3.9mm F3.5 STM DUAL FISHEYE"),
    CANON_RF_28_70MM_F2_8_IS_STM(318, "Canon RF 28-70mm F2.8 IS STM"),
    CANON_RF_70_200MM_F2_8_L_IS_USM_Z(319, "Canon RF 70-200mm F2.8 L IS USM Z"),
    CANON_RF_70_200MM_F2_8_L_IS_USM_Z_RF1_4X(320, "Canon RF 70-200mm F2.8 L IS USM Z + RF1.4x"),
    CANON_RF_70_200MM_F2_8_L_IS_USM_Z_RF2X(321, "Canon RF 70-200mm F2.8 L IS USM Z + RF2x"),
    CANON_RF_16_28MM_F2_8_IS_STM(323, "Canon RF 16-28mm F2.8 IS STM"),
    CANON_RF_S_14_30MM_F4_6_3_IS_STM_PZ(324, "Canon RF-S 14-30mm F4-6.3 IS STM PZ"),
    CANON_RF_50MM_F1_4_L_VCM(325, "Canon RF 50mm F1.4 L VCM"),
    CANON_RF_24MM_F1_4_L_VCM(326, "Canon RF 24mm F1.4 L VCM"),
    CANON_RF_20MM_F1_4_L_VCM(327, "Canon RF 20mm F1.4 L VCM"),
    CANON_RF_85MM_F1_4_L_VCM(328, "Canon RF 85mm F1.4 L VCM"),
    CANON_RF_20_50MM_F4_L_IS_USM_PZ(329, "Canon RF 20-50mm F4 L IS USM PZ"),
    CANON_RF_45MM_F1_2_STM(330, "Canon RF 45mm F1.2 STM"),
    CANON_RF_7_14MM_F2_8_3_5_L_FISHEYE_STM(331, "Canon RF 7-14mm F2.8-3.5 L FISHEYE STM"),
    CANON_RF_14MM_F1_4_L_VCM(332, "Canon RF 14mm F1.4 L VCM");

    public companion object {

        public fun fromValue(value: Int): CanonRFLensType? =
            entries.firstOrNull { it.value == value }
    }
}
