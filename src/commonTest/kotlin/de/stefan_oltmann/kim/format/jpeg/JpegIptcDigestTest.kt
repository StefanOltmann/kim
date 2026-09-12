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
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcConstants
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
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
         * digest, or tools flag the file as out of sync.
         */
        val digestResource = iptc.nonIptcBlocks.firstOrNull { block ->
            block.blockType == IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DIGEST
        }

        assertNotNull(digestResource, "The Photoshop IPTCDigest resource is missing.")

        assertEquals(
            digest,
            Md5.digest(IptcWriter.writeIptcBlockData(iptc.records)).toHex()
        )
    }
}
