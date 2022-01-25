import dev.robocode.tankroyale.tasks.FatJar
import proguard.gradle.ProGuardTask

group = "dev.robocode.tankroyale"
version = "0.9.5"
description = "Application used for booting up Robocode Tank Royale bots"

val jarManifestTitle = "Robocode Tank Royale Booter"
val jarManifestMainClass = "dev.robocode.tankroyale.booter.BooterKt"

val archiveFileName = "$buildDir/libs/robocode-tankroyale-booter-$version.jar"

buildscript {
    dependencies {
        classpath(libs.proguard.gradle)
    }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    idea
}

idea.module.outputDir = file("$buildDir/classes/kotlin/main") // needed?

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.picocli)
}

tasks {
    val fatJar by registering(FatJar::class) {
        dependsOn(classes)

        title.set(jarManifestTitle)
        mainClass.set(jarManifestMainClass)
    }

    val proguard by registering(ProGuardTask::class) {
        dependsOn(fatJar)

        injars("$buildDir/libs/${project.name}-$version.jar")
        outjars(archiveFileName)
        configuration("proguard-rules.pro")
    }

    jar { // Replace jar task
        actions = emptyList()
        finalizedBy(proguard)
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