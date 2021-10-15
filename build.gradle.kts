import java.time.Year
import com.hierynomus.gradle.license.tasks.LicenseFormat

plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
    id("com.github.ben-manes.versions") version "0.39.0"
    id("com.github.hierynomus.license-base") version "0.16.1"
}

subprojects {
    apply(plugin = "com.github.hierynomus.license-base")

    license {
        header = rootProject.file("LICENSE.header")
        encoding = "UTF-8"
//        skipExistingHeaders = true
        mapping("java", "SLASHSTAR_STYLE")
        mapping("kt", "SLASHSTAR_STYLE")
        mapping("svg", "XML_STYLE")
        exclude("**/META-INF/**")
        exclude("**/*.png")
        exclude("**/*.txt")

        var ext = this as ExtensionAware
        with (ext) {
            extra["year"] = Year.now()
            extra["name"] = "Flemming Nørnberg Larsen"
        }
    }
}

tasks.named("assemble") {
    dependsOn("licenseFormatMain")
}
