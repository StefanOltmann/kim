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
 * Values of the Canon RecordMode tag.
 *
 * See https://exiftool.sourceforge.net/TagNames/Canon.html
 */
@Suppress("MaxLineLength")
public enum class CanonRecordMode(
    public val value: Int,
    public val displayName: String
) {

    JPEG(1, "JPEG"),
    CRW_THM(2, "CRW+THM"),
    AVI_THM(3, "AVI+THM"),
    TIF(4, "TIF"),
    TIF_JPEG(5, "TIF+JPEG"),
    CR2(6, "CR2"),
    CR2_JPEG(7, "CR2+JPEG"),
    MOV(9, "MOV"),
    MP4(10, "MP4"),
    CRM(11, "CRM"),
    CR3(12, "CR3"),
    CR3_JPEG(13, "CR3+JPEG"),
    HIF(14, "HIF"),
    CR3_HIF(15, "CR3+HIF");

    public companion object {

        public fun fromValue(value: Int): CanonRecordMode? =
            entries.firstOrNull { it.value == value }
    }
}
