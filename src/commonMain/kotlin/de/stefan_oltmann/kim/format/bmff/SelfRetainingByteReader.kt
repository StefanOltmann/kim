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
package de.stefan_oltmann.kim.format.bmff

import de.stefan_oltmann.kim.input.ByteReader

/**
 * Marker interface for [ByteReader] implementations that keep their own
 * copy of every consumed byte, so already-read regions stay accessible
 * on forward-only streams.
 *
 * Boxes parsed from such a reader must not additionally buffer large
 * payloads themselves, or the data ends up in memory twice.
 */
internal interface SelfRetainingByteReader
