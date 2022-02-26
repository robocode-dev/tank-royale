import java.nio.file.Files
import java.nio.file.Paths

group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()
description = "Bot API for Robocode Tank Royale"

val javadocTitle = "Robocode Tank Royale Bot API"
val artifactBaseName = "robocode-tankroyale-bot-api"


@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow.jar)
    alias(libs.plugins.hidetake.ssh)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = sourceCompatibility

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(libs.tankroyale.schema)
    implementation(libs.gson)
    implementation(libs.gson.extras)
    implementation(libs.nv.i18n)
}

tasks {
    jar {
        enabled = false
        dependsOn(
            shadowJar
        )
    }

    shadowJar.configure {
        archiveBaseName.set(artifactBaseName)
        archiveClassifier.set(null as String?) // get rid of "-all" classifier
    }

    val javadoc = withType<Javadoc> {
        title = "$javadocTitle $version"
        source(sourceSets.main.get().allJava)

        (options as StandardJavadocDocletOptions).apply {
            memberLevel = JavadocMemberLevel.PUBLIC
            overview = "src/main/javadoc/overview.html"

            addFileOption("-add-stylesheet", File(projectDir, "src/main/javadoc/themes/prism.css"))
            addBooleanOption("-allow-script-in-comments", true)
            addStringOption("Xdoclint:none", "-quiet")
        }
        exclude(
            "**/dev/robocode/tankroyale/botapi/internal/**",
            "**/dev/robocode/tankroyale/botapi/mapper/**",
            "**/dev/robocode/tankroyale/sample/**"
        )
        doLast {
            Files.copy(
                Paths.get("$projectDir/src/main/javadoc/prism.js"),
                Paths.get("$buildDir/docs/javadoc/prism.js")
            )
        }
    }

    register<Copy>("uploadDocs") {
        dependsOn(javadoc)

        val javadocDir = "../../docs/api/java"

        delete(javadocDir)
        mkdir(javadocDir)

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from("build/docs/javadoc")
        into(javadocDir)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = group as String?
            artifactId = artifactBaseName
            version
            from(components["java"])
        }
    }
}