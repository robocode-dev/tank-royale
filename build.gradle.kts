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

subprojects {

    // Make sure to replace ${version} token when processing resources, for example for the version.txt file
    tasks.withType(ProcessResources::class) {
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

tasks.named("assemble") {
    dependsOn("licenseFormatMain")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_11.toString()))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}