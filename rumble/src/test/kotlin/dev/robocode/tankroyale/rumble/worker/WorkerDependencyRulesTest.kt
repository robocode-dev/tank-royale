package dev.robocode.tankroyale.rumble.worker

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Path

class WorkerDependencyRulesTest {

    private val sources: List<Pair<String, String>> by lazy {
        Path.of("src", "main", "kotlin").toFile().walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .map { it.name to it.readText() }
            .toList()
    }

    // RBW-006

    @Test
    @Tag("RBW-006")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBW006_UnitNegative_theWorkerSourceNamesNoCredentialHomeFolderEnvironmentOrContainerSocket() {
        assertTrue(sources.isNotEmpty())
        val forbidden = listOf(
            "token", "github", "credential", "password", "secret", "user.home", "System.getenv", "docker.sock", "/var/run/docker",
            "dev.robocode.tankroyale.rumble.RumbleEnvironment", "dev.robocode.tankroyale.botproject", "javax.swing", "java.awt",
        )

        sources.forEach { (name, text) ->
            forbidden.forEach { word -> assertFalse(text.contains(word, ignoreCase = true), "$name names $word") }
        }
    }

    @Test
    @Tag("RBW-006")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBW006_UnitPositive_theWorkerImportsOnlyItsOwnPackageTheMessagesTheRunnerAndTheJdk() {
        val allowed = listOf("dev.robocode.tankroyale.rumble.worker", "dev.robocode.tankroyale.rumble.protocol", "dev.robocode.tankroyale.runner", "java.", "kotlin.")

        sources.forEach { (name, text) ->
            text.lines().filter { it.startsWith("import ") }.map { it.removePrefix("import ").trim() }.forEach { import ->
                assertTrue(allowed.any { import.startsWith(it) }, "$name imports $import")
            }
        }
    }
}
