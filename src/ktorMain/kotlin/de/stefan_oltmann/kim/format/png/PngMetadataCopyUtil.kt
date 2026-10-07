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

import de.stefan_oltmann.kim.common.ImageReadException
import de.stefan_oltmann.kim.common.tryWithImageReadException
import de.stefan_oltmann.kim.format.png.chunk.PngChunk
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.input.KotlinIoSourceByteReader
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.KotlinIoSinkByteWriter
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * A utility for transferring all metadata chunks from one file to another.
 *
 * This is a maintained public feature in its own right, used by production
 * apps to retain metadata when a new image is created due to scaling,
 * rotation, or other modifications. It is not a leftover of [PngWriter].
 *
 * The contract: the tEXt, zTXt, iTXt and eXIf chunks of the source replace
 * the same types in the destination and are inserted right behind the
 * mandatory IHDR chunk. Both files must be valid PNGs; the output is
 * written by [PngWriter], so both entry points produce byte-identical
 * results. The file variant writes a temporary file and moves it
 * atomically, so the destination is never left half-written.
 */
public object PngMetadataCopyUtil {

    private val chunkTypesToCopy = listOf(
        PngChunkType.TEXT,
        PngChunkType.ZTXT,
        PngChunkType.ITXT,
        PngChunkType.EXIF
    )

    /**
     * Copies the metadata chunks from the source file to the destination file.
     *
     * The output is written through a temporary file and moved atomically,
     * so the destination is never left half-written.
     *
     * @throws ImageReadException if the source or destination file could not be read
     * @throws ImageWriteException if the output could not be written
     * @throws IOException if the temporary file or the atomic move failed
     */
    public fun copy(
        source: Path,
        destination: Path
    ) {

        /*
         * Only the read steps run inside the read wrapper: a failure of
         * the write or the move must surface as its own error type, not
         * as a read failure of the input files.
         */
        val sourceMetadataChunks: List<PngChunk> =
            tryWithImageReadException {
                KotlinIoSourceByteReader.read(source) { byteReader ->
                    byteReader?.let {
                        PngImageParser.readChunks(
                            byteReader = byteReader,
                            chunkTypeFilter = chunkTypesToCopy
                        )
                    }
                } ?: throw ImageReadException("Failed to read source chunks: $source")
            }

        val destinationChunks: List<PngChunk> =
            tryWithImageReadException {
                KotlinIoSourceByteReader.read(destination) { byteReader ->
                    byteReader?.let {
                        PngImageParser.readChunks(
                            byteReader = byteReader,
                            chunkTypeFilter = null /*  = All of them */
                        )
                    }
                } ?: throw ImageReadException("Failed to read destination chunks: $destination")
            }

        val newChunks = mergeChunks(sourceMetadataChunks, destinationChunks)

        val tempFilePath = tempFilePathFor(destination)

        try {

            KotlinIoSinkByteWriter.write(tempFilePath) { byteWriter ->

                PngWriter.writeImage(
                    chunks = newChunks,
                    byteWriter = byteWriter
                )
            }

            SystemFileSystem.atomicMove(
                tempFilePath,
                destination
            )

        } catch (ex: Throwable) {

            /*
             * The atomic move did not happen, so the temporary file would
             * leak into the destination directory forever.
             */
            if (SystemFileSystem.exists(tempFilePath))
                SystemFileSystem.delete(tempFilePath)

            throw ex
        }
    }

    /**
     * Returns the index where the metadata chunks are inserted: right
     * behind the mandatory IHDR chunk. A destination without an IHDR
     * would receive the metadata at a spec-invalid position and is
     * rejected instead.
     */
    private fun List<PngChunk>.insertionIndexAfterIhdr(): Int {

        val ihdrIndex = indexOfFirst { it.type == PngChunkType.IHDR }

        if (ihdrIndex == -1)
            throw ImageReadException("The destination PNG has no IHDR chunk.")

        return ihdrIndex + 1
    }

    /*
     * Merges the metadata chunks of the source into the chunk list of
     * the destination: the destination chunks of the copied types are
     * dropped and the source chunks are inserted right behind the IHDR.
     */
    private fun mergeChunks(
        sourceMetadataChunks: List<PngChunk>,
        destinationChunks: List<PngChunk>
    ): List<PngChunk> =
        destinationChunks
            .filterNot { chunkTypesToCopy.contains(it.type) }
            .toMutableList()
            .apply {
                addAll(
                    index = insertionIndexAfterIhdr(),
                    elements = sourceMetadataChunks
                )
            }

    /**
     * Builds the path of the temporary file for the given destination.
     *
     * The parent may be NULL for bare relative destinations - building
     * the string naively produced a literal "null/..." directory name.
     */
    internal fun tempFilePathFor(destination: Path): Path {

        val fileName = "${destination.name}.tmp"

        return if (destination.parent != null)
            Path("${destination.parent}/${fileName}")
        else
            Path(fileName)
    }

    /**
     * Copies the metadata chunks from the source bytes to the destination bytes.
     *
     * @throws ImageReadException if the source or destination bytes could not be read
     * @throws ImageWriteException if the output could not be written
     */
    public fun copy(
        source: ByteArray,
        destination: ByteArray
    ): ByteArray {

        val sourceMetadataChunks: List<PngChunk> =
            PngImageParser.readChunks(
                byteReader = ByteArrayByteReader(source),
                chunkTypeFilter = chunkTypesToCopy
            )

        val destinationChunks: List<PngChunk> =
            PngImageParser.readChunks(
                byteReader = ByteArrayByteReader(destination),
                chunkTypeFilter = null /*  = All of them */
            )

        val newChunks = mergeChunks(sourceMetadataChunks, destinationChunks)

        val byteWriter = ByteArrayByteWriter()

        PngWriter.writeImage(
            chunks = newChunks,
            byteWriter = byteWriter
        )

        return byteWriter.toByteArray()
    }
}
