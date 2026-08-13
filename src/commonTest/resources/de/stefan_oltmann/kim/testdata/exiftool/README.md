# ExifTool dumps of the test files

This folder contains the ExifTool reference dumps of every test file in
[`../full`](../full) (`media_1` to `media_88`). They document how ExifTool
sees the metadata structure of each file and are used to compare the space
computation of the lossless EXIF writer with ExifTool.

For each test file there are two dumps:

* `media_N.txt` - a verbose text dump
* `media_N.html` - an HTML dump

Both are generated with ExifTool 13.59:

```bash
exiftool.exe -v4 -u -U "testdata/full/media_N.ext" > testdata/exiftool/media_N.txt
exiftool.exe -htmlDump0 -v2 -u -U "testdata/full/media_N.ext" > testdata/exiftool/media_N.html
```

* `-v4` / `-v2` - verbose output including offsets and structure
* `-u` - show unknown tags
* `-U` - show unknown tag values
* `-htmlDump0` - dump page 0 of the HTML dump

Files without EXIF or TIFF metadata (e.g. the GIF file) only contain a
minimal note in the dump ("No EXIF or TIFF information found in image").

To regenerate the dumps, run the commands above for every test file in
`testdata/full` (all formats, excluding the `.txt` companion files).
