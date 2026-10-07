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
package de.stefan_oltmann.kim.format.icc

/**
 * A single entry of the ICC tag table.
 *
 * The [signature] is the 4-character type signature the entry's value
 * starts with (like "desc" or "XYZ "), [name] the display name from the
 * ICC tag registry or NULL for signatures without a known name, and
 * [value] the decoded text or number list - NULL for value types whose
 * binary layout the parser does not decode, like curve parameters.
 */
public data class IccEntry(
    val signature: String,
    val name: String?,
    val value: String?
)
