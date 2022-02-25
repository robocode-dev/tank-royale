import dev.robocode.tankroyale.tasks.FatJar
import proguard.gradle.ProGuardTask

group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()
description = "Graphical user interface for Robocode Tank Royale"

val jarManifestTitle = "Robocode Tank Royale GUI"
val jarManifestMainClass = "dev.robocode.tankroyale.gui.MainWindowKt"

val archiveBaseName = "robocode-tankroyale-gui"
val archiveFileName = "$buildDir/libs/$archiveBaseName-$version.jar"

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
    implementation(libs.miglayout.swing)
}

tasks {
    val copyBooterJar by registering(Copy::class) {
        dependsOn(inspectClassesForKotlinIC, ":booter:proguard")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":booter").file("/build/libs"))
        into(project.idea.module.outputDir)
        include("robocode-tankroyale-booter-*-proguard.jar")
        rename(".*", "robocode-tankroyale-booter.jar")
    }

    val copyServerJar by registering(Copy::class) {
        dependsOn(inspectClassesForKotlinIC, ":server:proguard")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":server").file("/build/libs"))
        into(project.idea.module.outputDir)
        include("robocode-tankroyale-server-*-proguard.jar")
        rename(".*", "robocode-tankroyale-server.jar")
    }

    val copyJars = register("copyJars") {
        dependsOn(copyBooterJar, copyServerJar)
    }

    val fatJar by registering(FatJar::class) {
        dependsOn(classes, copyJars)

        title.set(jarManifestTitle)
        mainClass.set(jarManifestMainClass)

        outputFilename.set(archiveFileName)
    }

    val proguard by registering(ProGuardTask::class) {
        dependsOn(fatJar)

        injars("$buildDir/libs/${project.name}-$version.jar")
        outjars(archiveFileName)
        configuration("proguard-rules.pro")
    }

    jar {
        enabled = false
        dependsOn(
            proguard
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(archiveFileName)
            groupId = group as String?
            artifactId = archiveBaseName
            version
        }
    }
}