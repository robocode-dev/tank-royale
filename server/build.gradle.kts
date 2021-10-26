import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import proguard.gradle.ProGuardTask

val title = "Robocode Tank Royale Server"
description = "Server for running Robocode Tank Royale"

group = "dev.robocode.tankroyale"
version = "0.8.14"

val archiveFileName = "$buildDir/libs/robocode-tankroyale-server-$version.jar"

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.1.1")
    }
}

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    idea
}

tasks.withType<KotlinCompile> {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

idea {
    module {
        outputDir = file("$buildDir/classes/kotlin/main")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("dev.robocode.tankroyale:robocode-tankroyale-schema:0.8.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.2")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha5")
    implementation("info.picocli:picocli:4.6.1")
    implementation("org.fusesource.jansi:jansi:2.3.4")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.0.0.M1")
    testImplementation("io.mockk:mockk:1.12.0")
}

tasks.processResources {
    with(copySpec {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("/src/main/resources")
        include("version.txt")
        filter(ReplaceTokens::class, "tokens" to mapOf("version" to version))
    })
}

val fatJar = task<Jar>("fatJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Implementation-Title"] = title
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "dev.robocode.tankroyale.server.ServerKt"
    }
    from(
        configurations.compileClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) },
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    )
    exclude("*.kotlin_metadata")
    with(tasks["jar"] as CopySpec)
    archiveFileName.set("fat.jar")
}

val proguard = task<ProGuardTask>("proguard") {
    dependsOn(fatJar)
    injars("$buildDir/libs/fat.jar")
    outjars(archiveFileName)
    configuration("proguard-rules.pro")
}

tasks.named("build") {
    dependsOn(proguard)
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
