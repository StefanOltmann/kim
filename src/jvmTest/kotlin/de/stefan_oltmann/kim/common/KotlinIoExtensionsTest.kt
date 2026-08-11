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
package de.stefan_oltmann.kim.common

import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.model.MediaFormat
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import java.nio.file.Files
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/*
 * The test is placed in jvmTest, because the extensions live in ktorMain.
 */
class KotlinIoExtensionsTest {

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    private fun tempDir(): Path {
        val dir = Files.createTempDirectory("kim-test")
        dir.toFile().deleteOnExit()
        return Path(dir.toString())
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testWriteAndReadBytes() {

        val path = tempDir() / "file.bin"

        val bytes = byteArrayOf(1, 2, 3, 4)

        path.writeBytes(bytes)

        assertTrue(path.exists())
        assertContentEquals(bytes, path.readBytes())
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testCopyTo() {

        val source = tempDir() / "source.bin"
        val destination = tempDir() / "destination.bin"

        source.writeBytes(byteArrayOf(9, 8, 7))

        source.copyTo(destination)

        assertContentEquals(byteArrayOf(9, 8, 7), destination.readBytes())
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testCopyToRejectsMissingSource() {

        assertFailsWith<IllegalArgumentException> {
            (tempDir() / "missing.bin").copyTo(tempDir() / "destination.bin")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testList() {

        val dir = tempDir()

        (dir / "one.bin").writeBytes(byteArrayOf(1))
        (dir / "two.bin").writeBytes(byteArrayOf(2))

        val names = SystemFileSystem.list(dir).map { it.name }.sorted()

        assertEquals(listOf("one.bin", "two.bin"), names)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testKotlinIoSourceByteReader() {

        val path = tempDir() / "media.jpg"

        val bytes = de.stefan_oltmann.kim.testdata.KimTestData.getBytesOf(1)

        path.writeBytes(bytes)

        var result: MediaFormat? = null

        de.stefan_oltmann.kim.input.KotlinIoSourceByteReader.read(path) { byteReader ->
            val metadata = Kim.readMetadata(checkNotNull(byteReader))
            result = metadata?.mediaFormat
        }

        assertEquals(MediaFormat.JPEG, result)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testKotlinIoSourceByteReaderRejectsMissingFile() {

        var blockCalled = false

        de.stefan_oltmann.kim.input.KotlinIoSourceByteReader.read(tempDir() / "missing.jpg") { byteReader ->
            blockCalled = true
            assertEquals(null, byteReader)
        }

        assertTrue(blockCalled)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testKotlinIoSinkByteWriter() {

        var writtenBytes: ByteArray? = null

        val path = tempDir() / "out.bin"

        de.stefan_oltmann.kim.output.KotlinIoSinkByteWriter.write(path) { byteWriter ->
            byteWriter.write(1.toByte())
            byteWriter.write(byteArrayOf(2, 3))
            byteWriter.flush()
            writtenBytes = path.readBytes()
        }

        assertContentEquals(byteArrayOf(1, 2, 3), writtenBytes)
    }

    @Test
    fun testKotlinIoSinkByteWriterWritesToBuffer() {

        val buffer = Buffer()

        val writer = de.stefan_oltmann.kim.output.KotlinIoSinkByteWriter(buffer)

        writer.write(5.toByte())
        writer.write(byteArrayOf(6, 7))
        writer.flush()

        assertContentEquals(byteArrayOf(5, 6, 7), buffer.readByteArray())

        writer.close()

        /* Sink close does not throw for a buffer. */
        assertNotNull(buffer)
    }

    private operator fun Path.div(name: String): Path = Path("$this/$name")
}
