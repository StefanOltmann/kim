import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    id("signing")
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.build.time.tracker)
    alias(libs.plugins.git.versioning)
    alias(libs.plugins.resources)
    alias(libs.plugins.versions)
    alias(libs.plugins.maven.publish)
}

repositories {
    google()
    mavenCentral()
}

val productName: String = "Kim"

private val mainEntryPoint = "de.stefan_oltmann.kim.main"

group = "de.stefan_oltmann.kim"
description = "Kotlin Image Metadata manipulation library"
version = "0.0.0"

gitVersioning.apply {

    refs {
        /* Main branch contains the current dev version */
        branch("main") {
            version = "\${commit.short}"
        }
        /* Release / tags have real version numbers */
        tag("v(?<version>.*)") {
            version = "\${ref.version}"
        }
    }

    /* Fallback if branch was not found (for feature branches) */
    rev {
        version = "\${commit.short}"
    }
}

buildTimeTracker {
    sortBy.set(com.asarkar.gradle.buildtimetracker.Sort.DESC)
}

detekt {
    source.setFrom("src", "build.gradle.kts")
    config.setFrom("detekt.yml")
    allRules = true
    parallel = true
    ignoreFailures = false
}

kotlin {

    explicitApi()

    android {

        namespace = "de.stefan_oltmann.kim"

        compileSdk = libs.versions.android.compile.sdk.get().toInt()

        minSdk = libs.versions.android.min.sdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        androidResources {
            enable = true
        }

        withHostTest {}
    }

    mingwX64("win") {
        binaries {
            executable(setOf(NativeBuildType.RELEASE)) {
                baseName = "kim"
                entryPoint = mainEntryPoint
            }
            staticLib(namePrefix = "", setOf(NativeBuildType.RELEASE)) {
                baseName = "kim"
            }
        }
    }

    linuxX64 {
        binaries {
            executable(setOf(NativeBuildType.RELEASE)) {
                entryPoint = mainEntryPoint
            }
            staticLib(namePrefix = "", setOf(NativeBuildType.RELEASE)) {
                baseName = "kim"
            }
        }
    }

    linuxArm64 {
        binaries {
            executable(setOf(NativeBuildType.RELEASE)) {
                entryPoint = mainEntryPoint
            }
            staticLib(namePrefix = "", setOf(NativeBuildType.RELEASE)) {
                baseName = "kim"
            }
        }
    }

    jvm {

        java {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    js()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs()

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmWasi()

    val commonMain = sourceSets.getByName("commonMain") {

        dependencies {

            /* Date handling */
            implementation(libs.kotlinx.datetime)

            /* XMP handling */
            api(libs.xmpcore)
        }
    }

    val commonTest = sourceSets.getByName("commonTest") {
        dependencies {

            /* Kotlin Test */
            implementation(kotlin("test"))

            /* Multiplatform file access */
            implementation(libs.kotlinx.io.core)

            /* Test resources */
            implementation(libs.resources)
        }
    }

    val xcf = XCFramework()

    listOf(
        /* App Store */
        iosArm64(),
        /* Apple Silicon iOS Simulator */
        iosSimulatorArm64(),
        /* macOS Devices */
        macosArm64()
    ).forEach {

        it.binaries.executable(setOf(NativeBuildType.RELEASE)) {
            baseName = "kim"
            entryPoint = mainEntryPoint
        }

        it.binaries.framework(
            buildTypes = setOf(NativeBuildType.RELEASE)
        ) {
            baseName = "kim"
            /* Part of the XCFramework */
            xcf.add(this)
        }
    }

    /*
     * Extra sourceSet to exclude unsupported features from JS / wasmJS targets.
     */
    val ktorMain = sourceSets.create("ktorMain") {

        dependsOn(commonMain)

        dependencies {

            /*
             * Ktor extensions
             *
             * Not available in commonMain due to missing WASM support.
             */
            implementation(libs.ktor.io)

            /*
             * Multiplatform file access
             *
             * Not available in commonMain due to missing JS browser support.
             */
            implementation(libs.kotlinx.io.core)
        }
    }

    val posixMain = sourceSets.create("posixMain") {

        dependsOn(commonMain)
        dependsOn(ktorMain)
    }

    sourceSets.getByName("jvmMain") {

        dependsOn(commonMain)
        dependsOn(ktorMain)
    }

    sourceSets.getByName("androidMain") {

        dependsOn(commonMain)
        dependsOn(ktorMain)
    }

    sourceSets.getByName("winMain") {
        dependsOn(posixMain)
    }

    sourceSets.getByName("linuxX64Main") {
        dependsOn(posixMain)
    }

    sourceSets.getByName("linuxArm64Main") {
        dependsOn(posixMain)
    }

    val iosArm64Main = sourceSets.getByName("iosArm64Main")
    val iosSimulatorArm64Main = sourceSets.getByName("iosSimulatorArm64Main")
    val macosArm64Main = sourceSets.getByName("macosArm64Main")

    sourceSets.create("appleMain") {

        dependsOn(commonMain)
        dependsOn(ktorMain)
        dependsOn(posixMain)

        iosArm64Main.dependsOn(this)
        iosSimulatorArm64Main.dependsOn(this)
        macosArm64Main.dependsOn(this)
    }

    val iosArm64Test = sourceSets.getByName("iosArm64Test")
    val iosSimulatorArm64Test = sourceSets.getByName("iosSimulatorArm64Test")
    val macosArm64Test = sourceSets.getByName("macosArm64Test")

    sourceSets.create("appleTest") {

        dependsOn(commonTest)

        iosArm64Test.dependsOn(this)
        iosSimulatorArm64Test.dependsOn(this)
        macosArm64Test.dependsOn(this)
    }

    sourceSets.getByName("jsMain") {

        dependsOn(commonMain)

        dependencies {
            api(npm("pako", "2.1.0"))
        }
    }

    val wasmJsMain = sourceSets.getByName("wasmJsMain")
    // val wasmWasiMain by sourceSets.getting

    sourceSets.create("wasmMain") {

        dependsOn(commonMain)

        wasmJsMain.dependsOn(this)
        // wasmWasiMain.dependsOn(this)

        dependencies {

            implementation(libs.kotlinx.browser)

            implementation(npm("pako", libs.versions.pako.get()))
        }
    }
}

// region Writing version.txt for GitHub Actions
val writeVersion: TaskProvider<Task> = tasks.register("writeVersion") {
    group = "build"
    description = "Writes the current project version to build/version.txt"
    doLast {
        File("build/version.txt").writeText(project.version.toString())
    }
}

tasks.getByPath("build").finalizedBy(writeVersion)
// endregion

// region Maven publish

val signingEnabled: Boolean = System.getenv("SIGNING_ENABLED")?.toBoolean() ?: false

mavenPublishing {

    publishToMavenCentral()

    if (signingEnabled)
        signAllPublications()

    coordinates(
        groupId = "de.stefan-oltmann",
        artifactId = "kim",
        version = version.toString()
    )

    pom {

        name = productName
        description = "Kotlin Multiplatform library for image metadata manipulation"
        url = "https://github.com/StefanOltmann/kim"

        licenses {
            license {
                name = "Apache License 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        developers {
            developer {
                name = "Stefan Oltmann"
                url = "https://stefan-oltmann.de/"
                roles = listOf("maintainer", "developer")
                properties = mapOf("github" to "StefanOltmann")
            }
        }

        scm {
            url = "https://github.com/StefanOltmann/kim"
            connection = "scm:git:git://github.com/StefanOltmann/kim.git"
        }
    }
}
// endregion

// region Code coverage
kover {

    reports {

        /* Common filters for all report variants */
        filters {

            excludes {

                /*
                 * These Android classes use the Android framework
                 * (ContentResolver, Uri, Build) and therefore cannot
                 * be covered by host JVM tests.
                 */
                classes(
                    "de.stefan_oltmann.kim.android.KimAndroid",
                    "de.stefan_oltmann.kim.android.ContentResolverExtensionsKt"
                )
            }
        }

        /* Common verification rules for all report variants */
        verify {

            rule {
                minBound(95)
            }
        }

        total {

            verifyAppend {
                onCheck = true
            }
        }

        variant("jvm") {

            verifyAppend {
                onCheck = true
            }
        }

        variant("android") {

            verifyAppend {
                onCheck = true
            }
        }
    }
}
// endregion
