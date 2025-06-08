import java.nio.file.Files

description = "Robocode Tank Royale Common Lib"

val javadocTitle = "Robocode Tank Royale Common Lib"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

base {
    archivesName = "robocode-tankroyale-common" // renames _all_ archive names
}

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.slf4j.api)
    implementation(libs.jansi)

    testImplementation(testLibs.kotest.junit5)
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
