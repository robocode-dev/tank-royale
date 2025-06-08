import java.nio.file.Files

description = "Robocode Tank Royale Client Lib for Java"

val javadocTitle = "Robocode Tank Royale Client"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

base {
    archivesName = "robocode-tankroyale-client" // renames _all_ archive names
}

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":lib:common"))
    implementation(libs.kotlinx.serialization.json)

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }

    withJavadocJar()
    withSourcesJar()
}

tasks {
    test {
        useJUnitPlatform()
        failFast = true
    }

    javadoc {
        title = "$javadocTitle $version"
        source(sourceSets.main.get().allJava)

        (options as StandardJavadocDocletOptions).apply {
            memberLevel = JavadocMemberLevel.PROTECTED
            overview = layout.projectDirectory.file("src/main/javadoc/overview.html").asFile.path

            addFileOption("-add-stylesheet", layout.projectDirectory.file("src/main/javadoc/themes/prism.css").asFile)
            addBooleanOption("-allow-script-in-comments", true)
            addStringOption("Xdoclint:none", "-quiet")
        }
        doLast {
            val sourceFile = layout.projectDirectory.file("src/main/javadoc/prism.js").asFile.toPath()
            val targetFile = layout.buildDirectory.file("docs/javadoc/prism.js").get().asFile.toPath()
            Files.copy(sourceFile, targetFile)
        }
    }

}
