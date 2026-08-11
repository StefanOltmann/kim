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
package de.stefan_oltmann.kim.format.tiff.taginfo

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.fieldtype.FieldTypeLong

/**
 * Tag info for a long value.
 */
public class TagInfoLong : TagInfo {

    public constructor(
        tag: Int,
        name: String,
        directoryType: TiffDirectoryType?
    ) : super(tag, name, FieldTypeLong, 1, directoryType)

    public constructor(
        tag: Int,
        name: String,
        directoryType: TiffDirectoryType?,
        isOffset: Boolean
    ) : super(tag, name, FieldTypeLong, 1, directoryType, isOffset)
}
