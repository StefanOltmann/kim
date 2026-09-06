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
 * The intended use case is to retain metadata when a new
 * image is created due to scaling, rotation, or other modifications.
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
     * @throws ImageReadException if the source or destination file could not be read
     */
    public fun copy(
        source: Path,
        destination: Path
    ): Unit = tryWithImageReadException {

        val sourceMetadataChunks: List<PngChunk> =
            KotlinIoSourceByteReader.read(source) { byteReader ->
                byteReader?.let {
                    PngImageParser.readChunks(
                        byteReader = byteReader,
                        chunkTypeFilter = chunkTypesToCopy
                    )
                }
            } ?: throw ImageReadException("Failed to read source chunks: $source")

        val destinationChunks: List<PngChunk> =
            KotlinIoSourceByteReader.read(destination) { byteReader ->
                byteReader?.let {
                    PngImageParser.readChunks(
                        byteReader = byteReader,
                        chunkTypeFilter = null // = All of them
                    )
                }
            } ?: throw ImageReadException("Failed to read destination chunks: $destination")

        val filteredDestinationChunks = destinationChunks.filterNot {
            chunkTypesToCopy.contains(it.type)
        }

        val newChunks = filteredDestinationChunks.toMutableList().apply {
            addAll(
                index = insertionIndexAfterIhdr(),
                elements = sourceMetadataChunks
            )
        }

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
                chunkTypeFilter = null // = All of them
            )

        val filteredDestinationChunks = destinationChunks.filterNot {
            chunkTypesToCopy.contains(it.type)
        }

        val newChunks = filteredDestinationChunks.toMutableList().apply {
            addAll(
                index = insertionIndexAfterIhdr(),
                elements = sourceMetadataChunks
            )
        }

        val byteWriter = ByteArrayByteWriter()

        PngWriter.writeImage(
            chunks = newChunks,
            byteWriter = byteWriter
        )

        return byteWriter.toByteArray()
    }
}
