import proguard.gradle.ProGuardTask
import dev.robocode.tankroyale.archive.FatJar

val archiveTitle = "Robocode Tank Royale GUI Application"
description = "GUI application for starting battles for Robocode Tank Royale"

group = "dev.robocode.tankroyale"
val artifactId = "robocode-tankroyale-gui"
version = "0.9.2"

val archiveFileName = "$buildDir/libs/$artifactId-$version.jar"

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
    implementation(libs.miglayout.swing)

    runtimeOnly(project(":server"))
    runtimeOnly(project(":booter"))
}

tasks {

    val copyBooterJar by registering(Copy::class) {
        dependsOn(":booter:archive")

        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(project(":booter").file("/build/libs"))
        into(project.idea.module.outputDir)
        include("robocode-tankroyale-booter-*.jar")
        rename("(.*)-[0-9]+\\..*.jar", "\$1.jar")
    }

    val copyServerJar by registering(Copy::class) {
        dependsOn(":server:archive")

        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(project(":server").file("/build/libs"))
        into(project.idea.module.outputDir)
        include("robocode-tankroyale-server-*.jar")
        rename("(.*)-[0-9]+\\..*.jar", "\$1.jar")
    }

    val fatJar by registering(FatJar::class) {
        dependsOn(copyServerJar, copyBooterJar)

        title.set(archiveTitle)
        mainClass.set("dev.robocode.tankroyale.gui.MainWindowKt")
    }

    val proguard by registering(ProGuardTask::class) {
        dependsOn(fatJar)
        injars("${project.name}-$version.jar")
        outjars(archiveFileName)
        configuration("proguard-rules.pro")
    }

    register("copyJars") {
        dependsOn(copyBooterJar, copyServerJar)
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