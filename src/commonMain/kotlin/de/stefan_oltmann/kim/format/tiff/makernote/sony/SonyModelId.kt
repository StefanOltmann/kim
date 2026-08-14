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
package de.stefan_oltmann.kim.format.tiff.makernote.sony

/**
 * Values of the Sony ModelID tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Sony.html#ModelID
 */
@Suppress("MaxLineLength")
public enum class SonyModelId(
    public val value: Int,
    public val displayName: String
) {

    DSC_R1(0x2, "DSC-R1"),
    DSLR_A100(0x100, "DSLR-A100"),
    DSLR_A900(0x101, "DSLR-A900"),
    DSLR_A700(0x102, "DSLR-A700"),
    DSLR_A200(0x103, "DSLR-A200"),
    DSLR_A350(0x104, "DSLR-A350"),
    DSLR_A300(0x105, "DSLR-A300"),
    DSLR_A900_APS_C_MODE(0x106, "DSLR-A900 (APS-C mode)"),
    DSLR_A380_A390(0x107, "DSLR-A380/A390"),
    DSLR_A330(0x108, "DSLR-A330"),
    DSLR_A230(0x109, "DSLR-A230"),
    DSLR_A290(0x10a, "DSLR-A290"),
    DSLR_A850(0x10d, "DSLR-A850"),
    DSLR_A850_APS_C_MODE(0x10e, "DSLR-A850 (APS-C mode)"),
    DSLR_A550(0x111, "DSLR-A550"),
    DSLR_A500(0x112, "DSLR-A500"),
    DSLR_A450(0x113, "DSLR-A450"),
    NEX_5(0x116, "NEX-5"),
    NEX_3(0x117, "NEX-3"),
    SLT_A33(0x118, "SLT-A33"),
    SLT_A55_SLT_A55V(0x119, "SLT-A55 / SLT-A55V"),
    DSLR_A560(0x11a, "DSLR-A560"),
    DSLR_A580(0x11b, "DSLR-A580"),
    NEX_C3(0x11c, "NEX-C3"),
    SLT_A35(0x11d, "SLT-A35"),
    SLT_A65_SLT_A65V(0x11e, "SLT-A65 / SLT-A65V"),
    SLT_A77_SLT_A77V(0x11f, "SLT-A77 / SLT-A77V"),
    NEX_5N(0x120, "NEX-5N"),
    NEX_7(0x121, "NEX-7"),
    NEX_VG20E(0x122, "NEX-VG20E"),
    SLT_A37(0x123, "SLT-A37"),
    SLT_A57(0x124, "SLT-A57"),
    NEX_F3(0x125, "NEX-F3"),
    SLT_A99_SLT_A99V(0x126, "SLT-A99 / SLT-A99V"),
    NEX_6(0x127, "NEX-6"),
    NEX_5R(0x128, "NEX-5R"),
    DSC_RX100(0x129, "DSC-RX100"),
    DSC_RX1(0x12a, "DSC-RX1"),
    NEX_VG900(0x12b, "NEX-VG900"),
    NEX_VG30E(0x12c, "NEX-VG30E"),
    ILCE_3000_ILCE_3500(0x12e, "ILCE-3000 / ILCE-3500"),
    SLT_A58(0x12f, "SLT-A58"),
    NEX_3N(0x131, "NEX-3N"),
    ILCE_7(0x132, "ILCE-7"),
    NEX_5T(0x133, "NEX-5T"),
    DSC_RX100M2(0x134, "DSC-RX100M2"),
    DSC_RX10(0x135, "DSC-RX10"),
    DSC_RX1R(0x136, "DSC-RX1R"),
    ILCE_7R(0x137, "ILCE-7R"),
    ILCE_6000(0x138, "ILCE-6000"),
    ILCE_5000(0x139, "ILCE-5000"),
    DSC_RX100M3(0x13d, "DSC-RX100M3"),
    ILCE_7S(0x13e, "ILCE-7S"),
    ILCA_77M2(0x13f, "ILCA-77M2"),
    ILCE_5100(0x153, "ILCE-5100"),
    ILCE_7M2(0x154, "ILCE-7M2"),
    DSC_RX100M4(0x155, "DSC-RX100M4"),
    DSC_RX10M2(0x156, "DSC-RX10M2"),
    DSC_RX1RM2(0x158, "DSC-RX1RM2"),
    ILCE_QX1(0x15a, "ILCE-QX1"),
    ILCE_7RM2(0x15b, "ILCE-7RM2"),
    ILCE_7SM2(0x15e, "ILCE-7SM2"),
    ILCA_68(0x161, "ILCA-68"),
    ILCA_99M2(0x162, "ILCA-99M2"),
    DSC_RX10M3(0x163, "DSC-RX10M3"),
    DSC_RX100M5(0x164, "DSC-RX100M5"),
    ILCE_6300(0x165, "ILCE-6300"),
    ILCE_9(0x166, "ILCE-9"),
    ILCE_6500(0x168, "ILCE-6500"),
    ILCE_7RM3(0x16a, "ILCE-7RM3"),
    ILCE_7M3(0x16b, "ILCE-7M3"),
    DSC_RX0(0x16c, "DSC-RX0"),
    DSC_RX10M4(0x16d, "DSC-RX10M4"),
    DSC_RX100M6(0x16e, "DSC-RX100M6"),
    DSC_HX99(0x16f, "DSC-HX99"),
    DSC_RX100M5A(0x171, "DSC-RX100M5A"),
    ILCE_6400(0x173, "ILCE-6400"),
    DSC_RX0M2(0x174, "DSC-RX0M2"),
    DSC_HX95(0x175, "DSC-HX95"),
    DSC_RX100M7(0x176, "DSC-RX100M7"),
    ILCE_7RM4(0x177, "ILCE-7RM4"),
    ILCE_9M2(0x178, "ILCE-9M2"),
    ILCE_6600(0x17a, "ILCE-6600"),
    ILCE_6100(0x17b, "ILCE-6100"),
    ZV_1(0x17c, "ZV-1"),
    ILCE_7C(0x17d, "ILCE-7C"),
    ZV_E10(0x17e, "ZV-E10"),
    ILCE_7SM3(0x17f, "ILCE-7SM3"),
    ILCE_1(0x180, "ILCE-1"),
    ILME_FX3(0x181, "ILME-FX3"),
    ILCE_7RM3A(0x182, "ILCE-7RM3A"),
    ILCE_7RM4A(0x183, "ILCE-7RM4A"),
    ILCE_7M4(0x184, "ILCE-7M4"),
    ZV_1F(0x185, "ZV-1F"),
    ILCE_7RM5(0x186, "ILCE-7RM5"),
    ILME_FX30(0x187, "ILME-FX30"),
    ILCE_9M3(0x188, "ILCE-9M3"),
    ZV_E1(0x189, "ZV-E1"),
    ILCE_6700(0x18a, "ILCE-6700"),
    ZV_1M2(0x18b, "ZV-1M2"),
    ILCE_7CR(0x18c, "ILCE-7CR"),
    ILCE_7CM2(0x18d, "ILCE-7CM2"),
    ILX_LR1(0x18e, "ILX-LR1"),
    ZV_E10M2(0x18f, "ZV-E10M2"),
    ILCE_1M2(0x190, "ILCE-1M2"),
    DSC_RX1RM3(0x191, "DSC-RX1RM3"),
    ILCE_6400A(0x192, "ILCE-6400A"),
    ILCE_6100A(0x193, "ILCE-6100A"),
    DSC_RX100M7A(0x194, "DSC-RX100M7A"),
    ILME_FX2(0x196, "ILME-FX2"),
    ILCE_7M5(0x197, "ILCE-7M5"),
    ZV_1A(0x198, "ZV-1A"),
    ILCE_7RM6(0x19a, "ILCE-7RM6");

    public companion object {

        public fun fromValue(value: Int): SonyModelId? =
            entries.firstOrNull { it.value == value }
    }
}
