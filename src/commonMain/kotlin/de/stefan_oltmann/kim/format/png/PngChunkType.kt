/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package de.stefan_oltmann.kim.format.png

import de.stefan_oltmann.kim.common.toFourCCTypeString
import de.stefan_oltmann.kim.format.png.PngConstants.TYPE_LENGTH

/**
 * Type of a PNG chunk, identified by its four byte code. The
 * documentation of each chunk type links the section that defines it.
 *
 * See https://www.w3.org/TR/PNG/
 */
public data class PngChunkType(
    val bytes: ByteArray,
    val name: String,
    val intValue: Int
) {

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (other !is PngChunkType)
            return false

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int =
        bytes.contentHashCode()

    override fun toString(): String =
        name

    public companion object {

        /**
         * Image header: pixel dimensions, bit depth, color type and the
         * interlace method. Every PNG datastream starts with this chunk.
         *
         * Exactly once, always the first chunk.
         *
         * See https://www.w3.org/TR/PNG/#11IHDR
         */
        public val IHDR: PngChunkType = of("IHDR".encodeToByteArray())

        /**
         * Palette: 1 to 256 RGB entries for indexed-color images.
         * Required for color type 3, optional for color types 2 and 6,
         * and it must not appear for color types 0 and 4.
         *
         * At most once, before the first IDAT chunk.
         *
         * See https://www.w3.org/TR/PNG/#11PLTE
         */
        private val PLTE: PngChunkType = of("PLTE".encodeToByteArray())

        /**
         * Image data: the zlib-compressed pixel stream. Multiple IDAT
         * chunks form one logical stream and must be consecutive.
         *
         * See https://www.w3.org/TR/PNG/#11IDAT
         */
        public val IDAT: PngChunkType = of("IDAT".encodeToByteArray())

        /**
         * Image trailer: marks the end of the datastream and carries no
         * data. Exactly once, always the last chunk.
         *
         * See https://www.w3.org/TR/PNG/#11IEND
         */
        public val IEND: PngChunkType = of("IEND".encodeToByteArray())

        /**
         * Primary chromaticities and white point of the image's RGB
         * space, for viewers that want to be color-exact.
         *
         * At most once, before PLTE and IDAT.
         *
         * See https://www.w3.org/TR/PNG/#11cHRM
         */
        private val CHRM: PngChunkType = of("cHRM".encodeToByteArray())

        /**
         * Coding-independent code points (ITU-T H.273) identifying the
         * video color space of the image. Introduced with the PNG third
         * edition.
         *
         * At most once, before PLTE and IDAT.
         *
         * See https://www.w3.org/TR/PNG/#cICP-chunk
         */
        private val CICP: PngChunkType = of("cICP".encodeToByteArray())

        /**
         * Image gamma, so viewers can reproduce the intended color
         * intensity.
         *
         * At most once, before PLTE and IDAT.
         *
         * See https://www.w3.org/TR/PNG/#11gAMA
         */
        private val GAMA: PngChunkType = of("gAMA".encodeToByteArray())

        /**
         * Embedded ICC color profile: a Latin-1 profile name, a NUL
         * terminator, the compression method byte and the zlib-compressed
         * profile behind it. Kim decompresses the profile and parses it
         * as the ICC profile of the metadata.
         *
         * At most once, before PLTE and IDAT. When an sRGB chunk is
         * present, the specification says an iCCP chunk should not be.
         *
         * See https://www.w3.org/TR/PNG/#11iCCP
         */
        public val ICCP: PngChunkType = of("iCCP".encodeToByteArray())

        /**
         * Mastering display color volume: the display characteristics
         * the image was mastered for. Introduced with the PNG third
         * edition.
         *
         * At most once, before PLTE and IDAT.
         *
         * See https://www.w3.org/TR/PNG/#mDCV-chunk
         */
        private val MDCV: PngChunkType = of("mDCV".encodeToByteArray())

        /**
         * Content light level: the maximum and average light level of
         * the content. Introduced with the PNG third edition.
         *
         * At most once, before PLTE and IDAT.
         *
         * See https://www.w3.org/TR/PNG/#cLLI-chunk
         */
        private val CLLI: PngChunkType = of("cLLI".encodeToByteArray())

        /**
         * Significant bits: how many bits of each sample are meaningful
         * when the data comes from a source with lower precision.
         *
         * At most once, before PLTE and IDAT.
         *
         * See https://www.w3.org/TR/PNG/#11sBIT
         */
        private val SBIT: PngChunkType = of("sBIT".encodeToByteArray())

        /**
         * Declares the image as sRGB-encoded, so viewers can skip color
         * management. When it is present, the specification says an
         * iCCP chunk should not be.
         *
         * At most once, before PLTE and IDAT.
         *
         * See https://www.w3.org/TR/PNG/#srgb-standard-colour-space
         */
        private val SRGB: PngChunkType = of("sRGB".encodeToByteArray())

        /**
         * Background color, whose interpretation depends on the color
         * type. Viewers use it when no better background is known.
         *
         * At most once, after PLTE and before IDAT.
         *
         * See https://www.w3.org/TR/PNG/#11bKGD
         */
        private val BKGD: PngChunkType = of("bKGD".encodeToByteArray())

        /**
         * Histogram of how often each palette entry is used, so viewers
         * can pick useful substitute colors on a palette shortage.
         * Requires a PLTE chunk.
         *
         * At most once, after PLTE and before IDAT.
         *
         * See https://www.w3.org/TR/PNG/#11hIST
         */
        private val HIST: PngChunkType = of("hIST".encodeToByteArray())

        /**
         * Transparency: palette alphas for indexed images or a single
         * transparent color otherwise. Must not appear for color types
         * 4 and 6, which carry a full alpha channel.
         *
         * At most once, after PLTE and before IDAT.
         *
         * See https://www.w3.org/TR/PNG/#11tRNS
         */
        private val TRNS: PngChunkType = of("tRNS".encodeToByteArray())

        /**
         * Exif profile: a raw TIFF structure holding camera metadata
         * like orientation, exposure or GPS. Kim parses it into the
         * EXIF of the metadata.
         *
         * At most once, before IDAT. Introduced with the PNG third
         * edition.
         *
         * See https://www.w3.org/TR/PNG/#eXIf
         */
        public val EXIF: PngChunkType = of("eXIf".encodeToByteArray())

        /**
         * Compressed Exif profile: like eXIf, but the TIFF payload is
         * zlib-compressed behind a NUL byte and the uncompressed byte
         * count. The chunk type is unofficial - the PNG specification
         * does not know it. Kim reads it and writes the standard eXIf
         * chunk instead.
         *
         * At most once. No specification section exists for it.
         */
        public val ZXIF: PngChunkType = of("zxIf".encodeToByteArray())

        /**
         * Physical pixel dimensions, intended aspect ratio or print
         * density.
         *
         * At most once, before IDAT.
         *
         * See https://www.w3.org/TR/PNG/#11pHYs
         */
        private val PHYS: PngChunkType = of("pHYs".encodeToByteArray())

        /**
         * Last modification time of the image itself, not of the file.
         *
         * At most once, anywhere between IHDR and IEND.
         *
         * See https://www.w3.org/TR/PNG/#11tIME
         */
        public val TIME: PngChunkType = of("tIME".encodeToByteArray())

        /**
         * Textual data: a Latin-1 keyword, a NUL terminator and Latin-1
         * text. Multiple chunks are allowed and common - comments,
         * descriptions and legacy XMP or IPTC stores all use it.
         *
         * See https://www.w3.org/TR/PNG/#11tEXt
         */
        public val TEXT: PngChunkType = of("tEXt".encodeToByteArray())

        /**
         * Compressed textual data: a Latin-1 keyword, a NUL terminator,
         * the compression method byte and the zlib-compressed Latin-1
         * text. Multiple chunks are allowed.
         *
         * See https://www.w3.org/TR/PNG/#11zTXt
         */
        public val ZTXT: PngChunkType = of("zTXt".encodeToByteArray())

        /**
         * International textual data: like tEXt, but with a compression
         * flag, a language tag, a UTF-8 translated keyword and UTF-8
         * text, so the content is not limited to Latin-1. Kim reads XMP
         * from the chunk with the XML:com.adobe.xmp keyword. Multiple
         * chunks are allowed.
         *
         * See https://www.w3.org/TR/PNG/#11iTXt
         */
        public val ITXT: PngChunkType = of("iTXt".encodeToByteArray())

        /**
         * The chunk types the PNG specification allows at most once per
         * datastream: the "Multiple: No" rows of its chunk ordering
         * rules. A file with a second chunk of one of these types is
         * corrupt and fails the read.
         *
         * See https://www.w3.org/TR/PNG/#5ChunkOrdering
         *
         * IEND is not listed because the chunk walk terminates at the
         * first IEND, so a second one is never seen. The unofficial zxIf
         * is the compressed eXIf variant and follows the eXIf rule.
         *
         * The types Kim does not interpret are private to this file:
         * only the duplicate check needs their identity.
         */
        internal val SINGLE_APPEARANCE_CHUNK_TYPES: Set<PngChunkType> = setOf(
            IHDR,
            PLTE,
            CHRM,
            CICP,
            GAMA,
            ICCP,
            MDCV,
            CLLI,
            SBIT,
            SRGB,
            BKGD,
            HIST,
            TRNS,
            EXIF,
            ZXIF,
            PHYS,
            TIME
        )

        @Suppress("MagicNumber")
        public fun of(typeBytes: ByteArray): PngChunkType {

            require(typeBytes.size == TYPE_LENGTH) {
                "ChunkType must be always 4 bytes, but got ${typeBytes.size} bytes!"
            }

            @Suppress("UnnecessaryParentheses")
            val intValue =
                (typeBytes[0].toInt() shl 24) or
                    (typeBytes[1].toInt() shl 16) or
                    (typeBytes[2].toInt() shl 8) or
                    (typeBytes[3].toInt() shl 0)

            return PngChunkType(
                bytes = typeBytes,
                name = intValue.toFourCCTypeString(),
                intValue = intValue
            )
        }
    }
}
