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
package de.stefan_oltmann.kim.format.jpeg

import kotlin.test.Test
import kotlin.test.assertEquals

class JpegConstantsTest {

    @Test
    fun testMarkerDescription() {

        val descriptions = mapOf(
            JpegConstants.SOI_MARKER to "SOI (Start of Image)",
            JpegConstants.DHT_MARKER to "DHT (Define Huffman Table)",
            JpegConstants.DQT_MARKER to "DQT (Define Quantization Table)",
            JpegConstants.SOS_MARKER to "SOS (Start of Scan)",
            JpegConstants.EOI_MARKER to "EOI (End of Image)",
            JpegConstants.JPEG_APP0_MARKER to "APP0 (Application Segment, JFIF)",
            JpegConstants.JPEG_APP1_MARKER to "APP1 (Application Segment, EXIF & XMP)",
            JpegConstants.JPEG_APP2_MARKER to "APP2 (Application Segment, ICC & FlashPix)",
            JpegConstants.JPEG_APP3_MARKER to "APP3 (Application Segment)",
            JpegConstants.JPEG_APP4_MARKER to "APP4 (Application Segment)",
            JpegConstants.JPEG_APP5_MARKER to "APP5 (Application Segment)",
            JpegConstants.JPEG_APP6_MARKER to "APP6 (Application Segment)",
            JpegConstants.JPEG_APP7_MARKER to "APP7 (Application Segment)",
            JpegConstants.JPEG_APP8_MARKER to "APP8 (Application Segment)",
            JpegConstants.JPEG_APP9_MARKER to "APP9 (Application Segment)",
            JpegConstants.JPEG_APP10_MARKER to "APP10 (Application Segment)",
            JpegConstants.JPEG_APP11_MARKER to "APP11 (Application Segment)",
            JpegConstants.JPEG_APP12_MARKER to "APP12 (Application Segment, Ducky)",
            JpegConstants.JPEG_APP13_MARKER to "APP13 (Application Segment, IPTC)",
            JpegConstants.JPEG_APP14_MARKER to "APP14 (Application Segment)",
            JpegConstants.JPEG_APP15_MARKER to "APP15 (Application Segment)",
            JpegConstants.SOF0_MARKER to "SOF0 (Start of Frame, Baseline DCT)",
            JpegConstants.SOF1_MARKER to "SOF1 (Start of Frame, Extended Sequential DCT)",
            JpegConstants.SOF2_MARKER to "SOF2 (Start of Frame, Progressive DCT)",
            JpegConstants.SOF3_MARKER to "SOF3 (Start of Frame, Lossless (sequential))",
            JpegConstants.SOF5_MARKER to "SOF5 (Start of Frame, Differential sequential DCT)",
            JpegConstants.SOF6_MARKER to "SOF6 (Start of Frame, Differential progressive DCT)",
            JpegConstants.SOF7_MARKER to "SOF7 (Start of Frame, Differential lossless (sequential))",
            JpegConstants.SOF9_MARKER to
                "SOF9 (Start of Frame, Extended sequential DCT, Arithmetic coding)",
            JpegConstants.SOF10_MARKER to
                "SOF10 (Start of Frame, Progressive DCT, Arithmetic coding)",
            JpegConstants.SOF11_MARKER to
                "SOF11 (Start of Frame, Lossless (sequential), Arithmetic coding)",
            JpegConstants.SOF13_MARKER to
                "SOF13 (Start of Frame, Differential sequential DCT, Arithmetic coding)",
            JpegConstants.SOF14_MARKER to
                "SOF14 (Start of Frame, Differential progressive DCT, Arithmetic coding)",
            JpegConstants.SOF15_MARKER to
                "SOF15 (Start of Frame, Differential lossless (sequential), Arithmetic coding)",
            JpegConstants.COM_MARKER_1 to "COM (Comment)",
            JpegConstants.DRI_MARKER to "DRI (Define Restart Interval)",
            JpegConstants.RST0_MARKER to "RST0 (Restart Marker)",
            JpegConstants.RST1_MARKER to "RST1 (Restart Marker)",
            JpegConstants.RST2_MARKER to "RST2 (Restart Marker)",
            JpegConstants.RST3_MARKER to "RST3 (Restart Marker)",
            JpegConstants.RST4_MARKER to "RST4 (Restart Marker)",
            JpegConstants.RST5_MARKER to "RST5 (Restart Marker)",
            JpegConstants.RST6_MARKER to "RST6 (Restart Marker)",
            JpegConstants.RST7_MARKER to "RST7 (Restart Marker)",
            JpegConstants.DNL_MARKER to "DNL (Define Number of Lines)",
            JpegConstants.JPG_EXT_MARKER to "JPG (JPEG Extensions)",
            JpegConstants.DAC_MARKER to "DAC (Define Arithmetic Coding)",
            JpegConstants.DHP_MARKER to "DHP (Define Hierarchical Progression)",
            JpegConstants.EXP_MARKER to "EXP (Expand Reference Component)"
        )

        for ((marker, description) in descriptions)
            assertEquals(description, JpegConstants.markerDescription(marker), "Marker $marker")
    }

    @Test
    fun testMarkerDescriptionUnknownMarker() {

        /* Unknown markers are shown as hex. */
        assertEquals(
            expected = "ABCD",
            actual = JpegConstants.markerDescription(0xABCD)
        )
    }
}
