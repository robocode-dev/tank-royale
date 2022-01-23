import proguard.gradle.ProGuardTask

val title = "Robocode Tank Royale Booter"
description = "Utility app for booting up bots from locale storage onto websocket"

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

idea {
    module {
        outputDir = file("$buildDir/classes/kotlin/main")
    }
}

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.picocli)
}

tasks {

    val fatJar by registering(Jar::class) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Implementation-Title"] = title
            attributes["Implementation-Version"] = archiveVersion
            attributes["Main-Class"] = "dev.robocode.tankroyale.booter.BooterKt"
        }
        from(
            configurations.compileClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) },
            configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
        )
        exclude("*.kotlin_metadata")
        with(getAt("jar") as CopySpec)
        archiveFileName.set("fat.jar")
    }

    val proguard by registering(ProGuardTask::class) {
        dependsOn(fatJar)
        injars("$buildDir/libs/fat.jar")
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
