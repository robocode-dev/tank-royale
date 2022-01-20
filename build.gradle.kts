import java.time.Year
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Constants

val homepageReleasePath by extra("public_html/tankroyale/releases")
val homepageSampleBotsReleasePath by extra("$homepageReleasePath/sample-bots")
val homepageGuiReleasePath by extra("$homepageReleasePath/gui")


plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.benmanes.versioning)
    alias(libs.plugins.hierynomus.license.base)
}

subprojects {
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