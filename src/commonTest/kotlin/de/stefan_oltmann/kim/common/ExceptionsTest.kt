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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Regression tests for the exception wrapping contract.
 *
 * Fatal VM errors must never be masked as ordinary parse failures,
 * because callers would retry or continue on broken virtual machine
 * state.
 */
class ExceptionsTest {

    private class FatalTestError(message: String) : Error(message)

    @Test
    fun testFatalErrorsAreNotWrappedOnRead() {

        val error = assertFailsWith<FatalTestError> {
            tryWithImageReadException { throw FatalTestError("read boom") }
        }

        assertEquals("read boom", error.message)
    }

    @Test
    fun testFatalErrorsAreNotWrappedOnWrite() {

        val error = assertFailsWith<FatalTestError> {
            tryWithImageWriteException { throw FatalTestError("write boom") }
        }

        assertEquals("write boom", error.message)
    }

    @Test
    fun testOtherExceptionsAreWrappedOnRead() {

        val exception = assertFailsWith<ImageReadException> {
            tryWithImageReadException { throw IllegalStateException("inner read") }
        }

        assertEquals("inner read", exception.cause?.message)
    }

    @Test
    fun testOtherExceptionsAreWrappedOnWrite() {

        val exception = assertFailsWith<ImageWriteException> {
            tryWithImageWriteException { throw IllegalStateException("inner write") }
        }

        assertEquals("inner write", exception.cause?.message)
    }

    @Test
    fun testImageReadExceptionIsNotDoubleWrapped() {

        val exception = assertFailsWith<ImageReadException> {
            tryWithImageReadException {
                throw ImageReadException("original")
            }
        }

        assertEquals("original", exception.message)
    }

    @Test
    fun testImageWriteExceptionIsNotDoubleWrapped() {

        val exception = assertFailsWith<ImageWriteException> {
            tryWithImageWriteException {
                throw ImageWriteException("original")
            }
        }

        assertEquals("original", exception.message)
    }
}
