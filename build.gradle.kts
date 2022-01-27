import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Constants

val releasesPath by extra("public_html/tankroyale/releases")
val sampleBotsReleasePath by extra("$releasesPath/sample-bots")
val guiReleasePath by extra("$releasesPath/gui")

val htmlRoot by extra("~/public_html/tankroyale")
val apiPath by extra("$htmlRoot/api")

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
        withType<KotlinCompile> {
            sourceCompatibility = JavaVersion.VERSION_11.toString()
            targetCompatibility = JavaVersion.VERSION_11.toString()

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

        withType(Assemble::class) {
            dependsOn("licenseFormatMain")
        }
    }
}