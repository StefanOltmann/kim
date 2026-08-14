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
package de.stefan_oltmann.kim.format.tiff.makernote.apple

import de.stefan_oltmann.kim.format.tiff.constant.TiffDirectoryType
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfo
import de.stefan_oltmann.kim.format.tiff.taginfo.TagInfoInt64

/**
 * Tags of the RunTime property list of the Apple MakerNote.
 */
public object AppleRunTimeTag {

    public val RUN_TIME_FLAGS: TagInfoInt64 = TagInfoInt64(
        0x1, "RunTimeFlags",
        1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE_RUN_TIME
    )

    public val RUN_TIME_VALUE: TagInfoInt64 = TagInfoInt64(
        0x2, "RunTimeValue",
        1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE_RUN_TIME
    )

    public val RUN_TIME_SCALE: TagInfoInt64 = TagInfoInt64(
        0x3, "RunTimeScale",
        1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE_RUN_TIME
    )

    public val RUN_TIME_EPOCH: TagInfoInt64 = TagInfoInt64(
        0x4, "RunTimeEpoch",
        1,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_APPLE_RUN_TIME
    )

    public val ALL: List<TagInfo> = listOf(
        RUN_TIME_FLAGS, RUN_TIME_VALUE, RUN_TIME_SCALE, RUN_TIME_EPOCH
    )
}
