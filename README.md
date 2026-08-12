# Kim - Kotlin Image Metadata

[![Kotlin](https://img.shields.io/badge/kotlin-2.4.10-blue.svg?logo=kotlin)](httpw://kotlinlang.org)
![JVM](https://img.shields.io/badge/-JVM-gray.svg?style=flat)
![Android](https://img.shields.io/badge/-Android-gray.svg?style=flat)
![iOS](https://img.shields.io/badge/-iOS-gray.svg?style=flat)
![Windows](https://img.shields.io/badge/-Windows-gray.svg?style=flat)
![Linux](https://img.shields.io/badge/-Linux-gray.svg?style=flat)
![macOS](https://img.shields.io/badge/-macOS-gray.svg?style=flat)
![JS](https://img.shields.io/badge/-JS-gray.svg?style=flat)
![WASM](https://img.shields.io/badge/-WASM-gray.svg?style=flat)

Kim is a Kotlin Multiplatform library for reading and writing image metadata.

## Features

* JPG: Read & Write EXIF, IPTC & XMP
* PNG: Read & Write `eXIf` chunk & XMP
    + Also read non-standard EXIF & IPTC from `tEXt`/`zTXt` chunk
* WebP: Read & Write EXIF & XMP
* HEIC / AVIF: Read EXIF & XMP
    + Support for animated AVIF files (AV1 Image Sequence)
* JXL: Read & Write EXIF & XMP of uncompressed files
* TIFF / RAW: Read EXIF & XMP
    + Full support for Adobe DNG, Canon CR2, Canon CR3 & Fujifilm RAF
    + Support for Nikon NEF, Sony ARW & Olympus ORF without lens info
    + Support for Panasonic RW2 without lens info and image size
    + API for preview image extraction of DNG, CR2, CR3, RAF, NEF, ARW & RW2
* GIF: Read & Write XMP
* Handling of XMP content through
  [XMP Core for Kotlin Multiplatform](https://github.com/StefanOltmann/xmpcore)
* Convenient `Kim.update()` API to perform updates to the relevant places
    + JPG: Lossless rotation by modifying only one byte (where present)
* `Kim.deleteMetadata()` API to remove all metadata, keeping the ICC profile

## Installation

```
implementation("de.stefan-oltmann:kim:<version>")
```

For the targets `wasmJs` & `js` you also need to specify this:

```
implementation(npm("pako", "2.1.0"))
```

## Sample usages

### Read metadata

`Kim.readMetadata()` takes `kotlin.ByteArray` on all platforms and depending on the platform also
`kotlinx.io.files.Path`, `kotlinx.io.Source` (for usage with Ktor) & `ByteReadChannel`,
`java.io.File`, `java.io.InputStream`, `NSData` (iOS) and `String` paths.

```kotlin
val bytes: ByteArray = loadBytes()

val metadata = Kim.readMetadata(bytes)

/* MediaMetadata has a proper toString() similar to the output of ExifTool */
println(metadata)

val orientation = metadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)

println("Orientation: $orientation")

val takenDate = metadata.findStringValue(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)

println("Taken date: $takenDate")
```

For streaming sources, `Kim.readMetadata()` also takes a `ByteReader`, so the file does not have to
be loaded into memory:

```kotlin
val byteReader = JvmInputStreamByteReader(inputFile.inputStream(), inputFile.length())

val metadata = Kim.readMetadata(byteReader)
```

### Create high level summary object

This creates an instance
of [MetadataSummary](src/commonMain/kotlin/de/stefan_oltmann/kim/model/MetadataSummary.kt). It
contains the following:

- Image size
- Orientation
- Date taken
- GPS coordinates
- Camera make & model
- Lens make & model
- ISO, Exposure time, F-Number, Focal length
- Image title & description
- Rating
- `XMP:pick` flag
- Keywords
- Faces (XMP-mwg-rs regions, used by Picasa and others)
- Persons in image

```kotlin
val bytes: ByteArray = loadBytes()

val summary = Kim.readMetadata(bytes).convertToSummary()
```

### Extract metadata bytes

`Kim.extractMetadataBytes()` determines the file type from the file header and returns the raw
metadata bytes. Cloud services can not reliably tell the mime type, so this can be used to upload
the metadata alongside the image.

```kotlin
val result = Kim.extractMetadataBytes(byteReader)

/* The detected media format, or NULL when it could not be determined. */
val mediaFormat: MediaFormat? = result.first

/* The raw metadata bytes to upload to the cloud service. */
val metadataBytes: ByteArray = result.second
```

### Extract preview image

`Kim.extractPreviewImage()` extracts the embedded preview image of DNG, CR2, CR3, RAF, NEF, ARW &
RW2 files as JPEG bytes.

```kotlin
val previewBytes: ByteArray? = Kim.extractPreviewImage(byteReader)

if (previewBytes != null)
    println("Preview image has ${previewBytes.size} bytes.")
```

### Change orientation using low level API

```kotlin
val inputFile = File("myphoto.jpg")
val outputFile = File("myphoto_changed.jpg")

val metadata = Kim.readMetadata(inputFile)

val outputSet: TiffOutputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

val rootDirectory = outputSet.getOrCreateRootDirectory()

rootDirectory.removeField(TiffTag.TIFF_TAG_ORIENTATION)
rootDirectory.add(TiffTag.TIFF_TAG_ORIENTATION, 8)

OutputStreamByteWriter(outputFile.outputStream()).use { outputStreamByteWriter ->

    JpegRewriter.updateExifMetadataLossless(
        byteReader = JvmInputStreamByteReader(inputFile.inputStream(), inputFile.length()),
        byteWriter = outputStreamByteWriter,
        outputSet = outputSet
    )
}
```

See the [example project](examples/kim-kotlin-jvm-sample/src/main/kotlin/Main.kt) for more details.

### Update metadata using Kim.update () API

`Kim.update()` applies the given updates to all formats that can represent them, so EXIF, IPTC and
XMP are updated simultaneously in one call.

```kotlin
val bytes: ByteArray = loadBytes()

/* A single update: */
val rotatedBytes = Kim.update(
    bytes = bytes,
    update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
)

/* Multiple updates in one call: */
val updatedBytes = Kim.update(
    bytes = bytes,
    updates = setOf(
        MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT),
        MetadataUpdate.TakenDate(timestamp),
        MetadataUpdate.Title("My title"),
        MetadataUpdate.Keywords(setOf("hello", "test"))
    )
)
```

The supported update types are:

| Update | Sets |
|---|---|
| `MetadataUpdate.Orientation` | Rotation (JPG supports a lossless single-byte swap) |
| `MetadataUpdate.TakenDate` | Date taken |
| `MetadataUpdate.GpsCoordinates` | GPS coordinates |
| `MetadataUpdate.LocationShown` | Location shown |
| `MetadataUpdate.GpsCoordinatesAndLocationShown` | GPS coordinates and location |
| `MetadataUpdate.Title` | Title |
| `MetadataUpdate.Description` | Description |
| `MetadataUpdate.Flagged` | The `XMP:pick` flag |
| `MetadataUpdate.Rating` | Star rating |
| `MetadataUpdate.Keywords` | Keywords |
| `MetadataUpdate.Faces` | Faces (XMP-mwg-rs regions) |
| `MetadataUpdate.Persons` | Persons in image |

An update call without any updates is rejected with an `ImageWriteException`.

See [AbstractUpdaterTest](src/commonTest/kotlin/de/stefan_oltmann/kim/format/AbstractUpdaterTest.kt)
for more samples.

#### Streaming update

The update can stream the file from a `ByteReader` to a `ByteWriter` without loading it into memory.
The image data is streamed in bounded chunks, so very large files can be updated efficiently. A
single-update overload exists for both the byte array and the streaming variant.

```kotlin
val byteReader = JvmInputStreamByteReader(inputFile.inputStream(), inputFile.length())

OutputStreamByteWriter(outputFile.outputStream()).use { outputStreamByteWriter ->

    Kim.update(
        byteReader = byteReader,
        byteWriter = outputStreamByteWriter,
        updates = setOf(MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT))
    )
}
```

### Delete metadata using Kim.deleteMetadata () API

`Kim.deleteMetadata()` removes all metadata of a file, but keeps the ICC chunks, because they would
change how the image is displayed.

* JPG: removes EXIF, XMP, IPTC & comment segments
* PNG: removes the `eXIf` chunk, all text chunks & the `tIME` chunk
* WebP: removes EXIF & XMP chunks and clears the VP8X metadata flags
* JXL: removes Exif & xml boxes
* GIF: removes the XMP application extension & comment extensions

```kotlin
val bytes: ByteArray = loadBytes()

val newBytes = Kim.deleteMetadata(bytes)
```

Like `Kim.update()`, `deleteMetadata()` also offers a streaming overload that writes to a
`ByteWriter` without loading the file into memory:

```kotlin
Kim.deleteMetadata(
    byteReader = byteReader,
    byteWriter = byteWriter
)
```

### Update thumbnail using Kim.updateThumbnail () API

```kotlin
val bytes: ByteArray = loadBytes()
val thumbnailBytes: ByteArray = loadThumbnailBytes()

val newBytes = Kim.updateThumbnail(
    bytes = bytes,
    thumbnailBytes = thumbnailBytes
)
```

### Using Java

See the [Java example project](examples/kim-java-sample/src/main/java/Main.java) how to use Kim in
Java projects.

## Limitations

* Does not read the image size and orientation for HEIC, AVIF & JPEG XL.
* Does not read brotli compressed metadata of JPEG XL due to missing brotli KMP libs.
* MakerNote support is experimental and limited.
    + Can't extract preview image of ORF as offsets are burried into MakerNote.
    + Can't identify lens info of NEF, ARW, RW2 & ORF because this is constructed from MakerNote
      fields.
    + Missing image size for RW2 as this is also burried in MakerNotes.
* There is right now no convienient tooling for GeoTiff like there is for GPS.

### Regarding HEIC & AVIF metadata

In the processing of HEIC and AVIF files, we handle them as standard ISOBMFF-based files, adhering
rigorously to the EIC/ISO 14496-12 specification. To preempt potential legal issues, we
intentionally omit certain boxes outlined in the HEIC specification, notably the image size ("ispe")
and image rotation ("irot") boxes. This approach extends to AVIF images, as they repurpose the same
boxes.

## Contributions

Contributions to Kim are welcome! If you encounter any issues, have suggestions for improvements, or
would like to contribute new features, please feel free to submit a pull request.

## Acknowledgements

* JetBrains for making [Kotlin](https://kotlinlang.org).
* Apache Software Foundation for
  making [Apache Commons Imaging](https://commons.apache.org/proper/commons-imaging/).
* Drew Noakes for making [metadata-extractor](https://github.com/drewnoakes/metadata-extractor).
* Phil Harvey for making [ExifTool](https://exiftool.org/).
* [Unsplash](https://unsplash.com) for providing test images.

## License

This code is under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

See the `NOTICE.txt` file for required notices and attributions.
