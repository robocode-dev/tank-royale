import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import build.release.createRelease
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

description = "Robocode: Build the best - destroy the rest!"

group = "dev.robocode.tankroyale"

val ossrhUsername: String? by project
val ossrhPassword: String? by project
val tankRoyaleGitHubToken: String? by project

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jreleaser)

    alias(libs.plugins.benmanes.versions) // dependency management only
}

subprojects {

    repositories {
        mavenLocal()
        mavenCentral()
    }

    tasks {
        withType<KotlinJvmCompile>().configureEach {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_11
            }
        }

        // Make sure to replace $version token in version.txt when processing the resources
        withType<ProcessResources> {
            filesMatching("version.properties") {
                expand(mapOf("version" to version))
            }
        }
    }
}

tasks {
    register("build-release") {
        dependsOn(
            "bot-api:java:assemble",     // Bot API for Java VM
            "bot-api:dotnet:assemble",   // Bot API for .Net
            "booter:assemble",           // Booter (for booting up bots locally)
            "server:assemble",           // Server
            "gui-app:assemble",          // GUI
            "sample-bots:java:zip",      // Sample bots for Java
            "sample-bots:csharp:zip",    // Sample bots for C#
            "buildDocs:uploadDocs",      // Documentation
            "bot-api:dotnet:uploadDocs", // Docfx documentation for .NET Bot API
            "bot-api:java:uploadDocs"    // Javadocs for Java Bot API
        )
    }

    register("create-release") {
        dependsOn("build-release")

        doLast {
            val version = libs.versions.tankroyale.get()
            if (tankRoyaleGitHubToken.isNullOrBlank()) {
                throw IllegalStateException("'token' is null or blank meaning that it is missing")
            }
            createRelease(projectDir, version, tankRoyaleGitHubToken!!)
        }
    }


    // Make root project's jreleaserPublish task depend on all Sign tasks from subprojects
    named("jreleaserPublish") {
        val signTasks = subprojects
            .filter { it.plugins.hasPlugin("maven-publish") }
            .flatMap { it.tasks.withType<Sign>() }

        dependsOn(signTasks)
    }
}

val isSnapshot = version.toString().endsWith("-SNAPSHOT")

jreleaser {
    project {
        description.set("Robocode Tank Royale")
        authors.set(listOf("Flemming N. Larsen"))
        license.set("Apache License, Version 2.0")
        links {
            homepage.set("https://robocode-dev.github.io/tank-royale/")
            documentation.set("https://robocode-dev.github.io/tank-royale/articles/")
            license.set("https://github.com/robocode-dev/tank-royale/blob/master/LICENSE")
        }

        // Set project version with snapshot detection
        version.set(version.toString())

        // Set snapshot flag based on version string
        snapshot {
            enabled.set(isSnapshot)
        }
    }

    signing {
        armored.set(true)
    }

    // Required configuration properties
    dryrun.set(true)        // Set to true for testing without actual publishing
    gitRootSearch.set(true) // Automatically find Git repository root
    strict.set(true)        // Fail on warnings

    deploy {
        maven {
            nexus2 {
                create("maven-central") {
                    active.set(org.jreleaser.model.Active.ALWAYS)

                    // Base repository URLs
                    url.set("https://s01.oss.sonatype.org/service/local")
                    snapshotUrl.set("https://s01.oss.sonatype.org/content/repositories/snapshots/")

                    // Credentials
                    username.set(ossrhUsername)
                    password.set(ossrhPassword)

                    // Only close and release if not a snapshot
//                    closeRepository.set(!isSnapshot)
//                    releaseRepository.set(!isSnapshot)

                    closeRepository.set(false)
                    releaseRepository.set(false)
                }
            }
        }
    }
}

// Log during configuration
logger.lifecycle("Project version: $version (${if (isSnapshot) "SNAPSHOT" else "RELEASE"} version)")

// Configure signing for subprojects
subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("maven-publish")) {
            plugins.apply("signing")
            
            // Configure signing for all publications
            configure<SigningExtension> {
                useGpgCmd()
                sign(extensions.getByType<PublishingExtension>().publications)
            }
        }
    }
}
