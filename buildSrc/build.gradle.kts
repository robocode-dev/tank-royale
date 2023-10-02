import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "Robocode Tank Royale build sources"

plugins {
    kotlin("jvm").version("1.9.0")
}

repositories {
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
