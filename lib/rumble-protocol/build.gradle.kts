description = "Robocode Tank Royale Rumble host and worker messages"

group = "dev.robocode.tankroyale"

base {
    archivesName = "robocode-tankroyale-rumble-protocol"
}

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlinx.serialization.json)

    testImplementation(platform(testLibs.junit.bom))
    testImplementation(testLibs.bundles.junit)
}

// The messages are plain data. They never depend on the desktop program, the battle runner, the server or any other
// Rumble module, so both the host library and the worker can read them without sharing anything else.
val forbiddenProjects = setOf(":gui", ":runner", ":server", ":lib:rumble", ":lib:bot-project", ":rumble")

val checkDependencyRules by tasks.registering {
    group = "verification"
    description = "Fails when lib:rumble-protocol depends, directly or through another project, on a project it must not use."
    doLast {
        val violations = listOf(configurations.runtimeClasspath, configurations.compileClasspath)
            .flatMap { it.get().incoming.resolutionResult.allComponents }
            .mapNotNull { (it.id as? ProjectComponentIdentifier)?.projectPath }
            .filter { it in forbiddenProjects }
            .toSortedSet()
        check(violations.isEmpty()) {
            "lib:rumble-protocol must not depend on ${violations.joinToString()}"
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        failFast = true
    }

    check {
        dependsOn(checkDependencyRules)
    }
}
