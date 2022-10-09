import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import build.release.createRelease


val `tankroyale-github-token`: String? by project

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.benmanes.versioning)
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