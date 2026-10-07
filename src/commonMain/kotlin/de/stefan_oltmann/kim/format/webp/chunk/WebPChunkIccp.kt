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
package de.stefan_oltmann.kim.format.webp.chunk

import de.stefan_oltmann.kim.format.webp.WebPChunkType

/**
 * The ICCP chunk of a WebP file: the ICC color profile, stored
 * uncompressed per the WebP container specification.
 */
public class WebPChunkIccp(
    bytes: ByteArray
) : WebPChunk(WebPChunkType.ICCP, bytes)
