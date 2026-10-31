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

    testImplementation(platform(testLibs.junit.bom))
    testImplementation(testLibs.bundles.junit)
}

// The shared library never depends on the desktop program or the battle runner. The Swing and AWT part of the
// rule is checked on the sources by RumbleDependencyRulesTest.
val forbiddenProjects = setOf(":gui", ":runner")

val checkDependencyRules by tasks.registering {
    group = "verification"
    description = "Fails when lib:rumble depends on a project it must not depend on."
    val runtimeDependencies = configurations.runtimeClasspath.get().allDependencies
    val compileDependencies = configurations.compileClasspath.get().allDependencies
    doLast {
        val violations = (runtimeDependencies + compileDependencies)
            .filterIsInstance<ProjectDependency>()
            .map { it.path }
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
    }

    check {
        dependsOn(checkDependencyRules)
    }
}
