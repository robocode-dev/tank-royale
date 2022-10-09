import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val kotlinVersion = "1.6.20"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", kotlinVersion))
    }
}

plugins {
    kotlin("jvm").version("1.6.20")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20220924")
}

tasks {
    withType<KotlinCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()

        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }
}