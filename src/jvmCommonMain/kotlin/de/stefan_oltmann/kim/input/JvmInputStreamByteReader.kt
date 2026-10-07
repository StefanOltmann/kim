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
package de.stefan_oltmann.kim.input

import java.io.InputStream

/**
 * A ByteReader that reads from an InputStream.
 *
 * Shared by every JVM-based backend: the desktop JVM and Android.
 */
public open class JvmInputStreamByteReader(

    /**
     * The stream the bytes are read from. Protected so platform
     * subclasses can implement platform-specific read strategies.
     */
    protected val inputStream: InputStream,

    contentLength: Long
) : ByteReader {

    /**
     * A non-positive hint means the size is unknown, for example when a
     * content provider does not report one. It is reported as unbounded,
     * so parsers treat the stream end as the only truncation evidence -
     * a missing hint would otherwise let them reject valid content.
     */
    override val contentLength: Long =
        if (contentLength > 0) contentLength else Long.MAX_VALUE

    override fun readByte(): Byte? {

        val nextByte = inputStream.read()

        if (nextByte == -1)
            return null

        return nextByte.toByte()
    }

    override fun readBytes(count: Int): ByteArray =
        readBytesImpl(count)

    /**
     * The actual read strategy, kept internal and overridable so the
     * Android subclass can substitute the legacy loop below API 33:
     * [InputStream.readNBytes] does not exist there, and a public
     * Android-reachable path must never reach it.
     */
    internal open fun readBytesImpl(count: Int): ByteArray {

        require(count >= 0) { "Count must not be negative: $count" }

        return inputStream.readNBytes(count)
    }

    override fun close(): Unit =
        inputStream.close()
}
