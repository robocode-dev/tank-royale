import proguard.gradle.ProGuardTask

group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()
description = "Server for running Robocode Tank Royale"

val jarManifestTitle = "Robocode Tank Royale Server"
val jarManifestMainClass = "dev.robocode.tankroyale.server.ServerKt"

val artifactBaseName = "robocode-tankroyale-server"
val artifactBaseFilename = "${buildDir}/libs/${project.name}-${project.version}"

buildscript {
    dependencies {
        classpath(libs.proguard.gradle)
    }
}

@Suppress("DSL_SCOPE_VIOLATION") // remove later
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.shadow.jar)
    `maven-publish`
}

dependencies {
    implementation(libs.tankroyale.schema)
    implementation(libs.java.websocket)
    implementation(libs.slf4j.simple)
    implementation(libs.picocli)
    implementation(libs.jansi)

    testImplementation(libs.kotest.junit5)
    testImplementation(libs.mockk)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = jarManifestMainClass
            attributes["Implementation-Title"] = jarManifestTitle
            attributes["Implementation-Version"] = archiveVersion
            attributes["Implementation-Vendor"] = "robocode.dev"
        }
    }

    val proguard by registering(ProGuardTask::class) { // used for compacting and code-shaking
        dependsOn(shadowJar)
        injars("${artifactBaseFilename}-all.jar")
        outjars("${artifactBaseFilename}-proguard.jar")
        configuration("proguard-rules.pro")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(proguard.get().outJarFiles[0]) {
                    builtBy(proguard)
                }
                groupId = group as String?
                artifactId = artifactBaseName
                version
            }
        }
    }
}