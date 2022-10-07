import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Constants

val releasesPath by extra("public_html/tankroyale/releases")
val sampleBotsReleasePath by extra("$releasesPath/sample-bots")
val guiReleasePath by extra("$releasesPath/gui")

val htmlRoot by extra("~/public_html/tankroyale")
val apiPath by extra("$htmlRoot/api")


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
                jvmTarget = "11"
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
}