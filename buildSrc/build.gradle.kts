import org.jetbrains.kotlin.gradle.dsl.JvmTarget

description = "Robocode Tank Royale build sources"

plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.json)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
