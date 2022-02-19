import proguard.gradle.ProGuardTask

group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()
description = "Booter for booting up Robocode Tank Royale bots"

val jarManifestTitle = "Robocode Tank Royale Booter"
val jarManifestMainClass = "dev.robocode.tankroyale.booter.BooterKt"

val artifactBaseName = "robocode-tankroyale-booter"
val artifactBaseFilename = "${buildDir}/libs/${project.name}-${project.version}"

buildscript {
    dependencies {
        classpath(libs.proguard.gradle)
    }
}

@Suppress("DSL_SCOPE_VIOLATION") // remove later when IntelliJ supports the `libs.` DSL
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.shadow.jar)
    `maven-publish`
}

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.picocli)
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