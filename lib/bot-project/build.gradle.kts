description = "Robocode Tank Royale bot project library"

group = "dev.robocode.tankroyale"

base {
    archivesName = "robocode-tankroyale-bot-project"
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

// The bot project library knows no desktop, battle runner or Rumble service code. The import part of the rule
// (Swing, AWT, network clients) is checked on the sources by BotProjectDependencyRulesTest.
val forbiddenProjects = setOf(":gui", ":runner", ":lib:rumble")

val checkDependencyRules by tasks.registering {
    group = "verification"
    description = "Fails when lib:bot-project depends, directly or through another project, on a project it must not use."
    doLast {
        val violations = listOf(configurations.runtimeClasspath, configurations.compileClasspath)
            .flatMap { it.get().incoming.resolutionResult.allComponents }
            .mapNotNull { (it.id as? ProjectComponentIdentifier)?.projectPath }
            .filter { it in forbiddenProjects }
            .toSortedSet()
        check(violations.isEmpty()) {
            "lib:bot-project must not depend on ${violations.joinToString()}"
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
