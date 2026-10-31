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
package de.stefan_oltmann.kim

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

/**
 * The README is the public documentation of the update API surface, so its
 * update table must name every [MetadataUpdate] type - a missing row leaves
 * a supported update undiscoverable for the API consumer.
 *
 * The update types are extracted from the [MetadataUpdate] source file, so
 * adding a new update type without a README row fails this test.
 */
class ReadmeUpdateTableTest {

    /**
     * Every update type is declared as a data class inside the
     * [MetadataUpdate] source file, so this pattern finds exactly them.
     */
    private val updateDeclaration = Regex("""public data class (\w+)\(""")

    @Test
    fun testReadmeUpdateTableListsEveryMetadataUpdateType() {

        val source = findProjectFile(
            "src/commonMain/kotlin/de/stefan_oltmann/kim/model/MetadataUpdate.kt"
        ).readText()

        val readme = findProjectFile("README.md").readText()

        val updateTypes = updateDeclaration.findAll(source).map { it.groupValues[1] }.toList()

        /*
         * Guards the loop against silently passing on an empty type list,
         * for example when the source layout changed.
         */
        assertTrue(updateTypes.isNotEmpty())

        for (updateType in updateTypes)
            assertContains(
                readme,
                "| `MetadataUpdate.$updateType`",
                message = "The README update table misses MetadataUpdate.$updateType."
            )
    }

    /**
     * Walks up from the working directory, because the test JVM may start
     * in the repository root or in a module directory.
     */
    private fun findProjectFile(relativePath: String): Path {

        var directory = Path("").toAbsolutePath()

        while (true) {

            val candidate = directory / relativePath

            if (candidate.exists())
                return candidate

            directory = directory.parent
                ?: error("$relativePath not found above the working directory.")
        }
    }
}
