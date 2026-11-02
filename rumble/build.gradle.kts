import build.tasks.FatJar

description = "Robocode Tank Royale Rumble executable (battle worker)"

// A group of its own: this project and lib:rumble are both called "rumble", and Gradle would take two projects with the
// same group and name for one module and substitute one for the other.
group = "dev.robocode.tankroyale.executable"

base {
    archivesName = "robocode-tankroyale-rumble-worker"
}

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
}

val workerMainClass = "dev.robocode.tankroyale.rumble.worker.MainKt"

dependencies {
    implementation(project(":runner"))
    implementation(project(":lib:rumble-protocol"))

    testImplementation(project(":lib:common"))
    // Only the live host check uses the host library, to start this worker as a real process. It is not on the worker's own classpath.
    testImplementation(project(":lib:rumble"))
    testImplementation(platform(testLibs.junit.bom))
    testImplementation(testLibs.bundles.junit)
}

// The worker runs other people's bots, so it must never be able to reach a GitHub credential. Its only project
// dependencies are the battle runner and the message module. WorkerDependencyRulesTest scans its sources for
// anything that names a token, the home folder or a container socket.
val allowedProjects = setOf(":runner", ":lib:rumble-protocol", ":lib:client", ":lib:common", ":lib:intent-diagnostics", ":rumble")
val forbiddenProjects = setOf(":lib:rumble", ":lib:bot-project", ":gui")

val checkDependencyRules by tasks.registering {
    group = "verification"
    description = "Fails when the worker depends, directly or through another project, on a project it must not use."
    doLast {
        val used = listOf(configurations.runtimeClasspath, configurations.compileClasspath)
            .flatMap { it.get().incoming.resolutionResult.allComponents }
            .mapNotNull { (it.id as? ProjectComponentIdentifier)?.projectPath }
            .toSortedSet()
        val violations = used.filter { it in forbiddenProjects || it !in allowedProjects }
        check(violations.isEmpty()) {
            "The rumble worker must not depend on ${violations.joinToString()}"
        }
    }
}

tasks {
    // A runnable jar with the runner and the messages inside it, like the booter and the runner are, so the worker can be
    // started with java -jar. The runner's own jar carries the server and the booter as resources, and they come along.
    val fatJar by registering(FatJar::class) {
        dependsOn(classes)
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveFileName.set("${base.archivesName.get()}-${project.version}.jar")
        title.set("Robocode Tank Royale Rumble Battle Worker")
        mainClass.set(workerMainClass)
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
        // The exact classpath the worker's process loads, without the test classes or the host library.
        systemProperty("rumble.worker.classpath", sourceSets["main"].runtimeClasspath.asPath)
    }

    val integrationTest by registering(Test::class) {
        description = "Runs a real battle through the worker (needs the embedded server, booter and sample bots)."
        group = "verification"

        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath

        useJUnitPlatform {
            includeTags("integration")
        }
        failFast = true

        dependsOn(":runner:jar", ":sample-bots:java:build")

        systemProperty("sampleBots.java.dir", project(":sample-bots:java").layout.buildDirectory.dir("archive").get().asFile.absolutePath)
        // The worker's own classpath, without the test classes or the host library.
        systemProperty("rumble.worker.classpath", sourceSets["main"].runtimeClasspath.asPath)
    }

    check {
        dependsOn(checkDependencyRules)
    }
}
