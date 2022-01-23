import proguard.gradle.ProGuardTask
import dev.robocode.tankroyale.archive.FatJar

val archiveTitle = "Robocode Tank Royale Booter"
description = "Application used for booting up Robocode Tank Royale bots"

group = "dev.robocode.tankroyale"
version = "0.9.5"

val archiveFileName = "$buildDir/libs/robocode-tankroyale-booter-$version.jar"

buildscript {
    dependencies {
        classpath(libs.proguard.gradle)
    }
}

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    idea
}

idea.module.outputDir = file("$buildDir/classes/kotlin/main")

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.picocli)
}

tasks {
    val fatJar by registering(FatJar::class) {
        dependsOn(clean, build)

        title.set(archiveTitle)
        mainClass.set("dev.robocode.tankroyale.booter.BooterKt")
    }

    val proguard by registering(ProGuardTask::class) {
        dependsOn(fatJar)
        injars("$buildDir/libs/${project.name}-$version.jar")
        outjars(archiveFileName)
        configuration("proguard-rules.pro")
    }

    register("archive") {
        dependsOn(proguard)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(archiveFileName)
            groupId = group as String?
            artifactId
            version
        }
    }
}
