/*
 * Copyright 2026 Stefan Oltmann
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package de.stefan_oltmann.kim.format.png

import com.goncalossilva.resources.Resource
import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.convertHexStringToByteArray
import de.stefan_oltmann.kim.common.copyTo
import de.stefan_oltmann.kim.common.exists
import de.stefan_oltmann.kim.common.readBytes
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class PngMetadataCopyUtilTest {

    fun getFullImageDiskPath(index: Int): String {

        val bytes = KimTestData.getBytesOf(index)

        val tmp = java.io.File.createTempFile("kim-test-$index-", ".tmp")

        tmp.writeBytes(bytes)

        tmp.deleteOnExit()

        return tmp.absolutePath
    }

    @Test
    fun testCopy() {

        val source = Path(getFullImageDiskPath(52))

        val destination = Path("build/copy_test.png")

        /* Copy test image to local folder. */
        Path(getFullImageDiskPath(51)).copyTo(destination)

        /* Check that the file was actually copied. */
        assertTrue(destination.exists(), "copy_test.png does not exist.")

        PngMetadataCopyUtil.copy(
            source = source,
            destination = destination
        )

        val expectedBytes =
            Resource("de/stefan_oltmann/kim/copy_test.png").readBytes()

        val actualBytes = destination.readBytes()

        val equals = expectedBytes.contentEquals(actualBytes)

        if (!equals)
            fail("copy_test.png has not the expected bytes!")
    }

    @Test
    fun testCopyByteArray() {

        val sourceBytes = Path(getFullImageDiskPath(52)).readBytes()

        val destinationBytes = Path(getFullImageDiskPath(51)).readBytes()

        val expectedBytes =
            Resource("de/stefan_oltmann/kim/copy_test.png").readBytes()

        val actualBytes = PngMetadataCopyUtil.copy(
            source = sourceBytes,
            destination = destinationBytes
        )

        val equals = expectedBytes.contentEquals(actualBytes)

        if (!equals)
            fail("copy_test.png has not the expected bytes!")
    }

    /**
     * Regression test: a bare relative destination has no parent path.
     * The old string concatenation built a literal "null/out.png.tmp"
     * path and failed.
     */
    @Test
    fun testTempFilePathForBareRelativeDestination() {

        val tempFilePath = PngMetadataCopyUtil.tempFilePathFor(
            Path("out.png")
        )

        assertTrue(tempFilePath.name.startsWith("out.png."))
        assertTrue(tempFilePath.name.endsWith(".tmp"))

        assertNull(tempFilePath.parent)
    }

    /**
     * A destination inside a directory keeps that directory for its
     * temporary file.
     */
    @Test
    fun testTempFilePathForNestedDestination() {

        val tempFilePath = PngMetadataCopyUtil.tempFilePathFor(
            Path("build/sub/dir/out.png")
        )

        assertTrue(tempFilePath.name.startsWith("out.png."))
        assertTrue(tempFilePath.name.endsWith(".tmp"))

        assertTrue(
            tempFilePath.toString().replace('\\', '/').startsWith("build/sub/dir/out.png.")
        )
    }

    /**
     * Regression test: every call must produce its own temporary path. A
     * deterministic name lets two concurrent copies of the same
     * destination interleave their writes into one file, publishing a
     * corrupted PNG.
     */
    @Test
    fun testTempFilePathsAreUniquePerCall() {

        val destination = Path("build/out.png")

        val first = PngMetadataCopyUtil.tempFilePathFor(destination)
        val second = PngMetadataCopyUtil.tempFilePathFor(destination)

        assertNotEquals(first.name, second.name)
    }

    /**
     * Regression test: a user file that happens to carry the deterministic
     * temp name must survive a copy untouched - the old deterministic
     * name truncated it and the atomic move consumed it.
     */
    @Test
    fun testCopyDoesNotTouchAnExistingTmpFileOfTheOldName() {

        val destinationDirectory = Path("build/tmp-name-guard-${System.nanoTime()}")

        SystemFileSystem.createDirectories(destinationDirectory)

        val destination = Path("$destinationDirectory/out.png")

        Path(getFullImageDiskPath(51)).copyTo(destination)

        val markerBytes = "user data".encodeToByteArray()

        val preExistingTmp = Path("$destinationDirectory/out.png.tmp")

        preExistingTmp.writeBytes(markerBytes)

        try {

            PngMetadataCopyUtil.copy(
                source = Path(getFullImageDiskPath(52)),
                destination = destination
            )

            assertTrue(
                markerBytes.contentEquals(preExistingTmp.readBytes()),
                "The pre-existing out.png.tmp was modified by the copy."
            )
        } finally {

            SystemFileSystem.delete(destination, mustExist = false)
            SystemFileSystem.delete(preExistingTmp, mustExist = false)
            SystemFileSystem.delete(destinationDirectory, mustExist = false)
        }
    }

    /**
     * Regression test: if the copy fails while writing the temporary
     * file, no temporary file may leak into the destination directory.
     */
    @Test
    fun testFailedCopyDoesNotLeakTempFile() {

        val source = Path(getFullImageDiskPath(52))

        /* The destination directory does not exist, so writing fails. */
        val destination =
            Path("build/missing-dir-${System.nanoTime()}/copy_test.png")

        try {

            PngMetadataCopyUtil.copy(
                source = source,
                destination = destination
            )

            fail("Expected the copy to fail.")

        } catch (_: Exception) {
            /* Expected - the destination directory cannot be created. */
        }

        val leakedTempFiles = SystemFileSystem.list(Path("build"))
            .filter { it.name.endsWith(".tmp") }

        assertTrue(
            leakedTempFiles.isEmpty(),
            "Leaked temporary files: $leakedTempFiles"
        )
    }

    /**
     * A failure in the write or move step must surface as its own error
     * type. Labeling it ImageReadException would send callers into their
     * "bad input file" handling for what is an output problem.
     */
    @Test
    fun testWriteFailureDoesNotSurfaceAsReadException() {

        val destination = Path("build/copy_test.png")

        Path(getFullImageDiskPath(51)).copyTo(destination)

        /*
         * A destination held open without FILE_SHARE_DELETE makes the
         * atomic move fail on Windows while both input files read
         * cleanly. (The temporary path is unique per call, so blocking
         * exactly that path is not possible anymore.)
         */
        val heldDestination = java.io.RandomAccessFile(destination.toString(), "rw")

        try {

            PngMetadataCopyUtil.copy(
                source = Path(getFullImageDiskPath(52)),
                destination = destination
            )

            fail("Expected the copy to fail on the held destination.")

        } catch (ex: Throwable) {

            assertFalse(
                ex is ImageReadException,
                "A write failure surfaced as ImageReadException: $ex"
            )
        } finally {
            heldDestination.close()
        }
    }

    /**
     * The destination metadata chunks must follow the mandatory IHDR
     * chunk. A malformed destination without an IHDR must be rejected
     * instead of receiving the metadata at a spec-invalid position.
     */
    @Test
    fun testCopyRejectsDestinationWithoutIhdr() {

        val source = KimTestData.getHeaderBytesOf(
            KimTestData.PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX
        )

        /* PNG signature plus a bare IEND chunk, no IHDR. */
        val destination = convertHexStringToByteArray(
            "89504e470d0a1a0a" + "00000000" + "49454e44" + "ae426082"
        )

        try {
            PngMetadataCopyUtil.copy(
                source = source,
                destination = destination
            )

            fail("Expected the copy to fail for a destination without IHDR.")
        } catch (expected: ImageReadException) {
            /* Expected. */
        }
    }
}
