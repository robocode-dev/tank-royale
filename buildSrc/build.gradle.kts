import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "Robocode Tank Royale build sources"

buildscript {
    val kotlinVersion = "1.8.20"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", kotlinVersion))
    }
}

plugins {
    kotlin("jvm").version("1.8.20")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20230618")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }
}
