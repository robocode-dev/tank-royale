import proguard.gradle.ProGuardTask
import dev.robocode.tankroyale.tasks.FatJar

group = "dev.robocode.tankroyale"
version = "0.9.3"
description = "Graphical user interface for Robocode Tank Royale"

val jarManifestTitle = "Robocode Tank Royale GUI"
val jarManifestMainClass = "dev.robocode.tankroyale.gui.MainWindowKt"

val archiveFileName = "$buildDir/libs/robocode-tankroyale-gui-$version.jar"

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
        include("robocode-tankroyale-booter-*.jar")
        rename("(.*)-[0-9]+\\..*.jar", "\$1.jar")
    }

    val copyServerJar by registering(Copy::class) {
        dependsOn(inspectClassesForKotlinIC, ":server:proguard")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":server").file("/build/libs"))
        into(project.idea.module.outputDir)
        include("robocode-tankroyale-server-*.jar")
        rename("(.*)-[0-9]+\\..*.jar", "\$1.jar")
    }

    val fatJar by registering(FatJar::class) {
        dependsOn(classes, copyBooterJar, copyServerJar)

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

    register("copyJars") {
        dependsOn(copyBooterJar, copyServerJar)
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