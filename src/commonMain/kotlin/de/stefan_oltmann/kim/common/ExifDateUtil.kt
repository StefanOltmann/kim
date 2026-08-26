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
package de.stefan_oltmann.kim.common

private val emptyExifDateStrings = setOf(
    "0000:00:00 00:00:00",
    "    :  :     :  :  ",
    "                   "
)

/**
 * "yyyy:MM:dd" is 10 chars.
 */
private const val LENGTH_ONLY_DATE = 10

/**
 * "yyyy:MM:dd HH:mm:ss" is 19 chars.
 */
private const val LENGTH_DATE_WITH_TIME = 19

/**
 * "yyyy:MM:dd HH:mm" is 16 chars - some vendors drop the seconds.
 */
private const val LENGTH_DATE_WITHOUT_SECONDS = 16

private const val YEAR_AND_MONTH_SEPARATOR_INDEX = 4
private const val MONTH_AND_DAY_SEPARATOR_INDEX = 7
private const val TIME_SEPARATOR_INDEX = 10

private const val FIRST_SECOND_INDEX = 17
private const val SECOND_SECOND_INDEX = 18

/**
 * The EXIF year is always exactly four digits ("yyyy").
 */
private const val YEAR_DIGIT_COUNT = 4

public fun isExifDateEmpty(exifDate: String?): Boolean =
    exifDate.isNullOrBlank() || emptyExifDateStrings.contains(exifDate)

/**
 * Converts an EXIF-formatted date string to ISO 8601 format.
 *
 * EXIF dates are in the format of "yyyy:MM:dd HH:mm:ss" (19 chars),
 * which is transformed to ISO like "yyyy-MM-ddTHH:mm:ss".
 *
 * Vendor variants are tolerated: blank seconds are padded with zeros,
 * and 16-char dates missing the seconds entirely are normalized by
 * appending ":00". Only-date inputs ("yyyy:MM:dd") pass through with
 * separators replaced.
 *
 * @param exifDate An EXIF-formatted date string of at least 10 chars.
 * @return The converted ISO 8601 date string.
 * @throws IllegalArgumentException when the date is empty, shorter than
 *         10 chars, or the year portion does not contain exactly four
 *         digits.
 */
public fun convertExifDateToIso8601Date(exifDate: String): String {

    require(!isExifDateEmpty(exifDate)) { "Given date was empty: $exifDate" }

    /*
     * Different vendors may have chosen to write a variant of the date
     * instead of the specified format. To encounter this we do replacements
     * that always should result in an ISO 8601 date.
     *
     * The shortest date we should ever encounter is "yyyy:MM:dd" (10 chars)
     */
    require(exifDate.length >= LENGTH_ONLY_DATE) { "Invalid date: $exifDate" }

    /*
     * The year must be exactly four digits, so obvious garbage like
     * "aaaa:bb:cc dd:ee:ff" fails here instead of producing an invalid
     * ISO string that only fails later during LocalDateTime.parse.
     */
    require(exifDate.take(YEAR_DIGIT_COUNT).all(Char::isDigit)) {
        "Invalid year in date: $exifDate"
    }

    val charArray = exifDate.toCharArray()

    charArray[YEAR_AND_MONTH_SEPARATOR_INDEX] = '-'
    charArray[MONTH_AND_DAY_SEPARATOR_INDEX] = '-'

    if (charArray.size > LENGTH_ONLY_DATE)
        charArray[TIME_SEPARATOR_INDEX] = 'T'

    /**
     * We saw some files where some buggy software turned
     * "2023:05:12 18:04:00" into "2023:05:12 18:04:  ".
     * We don't want to lose the whole date, just because
     * some buggy software discarded the seconds.
     */
    if (charArray.size >= LENGTH_DATE_WITH_TIME) {

        if (charArray[FIRST_SECOND_INDEX] == ' ')
            charArray[FIRST_SECOND_INDEX] = '0'

        if (charArray[SECOND_SECOND_INDEX] == ' ')
            charArray[SECOND_SECOND_INDEX] = '0'
    }

    /*
     * Some vendors truncate the date to "yyyy:MM:dd HH:mm" (16 chars),
     * dropping the seconds entirely. Appending ":00" normalizes this to
     * a valid ISO datetime; without it, a later sub-second append would
     * produce "HH:mm.0", which is invalid ISO and silently loses the
     * taken date.
     */
    if (charArray.size == LENGTH_DATE_WITHOUT_SECONDS)
        return charArray.concatToString() + ":00"

    return charArray.concatToString()
}
