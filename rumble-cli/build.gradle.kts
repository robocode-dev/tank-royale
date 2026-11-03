import build.tasks.FatJar

description = "Robocode Tank Royale Rumble command line (program name: rumble)"

group = "dev.robocode.tankroyale"

base {
    archivesName = "robocode-tankroyale-rumble-cli"
}

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

val cliMainClass = "dev.robocode.tankroyale.rumble.cli.MainKt"

dependencies {
    implementation(project(":lib:common"))
    implementation(project(":lib:rumble"))
    implementation(project(":lib:rumble-protocol"))
    implementation(project(":lib:bot-project"))
    implementation(libs.kotlinx.serialization.json)
    // The core only, without the terminal library: a smaller program, and no native access warning from Java.
    implementation(libs.clikt.core)

    testImplementation(platform(testLibs.junit.bom))
    testImplementation(testLibs.bundles.junit)
}

// The command line is the controller, so it carries the Rumble library. It never depends on the battle worker as a
// project: the worker's jar comes inside it as a resource, so none of the worker's classes are on this classpath.
val forbiddenProjects = setOf(":gui", ":runner", ":rumble")

val checkDependencyRules by tasks.registering {
    group = "verification"
    description = "Fails when the Rumble command line depends, directly or through another project, on a project it must not use."
    doLast {
        val violations = listOf(configurations.runtimeClasspath, configurations.compileClasspath)
            .flatMap { it.get().incoming.resolutionResult.allComponents }
            .mapNotNull { (it.id as? ProjectComponentIdentifier)?.projectPath }
            .filter { it in forbiddenProjects }
            .toSortedSet()
        check(violations.isEmpty()) {
            "The Rumble command line must not depend on ${violations.joinToString()}"
        }
    }
}

val fatJarName = "${base.archivesName.get()}-${project.version}.jar"

tasks {
    processResources {
        filesMatching("rumble-cli.properties") { expand("version" to project.version.toString()) }
    }

    // The battle worker, carried inside the command line as a jar resource next to the classes, the way the battle
    // runner carries the server and the booter.
    val copyWorkerJar by registering(Copy::class) {
        dependsOn(":rumble:fatJar")
        from(project(":rumble").layout.buildDirectory.dir("libs")) {
            include("robocode-tankroyale-rumble-worker-*.jar")
            exclude("*-javadoc.jar", "*-sources.jar")
        }
        into(layout.buildDirectory.dir("classes/kotlin/main"))
        rename(".*", "robocode-tankroyale-rumble-worker.jar")
    }

    classes {
        dependsOn(copyWorkerJar)
    }

    val fatJar by registering(FatJar::class) {
        dependsOn(classes, copyWorkerJar)
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveFileName.set(fatJarName)
        title.set("Robocode Tank Royale Rumble Command Line")
        mainClass.set(cliMainClass)
    }

    jar {
        enabled = false
        dependsOn(fatJar)
    }

    test {
        useJUnitPlatform {
            excludeTags("integration")
        }
        failFast = true
    }

    val integrationTest by registering(Test::class) {
        description = "Runs the command line and the battle worker as real processes."
        group = "verification"

        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath

        useJUnitPlatform {
            includeTags("integration")
        }
        failFast = true

        dependsOn(fatJar, ":rumble:fatJar", ":sample-bots:java:build")

        systemProperty("rumble.cli.jar", layout.buildDirectory.file("libs/$fatJarName").get().asFile.absolutePath)
        systemProperty("rumble.worker.jar", project(":rumble").layout.buildDirectory.file("libs/robocode-tankroyale-rumble-worker-${project.version}.jar").get().asFile.absolutePath)
        systemProperty("sampleBots.java.dir", project(":sample-bots:java").layout.buildDirectory.dir("archive").get().asFile.absolutePath)
    }

    check {
        dependsOn(checkDependencyRules)
    }
}
