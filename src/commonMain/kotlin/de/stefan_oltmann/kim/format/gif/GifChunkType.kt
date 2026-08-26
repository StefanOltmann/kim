/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ramon Bouckaert
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

package de.stefan_oltmann.kim.format.gif

/**
 * The type of a chunk in a GIF file.
 */
public enum class GifChunkType {
    HEADER,
    LOGICAL_SCREEN_DESCRIPTOR,
    GLOBAL_COLOR_TABLE,
    LOCAL_COLOR_TABLE,
    IMAGE_DESCRIPTOR,
    IMAGE_DATA,
    GRAPHICS_CONTROL_EXTENSION,
    PLAIN_TEXT_EXTENSION,
    APPLICATION_EXTENSION,
    COMMENT_EXTENSION,

    /**
     * An extension block with a label this parser does not know. It is
     * kept as an opaque block, so rewrites never destroy its content.
     */
    UNKNOWN_EXTENSION,
    TERMINATOR
}
