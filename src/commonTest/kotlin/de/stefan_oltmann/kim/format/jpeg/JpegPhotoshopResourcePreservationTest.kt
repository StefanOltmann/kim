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

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Photoshop resources the parser deliberately does not interpret -
 * like the ICC-untagged flag (0x043C) that tells Photoshop how to
 * color-manage the file - are still file content. They must survive
 * an IPTC rewrite byte-exact instead of being silently dropped.
 */
class JpegPhotoshopResourcePreservationTest {

    @Test
    fun testNonInterpretedPhotoshopResourcesSurviveIptcRewrite() {

        val original = KimTestData.getBytesOf(37)

        /* media_37 carries the 0x043C resource in its IPTC APP13 stream. */
        val before = assertNotNull(assertNotNull(Kim.readMetadata(original)).iptc)

        val flaggedBlock = assertNotNull(
            before.rawBlocks.singleOrNull { block -> block.blockType == 0x043C }
        )

        val updated = Kim.update(
            bytes = original,
            updates = setOf(MetadataUpdate.Keywords(setOf("rewritten")))
        )

        val after = assertNotNull(assertNotNull(Kim.readMetadata(updated)).iptc)

        val preservedBlock = assertNotNull(
            after.rawBlocks.singleOrNull { block -> block.blockType == 0x043C }
        )

        /*
         * Byte lists instead of byte arrays: array equality is
         * identity, and the rewrite re-parses a fresh buffer.
         */
        assertEquals(flaggedBlock.blockData.toList(), preservedBlock.blockData.toList())
    }
}
