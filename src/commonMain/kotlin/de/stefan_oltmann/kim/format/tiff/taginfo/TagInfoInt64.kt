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
package de.stefan_oltmann.kim.format.tiff.taginfo

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeInt64

/**
 * Tag info for a 64-bit integer value, used by Apple MakerNotes.
 */
public class TagInfoInt64 : TagInfo {

    public constructor(
        tag: Int,
        name: String,
        length: Int = LENGTH_UNKNOWN,
        directoryType: TiffDirectoryType?
    ) : super(tag, name, FieldTypeInt64, length, directoryType)
}
