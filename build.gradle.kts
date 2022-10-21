import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import build.release.createRelease

description = "Robocode: Build the best - destroy the rest!"

group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val `ossrh-username`: String? by project
val `ossrh-password`: String? by project
val `tankroyale-github-token`: String? by project

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.nexus.publish)

    alias(libs.plugins.benmanes.versioning) // dependency management only
}

subprojects {

    repositories {
        mavenLocal()
        mavenCentral()
    }

    tasks {
        withType<KotlinCompile>().configureEach {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_11.toString()
            }
        }

        // Make sure to replace $version token in version.txt when processing the resources
        withType<ProcessResources> {
            filesMatching("version.txt") {
                expand(mapOf("version" to version))
            }
        }
    }
}

tasks {
    register("build-release") {
        dependsOn(
            "build",
            "sample-bots:java:zip", "sample-bots:csharp:zip",
            "buildDocs:uploadDocs", "bot-api:dotnet:uploadDocs", "bot-api:java:uploadDocs"
        )
    }

    register("create-release") {
        dependsOn("build-release")

        doLast {
            val version = libs.versions.tankroyale.get()
            createRelease(projectDir, version, `tankroyale-github-token`!!)
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(`ossrh-username`)
            password.set(`ossrh-password`)
        }
    }
}

val initializeSonatypeStagingRepository by tasks.existing
subprojects {
    initializeSonatypeStagingRepository {
        shouldRunAfter(tasks.withType<Sign>())
    }
}