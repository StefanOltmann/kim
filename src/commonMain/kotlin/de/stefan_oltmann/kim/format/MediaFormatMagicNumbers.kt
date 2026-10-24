/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2026 Ramon Bouckaert
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
package de.stefan_oltmann.kim.format

/**
 * See https://en.wikipedia.org/wiki/List_of_file_signatures
 *
 * Note that not all formats can be detected by a magic number.
 * For example HEIC has a broad number of different headers bytes.
 */
@Suppress("MagicNumber")
public object MediaFormatMagicNumbers {

    public val jpeg: List<Byte> = byteListOf(
        0xFF, 0xD8
    )

    public val gif87a: List<Byte> = byteListOf(
        0x47, 0x49, 0x46, 0x38, 0x37, 0x61
    )

    public val gif89a: List<Byte> = byteListOf(
        0x47, 0x49, 0x46, 0x38, 0x39, 0x61
    )

    public val png: List<Byte> = byteListOf(
        0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    )

    public val tiffLittleEndian: List<Byte> = byteListOf(
        0x49, 0x49, 0x2A, 0x00
    )

    /*
     * Note that NEF, ARW & DNG also starts this way.
     * We can't distinguish a normal TIFF and a raw file based on them.
     */
    public val tiffBigEndian: List<Byte> = byteListOf(
        0x4D, 0x4D, 0x00, 0x2A
    )

    public val cr2: List<Byte> = byteListOf(
        0x49, 0x49, 0x2A, 0x00, 0x10, 0x00, 0x00, 0x00, 0x43, 0x52
    )

    public val raf: List<Byte> = "FUJIFILMCCD-RAW ".encodeToByteArray().toList()

    public val rw2: List<Byte> = byteListOf(
        0x49, 0x49, 0x55, 0x00
    )

    public val orf_iiro: List<Byte> = byteListOf(
        0x49, 0x49, 0x52, 0x4F, 0x08, 0x00
    )

    public val orf_mmor: List<Byte> = byteListOf(
        0x4D, 0x4D, 0x4F, 0x52, 0x00, 0x00
    )

    public val orf_iirs: List<Byte> = byteListOf(
        0x49, 0x49, 0x52, 0x53, 0x08, 0x00
    )

    public val webP: List<Byte?> = byteListOf(
        0x52, 0x49, 0x46, 0x46, null, null, null, null, 0x57, 0x45, 0x42, 0x50
    )

    /** 4 bytes + "ftypheic". */
    public val heic: List<Byte?> = ftypBrand("heic")

    /** A HEIC brand. */
    public val mif1: List<Byte?> = ftypBrand("mif1")

    /** A HEIC brand. */
    public val msf1: List<Byte?> = ftypBrand("msf1")

    /** A HEIC brand. */
    public val heix: List<Byte?> = ftypBrand("heix")

    /** A HEIC brand. */
    public val hevc: List<Byte?> = ftypBrand("hevc")

    /** A HEIC brand. */
    public val hevx: List<Byte?> = ftypBrand("hevx")

    /** 4 bytes + "ftypavif". */
    public val avif: List<Byte?> = ftypBrand("avif")

    /** 4 bytes + "ftypavis". */
    public val avis: List<Byte?> = ftypBrand("avis")

    /** 4 bytes + "ftypcrx". */
    public val cr3: List<Byte?> = ftypBrand("crx")

    /** The regular ISOBMFF-based JPEG XL. */
    public val jxl: List<Byte> = byteListOf(
        0x00, 0x00, 0x00, 0x0C, 0x4A, 0x58, 0x4C, 0x20, 0x0D, 0x0A, 0x87, 0x0A
    )

    /** The 'naked' code stream without metadata. */
    public val jxlCodeStream: List<Byte> = byteListOf(
        0xFF, 0x0A
    )

    /** The current MP4 standard. */
    public val mp42: List<Byte?> = ftypBrand("mp42")

    /** The older MP4 standard. */
    public val mp41: List<Byte?> = ftypBrand("mp41")

    /** The generic MP4 format (without closer specification). */
    public val isom: List<Byte?> = ftypBrand("isom")

    /** MP4, similar to ISOM. */
    public val iso2: List<Byte?> = ftypBrand("iso2")

    /** Apple MPEG-4 video (iTunes). */
    public val m4v: List<Byte?> = ftypBrand("m4v")

    /** Adobe Flash video. */
    public val f4v: List<Byte?> = ftypBrand("f4v")

    /** MP4 as written by newer iPhone generations. */
    public val mp71: List<Byte?> = ftypBrand("mp71")

    /** Sony MP4. */
    public val msnv: List<Byte?> = ftypBrand("MSNV")

    /** MPEG-DASH self-initializing segment. */
    public val dash: List<Byte?> = ftypBrand("dash")

    /** MP4 baseline profile. */
    public val iso5: List<Byte?> = ftypBrand("iso5")

    /** MP4 extended profile. */
    public val iso6: List<Byte?> = ftypBrand("iso6")

    /** MP4, often used for H.264 encoded files. */
    public val avc1: List<Byte?> = ftypBrand("avc1")

    /** QuickTime video, the usual ftyp for MOV files. */
    public val qt: List<Byte?> = ftypBrand("qt  ")

    /** The 'naked' code stream without metadata. */
    public val pdf: List<Byte> = byteListOf(
        0x25, 0x50, 0x44, 0x46, 0x2D
    )

    private fun byteListOf(vararg ints: Int?): List<Byte?> =
        ints.map { it?.toByte() }

    private fun byteListOf(vararg ints: Int): List<Byte> =
        ints.map { it.toByte() }

    /**
     * The ISOBMFF signatures: 4 wildcard major-brand size/indicator bytes
     * followed by "ftyp" and the brand. QuickTime carries trailing spaces
     * in its brand, so the builder takes the brand verbatim.
     */
    private fun ftypBrand(brand: String): List<Byte?> =
        byteListOf(null, null, null, null)
            .plus(("ftyp$brand").encodeToByteArray().toList())
}
