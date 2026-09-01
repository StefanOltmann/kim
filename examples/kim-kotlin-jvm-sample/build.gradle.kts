plugins {
    kotlin("jvm") version "2.4.10"
}

group = "de.stefan-oltmann"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("de.stefan-oltmann:kim:0.38.1")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.1")
}
