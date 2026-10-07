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
import de.stefan_oltmann.kim.format.jpeg.iptc.IptcWriter
import de.stefan_oltmann.kim.format.jpeg.xmp.JpegXmpParser
import de.stefan_oltmann.xmp.XMPMetaFactory
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * MWG-style writers may declare the IPTC digest only in the XMP
 * (xmpNote:IPTCDigest) without carrying the Photoshop 0x0425 resource.
 * After an IPTC-rewriting update, the XMP digest must track the new
 * records - a stale digest makes digest-aware tools report the stores
 * as out of sync although they agree.
 */
class JpegXmpOnlyDigestTest {

    @Test
    fun testXmpOnlyDigestIsRefreshedOnIptcRewrite() {

        /* Build a JPEG whose XMP declares a digest without any 0x0425
           resource: media_1's IPTC stays, the Photoshop blocks do not
           carry a digest resource. */
        val original = KimTestData.getBytesOf(1)

        val digestXmp = """
            <?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <rdf:Description rdf:about=""
                    xmlns:xmpNote="http://ns.adobe.com/xmp/note/"
                  xmpNote:IPTCDigest="AAAA"/>
              </rdf:RDF>
            </x:xmpmeta>
            <?xpacket end="w"?>
        """.trimIndent()

        val withXmp = ByteArrayByteWriter()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(original),
            byteWriter = withXmp,
            xmpXml = digestXmp
        )

        val updated = Kim.update(
            bytes = withXmp.toByteArray(),
            updates = setOf(MetadataUpdate.Keywords(setOf("new")))
        )

        /* Read the updated XMP digest and the updated IPTC records. */
        val metadata = assertNotNull(Kim.readMetadata(updated))

        /* The XMP digest must equal the MD5 of the rewritten IPTC
           records - the stale "AAAA" marker would make digest-aware
           tools report the stores as out of sync. */
        val xmpDigest = assertNotNull(
            XMPMetaFactory.parseFromString(assertNotNull(metadata.xmp)).getIptcDigest()
        )

        val expectedDigest = Md5.digest(
            IptcWriter.writeIptcBlockData(assertNotNull(metadata.iptc).records)
        ).toHex()

        assertEquals(expectedDigest.lowercase(), xmpDigest.lowercase())
    }
}
