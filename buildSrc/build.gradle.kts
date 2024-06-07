import org.jetbrains.kotlin.gradle.dsl.JvmTarget

description = "Robocode Tank Royale build sources"

plugins {
    kotlin("jvm").version("2.0.0")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20240303")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}
