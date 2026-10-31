description = "Robocode Tank Royale shared Rumble library"

group = "dev.robocode.tankroyale"

base {
    archivesName = "robocode-tankroyale-rumble"
}

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":lib:common"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":lib:bot-project"))
    testImplementation(platform(testLibs.junit.bom))
    testImplementation(testLibs.bundles.junit)
}

// The shared library never depends on the desktop program or the battle runner. The Swing and AWT part of the
// rule is checked on the sources by RumbleDependencyRulesTest.
val forbiddenProjects = setOf(":gui", ":runner")

val checkDependencyRules by tasks.registering {
    group = "verification"
    description = "Fails when lib:rumble depends, directly or through another project, on a project it must not use."
    doLast {
        val violations = listOf(configurations.runtimeClasspath, configurations.compileClasspath)
            .flatMap { it.get().incoming.resolutionResult.allComponents }
            .mapNotNull { (it.id as? ProjectComponentIdentifier)?.projectPath }
            .filter { it in forbiddenProjects }
            .toSortedSet()
        check(violations.isEmpty()) {
            "lib:rumble must not depend on ${violations.joinToString()}"
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        failFast = true
        // Reads the public Rumble in one test that is skipped unless asked for: -Drumble.live=true
        systemProperty("rumble.live", System.getProperty("rumble.live") ?: "false")
    }

    check {
        dependsOn(checkDependencyRules)
    }
}
