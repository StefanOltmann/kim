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

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.Md5
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.model.MetadataUpdate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Datasets outside IPTC application record 2 - such as the envelope
 * record version ExifTool writes into every IPTC block - are part of
 * the file's metadata. An IPTC rewrite must carry them through
 * byte-exact instead of silently dropping them.
 */
class JpegIptcForeignDatasetsTest {

    @Test
    fun testForeignIimDatasetsSurviveIptcRewrite() {

        val original = Resource(
            "de/stefan_oltmann/kim/testdata/jpeg_with_foreign_iptc_datasets.jpg"
        ).readBytes()

        val before = assertNotNull(assertNotNull(Kim.readMetadata(original)).iptc)

        /* The ExifTool-written fixture carries the envelope record
           version dataset (1:0), which must survive as foreign data. */
        assertTrue(before.foreignDatasets.isNotEmpty())

        val updated = Kim.update(
            bytes = original,
            updates = setOf(MetadataUpdate.Keywords(setOf("rewritten")))
        )

        val after = assertNotNull(assertNotNull(Kim.readMetadata(updated)).iptc)

        /*
         * Byte lists instead of byte arrays: array equality is
         * identity, and the rewrite re-parses a fresh buffer.
         */
        assertEquals(
            before.foreignDatasets.map { it.toList() },
            after.foreignDatasets.map { it.toList() }
        )
    }

    @Test
    fun testIptcDigestResourceCoversForeignDatasets() {

        val original = Resource(
            "de/stefan_oltmann/kim/testdata/jpeg_with_foreign_iptc_datasets.jpg"
        ).readBytes()

        val updated = Kim.update(
            bytes = original,
            updates = setOf(MetadataUpdate.Keywords(setOf("rewritten")))
        )

        val after = assertNotNull(assertNotNull(Kim.readMetadata(updated)).iptc)

        val digestResource = assertNotNull(
            after.nonIptcBlocks.singleOrNull { block ->
                block.blockType == IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DIGEST
            }
        )

        /*
         * The digest marks the IPTC data as in sync - it must hash
         * exactly the block bytes that were written, foreign datasets
         * included, or digest-aware tools report a mismatch.
         */
        assertEquals(
            Md5.digest(
                IptcWriter.writeIptcBlockData(after.records, after.foreignDatasets)
            ).toList(),
            digestResource.blockData.toList()
        )
    }
}
