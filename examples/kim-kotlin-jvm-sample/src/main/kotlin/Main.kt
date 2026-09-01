import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.writeBytes
import de.stefan_oltmann.kim.format.jpeg.JpegRewriter
import de.stefan_oltmann.kim.format.tiff.TiffContents
import de.stefan_oltmann.kim.format.tiff.TiffReader
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.format.tiff.constant.GeoTiffTag
import de.stefan_oltmann.kim.format.tiff.write.TiffOutputSet
import de.stefan_oltmann.kim.format.tiff.write.TiffWriter
import de.stefan_oltmann.kim.input.DefaultRandomAccessByteReader
import de.stefan_oltmann.kim.input.JvmInputStreamByteReader
import de.stefan_oltmann.kim.input.KotlinIoSourceByteReader
import de.stefan_oltmann.kim.jvm.readMetadata
import de.stefan_oltmann.kim.model.MetadataUpdate
import de.stefan_oltmann.kim.output.ByteArrayByteWriter
import de.stefan_oltmann.kim.output.OutputStreamByteWriter
import java.io.File

fun main() {

    printMetadata()

    updateTakenDate()

    updateTakenDateLowLevelApi()

    /* Various GeoTiff samples */
    setGeoTiffToJpeg()
    setGeoTiffToTiff()
    setGeoTiffToTiffUsingKotlinx()
}

fun printMetadata() {

    val inputFile = File("testphoto.jpg")

    val imageMetadata = Kim.readMetadata(inputFile)

    println(imageMetadata)
}

/**
 * Shows how to update the taken date using Kim.update() API
 */
fun updateTakenDate() {

    val update = MetadataUpdate.TakenDate(System.currentTimeMillis())

    val inputFile = File("testphoto.jpg")
    val outputFile = File("testphoto_mod1.jpg")

    JvmInputStreamByteReader(inputFile.inputStream(), inputFile.length()).use { byteReader ->

        OutputStreamByteWriter(outputFile.outputStream()).use { byteWriter ->

            Kim.update(byteReader, byteWriter, update)
        }
    }
}

/**
 * Shows how to update the taken date using the low level API.
 */
fun updateTakenDateLowLevelApi() {

    val inputFile = File("testphoto.jpg")
    val outputFile = File("testphoto_mod2.jpg")

    val metadata = Kim.readMetadata(inputFile) ?: return

    val outputSet: TiffOutputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

    val exifDirectory = outputSet.getOrCreateExifDirectory()

    exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
    exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL, "2222:02:02 13:37:42")

    JvmInputStreamByteReader(inputFile.inputStream(), inputFile.length()).use { byteReader ->

        OutputStreamByteWriter(outputFile.outputStream()).use { outputStreamByteWriter ->

            JpegRewriter.updateExifMetadata(
                byteReader = byteReader,
                byteWriter = outputStreamByteWriter,
                outputSet = outputSet
            )
        }
    }
}

fun setGeoTiffToJpeg() {

    val inputFile = File("testphoto.jpg")
    val outputFile = File("testphoto_mod3.jpg")

    val metadata = Kim.readMetadata(inputFile) ?: return

    val outputSet: TiffOutputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

    val rootDirectory = outputSet.getOrCreateRootDirectory()

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_MODEL_PIXEL_SCALE_TAG,
        doubleArrayOf(0.0002303616678184751, -0.0001521606816798535, 0.0)
    )

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_MODEL_TIEPOINT_TAG,
        doubleArrayOf(0.0, 0.0, 0.0, 8.915687629578438, 48.92432542097789, 0.0)
    )

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_GEO_KEY_DIRECTORY_TAG,
        shortArrayOf(1, 0, 2, 3, 1024, 0, 1, 2, 2048, 0, 1, 4326, 1025, 0, 1, 2)
    )

    JvmInputStreamByteReader(inputFile.inputStream(), inputFile.length()).use { byteReader ->

        OutputStreamByteWriter(outputFile.outputStream()).use { outputStreamByteWriter ->

            JpegRewriter.updateExifMetadata(
                byteReader = byteReader,
                byteWriter = outputStreamByteWriter,
                outputSet = outputSet
            )
        }
    }
}

/**
 * Shows how to update set GeoTiff to a TIF file using JVM API.
 *
 * CAUTION: Writing TIFF is experimental and may corrupt the file!
 */
fun setGeoTiffToTiff() {

    val inputFile = File("empty.tif")
    val outputFile = File("geotiff.tif")

    val tiffContents: TiffContents =
        JvmInputStreamByteReader(inputFile.inputStream(), inputFile.length()).use { byteReader ->
            byteReader.let {

                TiffReader.read(
                    /*
                     * TIFF files can be extremely large.
                     * It is not advisable to load them entirely into a ByteArray.
                     */
                    byteReader = DefaultRandomAccessByteReader(byteReader),
                    /*
                     * Per default the strip bytes are not read.
                     * For GeoTiff writing to a TIFF this needs to be turned on.
                     */
                    readTiffImageBytes = true
                )
            }
        }

    val outputSet: TiffOutputSet = tiffContents.createOutputSet()

    val rootDirectory = outputSet.getOrCreateRootDirectory()

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_MODEL_PIXEL_SCALE_TAG,
        doubleArrayOf(0.0002303616678184751, -0.0001521606816798535, 0.0)
    )

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_MODEL_TIEPOINT_TAG,
        doubleArrayOf(0.0, 0.0, 0.0, 8.915687629578438, 48.92432542097789, 0.0)
    )

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_GEO_KEY_DIRECTORY_TAG,
        shortArrayOf(1, 0, 2, 3, 1024, 0, 1, 2, 2048, 0, 1, 4326, 1025, 0, 1, 2)
    )

    OutputStreamByteWriter(outputFile.outputStream()).use { outputStreamByteWriter ->

        val tiffWriter = TiffWriter(outputSet.byteOrder)

        tiffWriter.write(
            byteWriter = outputStreamByteWriter,
            outputSet = outputSet
        )
    }
}

/**
 * Shows how to update set GeoTiff to a TIF file using kotlinx-io.
 */
fun setGeoTiffToTiffUsingKotlinx() {

    val inputPath = kotlinx.io.files.Path("empty.tif")
    val outputPath = kotlinx.io.files.Path("geotiff_kotlinx.tif")

    val tiffContents: TiffContents? =
        KotlinIoSourceByteReader.read(inputPath) { byteReader ->
            byteReader?.let {

                TiffReader.read(
                    /*
                     * TIFF files can be extremely large.
                     * It is not advisable to load them entirely into a ByteArray.
                     */
                    byteReader = DefaultRandomAccessByteReader(byteReader),
                    /*
                     * Per default the strip bytes are not read.
                     * For GeoTiff writing to a TIFF this needs to be turned on.
                     */
                    readTiffImageBytes = true
                )
            }
        }

    val outputSet: TiffOutputSet = tiffContents?.createOutputSet() ?: TiffOutputSet()

    val rootDirectory = outputSet.getOrCreateRootDirectory()

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_MODEL_PIXEL_SCALE_TAG,
        doubleArrayOf(0.0002303616678184751, -0.0001521606816798535, 0.0)
    )

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_MODEL_TIEPOINT_TAG,
        doubleArrayOf(0.0, 0.0, 0.0, 8.915687629578438, 48.92432542097789, 0.0)
    )

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_GEO_KEY_DIRECTORY_TAG,
        shortArrayOf(1, 0, 2, 3, 1024, 0, 1, 2, 2048, 0, 1, 4326, 1025, 0, 1, 2)
    )

    val byteArrayByteWriter = ByteArrayByteWriter()

    val tiffWriter = TiffWriter(outputSet.byteOrder)

    tiffWriter.write(
        byteWriter = byteArrayByteWriter,
        outputSet = outputSet
    )

    val updatedBytes = byteArrayByteWriter.toByteArray()

    outputPath.writeBytes(updatedBytes)
}

