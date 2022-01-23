import java.time.Year
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.apache.tools.ant.filters.ReplaceTokens

// Constants

val releasesPath by extra("public_html/tankroyale/releases")
val sampleBotsReleasePath by extra("$releasesPath/sample-bots")
val guiReleasePath by extra("$releasesPath/gui")

val apiPath by extra("~/public_html/tankroyale/api")


plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.benmanes.versioning)
    alias(libs.plugins.hierynomus.license.base)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_11.toString()))
}

subprojects {

    repositories {
        mavenLocal()
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()

        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }

    // Make sure to replace ${version} token when processing resources, for example for the version.txt file
    tasks.withType<ProcessResources> {
        doFirst { // must be done prior to copying the resources for this to work
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            from("/src/main/resources")
            include("version.txt")
            filter(ReplaceTokens::class, "tokens" to mapOf("version" to version))
        }
    }

    apply(plugin = "com.github.hierynomus.license-base")

    license {
        header = rootProject.file("LICENSE.header")
        encoding = "UTF-8"
        mapping("java", "SLASHSTAR_STYLE")
        mapping("kt", "SLASHSTAR_STYLE")
        mapping("svg", "XML_STYLE")
        exclude("**/META-INF/**")
        exclude("**/*.png")
        exclude("**/*.txt")

        val ext = this as ExtensionAware
        with (ext) {
            extra["year"] = Year.now()
            extra["name"] = "Flemming Nørnberg Larsen"
        }
    }
}

allprojects {
    tasks.registering(Assemble::class) {
        dependsOn("licenseFormatMain")
    }
}