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
import de.stefan_oltmann.kim.common.Md5
import de.stefan_oltmann.kim.common.toHex
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcBlock
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcMetadata
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * When an update rewrites the IPTC block, the XMP must carry the MD5
 * digest of the new IPTC data as `xmpNote:IPTCDigest`, like ExifTool
 * writes it. Tools use the digest to detect XMP that is out of sync
 * with the IPTC IIM data.
 */
class JpegIptcDigestTest {

    @Test
    fun testUpdateWritesCurrentIptcDigest() {

        val updatedBytes = Kim.update(
            bytes = KimTestData.getBytesOf(index = 5),
            updates = setOf(MetadataUpdate.Keywords(setOf("alpha", "beta")))
        )

        val metadata = assertNotNull(Kim.readMetadata(updatedBytes))

        val xmp = assertNotNull(metadata.xmp, "The updated XMP is missing.")

        val digest = Regex("xmpNote:IPTCDigest=\"([0-9a-f]{32})\"").find(xmp)?.groupValues?.lastOrNull()

        assertNotNull(digest, "The XMP carries no xmpNote:IPTCDigest: $xmp")

        /*
         * The digest must match the IPTC IIM data as it exists in the
         * rewritten file.
         */
        val iptc = assertNotNull(metadata.iptc)

        val roundTripDigest = Md5.digest(IptcWriter.writeIptcBlockData(iptc.records)).toHex()

        assertEquals(roundTripDigest, digest)

        /*
         * The Photoshop IPTCDigest resource (0x0425) must carry the same
         * digest bytes, or tools flag the file as out of sync.
         */
        val digestResource = iptc.nonIptcBlocks.firstOrNull { block ->
            block.blockType == IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DIGEST
        }

        assertNotNull(digestResource, "The Photoshop IPTCDigest resource is missing.")

        assertContentEquals(
            expected = Md5.digest(IptcWriter.writeIptcBlockData(iptc.records)),
            actual = digestResource.blockData,
            message = "The Photoshop IPTCDigest resource carries stale digest bytes."
        )
    }

    /**
     * A digest resource whose data has a foreign size is still the file's
     * declared digest slot: a legacy tool may have written it padded. The
     * update must replace its data with the current digest regardless of
     * the old size, or the stale MD5 survives the rewrite and digest-aware
     * tools permanently flag the file as out of sync.
     */
    @Test
    fun testUpdateReplacesForeignSizeDigestResource() {

        val originalBytes = KimTestData.getBytesOf(index = 5)

        /* Build a file whose digest resource carries padded 20-byte data. */
        val originalIptc = assertNotNull(Kim.readMetadata(originalBytes)).iptc

        val paddedBlocks = assertNotNull(originalIptc).nonIptcBlocks.map { block ->
            if (block.blockType == IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DIGEST)
                IptcBlock(block.blockType, block.blockNameBytes, block.blockData + ByteArray(4))
            else
                block
        }

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.writeIPTC(
            byteReader = ByteArrayByteReader(originalBytes),
            byteWriter = byteWriter,
            metadata = IptcMetadata(
                records = originalIptc.records,
                rawBlocks = paddedBlocks
            )
        )

        val fixtureBytes = byteWriter.toByteArray()

        /* Sanity: the fixture really carries the padded digest slot. */
        val fixtureIptc = assertNotNull(Kim.readMetadata(fixtureBytes)).iptc

        val paddedDigestBlock = assertNotNull(fixtureIptc).nonIptcBlocks
            .first { it.blockType == IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DIGEST }

        assertEquals(20, paddedDigestBlock.blockData.size)

        val updatedBytes = Kim.update(
            bytes = fixtureBytes,
            updates = setOf(MetadataUpdate.Keywords(setOf("fresh")))
        )

        val updatedIptc = assertNotNull(Kim.readMetadata(updatedBytes)).iptc

        val digestResource = assertNotNull(updatedIptc).nonIptcBlocks.firstOrNull { block ->
            block.blockType == IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DIGEST
        }

        assertContentEquals(
            Md5.digest(IptcWriter.writeIptcBlockData(updatedIptc.records)),
            assertNotNull(digestResource, "The Photoshop IPTCDigest resource is missing.").blockData
        )
    }
}
