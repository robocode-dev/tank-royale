description = "Robocode Tank Royale Rumble executable (battle worker)"

group = "dev.robocode.tankroyale"

base {
    archivesName = "robocode-tankroyale-rumble-worker"
}

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
}

application {
    mainClass = "dev.robocode.tankroyale.rumble.worker.MainKt"
}

dependencies {
    implementation(project(":runner"))
    implementation(project(":lib:rumble-protocol"))

    testImplementation(project(":lib:common"))
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
    test {
        useJUnitPlatform {
            excludeTags("integration")
        }
        failFast = true
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
    }

    check {
        dependsOn(checkDependencyRules)
    }
}
