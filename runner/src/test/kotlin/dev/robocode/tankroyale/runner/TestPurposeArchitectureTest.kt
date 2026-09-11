package dev.robocode.tankroyale.runner

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/** Verifies the purpose contract for every JVM test source in the repository. */
class TestPurposeArchitectureTest {

    @Test
    @Tag("Arch")
    fun everyJvmTestHasExactlyOneEffectivePurpose() {
        val failures = mutableListOf<String>()
        val root = repositoryRoot()

        Files.walk(root).use { paths ->
            paths.filter { isJvmTestSource(it) }.forEach { path ->
                val source = Files.readString(path)
                if (path.fileName.toString() == "TestPurposeArchitectureTest.kt") {
                    return@forEach
                }
                if (source.contains("import io.kotest.core.spec.style.")) {
                    checkKotestSource(root, path, source, failures)
                } else {
                    checkJUnitSource(root, path, source, failures)
                }
            }
        }

        assertThat(failures)
            .withFailMessage("Test-purpose violations:\n%s", failures.joinToString("\n"))
            .isEmpty()
    }

    private fun checkJUnitSource(root: Path, path: Path, source: String, failures: MutableList<String>) {
        val lines = source.lines()
        val testAnnotation = Regex("""^\s*@(Test|ParameterizedTest|RepeatedTest|TestFactory|TestTemplate)\b""")
        val purposeTag = Regex("""@Tag\("([^"]+)"\)""")
        val classDeclaration = Regex("""^\s*(?:(?:public|private|protected|internal|abstract|final|open|sealed|data)\s+)*class\s+\w+""")

        lines.forEachIndexed { index, line ->
            if (!testAnnotation.matches(line)) return@forEachIndexed

            val purposes = mutableListOf<String>()
            var cursor = index - 1
            while (cursor >= 0 && isAnnotationOrBlank(lines[cursor])) {
                val candidate = lines[cursor].trim()
                if (candidate.startsWith("@")) {
                    purposeTag.findAll(candidate).map { it.groupValues[1] }
                        .filter(::isPurpose)
                        .forEach(purposes::add)
                }
                cursor--
            }
            cursor = index + 1
            while (cursor < lines.size && isAnnotationOrBlank(lines[cursor])) {
                val candidate = lines[cursor].trim()
                if (candidate.startsWith("@")) {
                    purposeTag.findAll(candidate).map { it.groupValues[1] }
                        .filter(::isPurpose)
                        .forEach(purposes::add)
                }
                cursor++
            }
            val effectivePurposes = if (purposes.isNotEmpty()) purposes else classPurposeValues(lines, classDeclaration, purposeTag, index)
            val acceptanceIds = effectivePurposes.filter(::isAcceptanceId)
            val effective = if (acceptanceIds.isNotEmpty()) acceptanceIds else effectivePurposes.filter(::isGenericPurpose)
            if (effective.size != 1) {
                failures += "${root.relativize(path)}:${index + 1} expected one purpose, found $purposes"
            }
        }
    }

    private fun classPurposeValues(
        lines: List<String>,
        classDeclaration: Regex,
        purposeTag: Regex,
        testIndex: Int,
    ): List<String> {
        val classIndex = lines.indexOfFirst { classDeclaration.containsMatchIn(it) }
        if (classIndex < 0 || classIndex >= testIndex) return emptyList()

        val purposes = mutableListOf<String>()
        var cursor = classIndex - 1
        while (cursor >= 0 && isAnnotationOrBlank(lines[cursor])) {
            val candidate = lines[cursor].trim()
            if (candidate.startsWith("@")) {
                purposeTag.findAll(candidate).map { it.groupValues[1] }
                    .filter(::isPurpose)
                    .forEach(purposes::add)
            }
            cursor--
        }
        return purposes
    }

    private fun checkKotestSource(root: Path, path: Path, source: String, failures: MutableList<String>) {
        val spec = Regex("class\\s+\\w+\\s*:\\s*\\w+Spec\\s*\\(\\s*\\{\\s*").find(source)
        if (spec == null) {
            failures += "${root.relativize(path)} has no recognizable Kotest spec"
            return
        }

        val purposes = Regex("tags\\(Tag\\(\\\"([^\\\"]+)\\\"\\)\\)")
            .findAll(source)
            .map { it.groupValues[1] }
            .filter(::isPurpose)
            .toList()
        val acceptanceIds = purposes.filter(::isAcceptanceId)
        val effective = if (acceptanceIds.isNotEmpty()) acceptanceIds else purposes.filter(::isGenericPurpose)
        if (effective.size != 1) {
            failures += "${root.relativize(path)} expected one spec purpose, found $purposes"
        }
    }

    private fun repositoryRoot(): Path {
        var current = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (!Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent ?: error("Could not locate repository root")
        }
        return current
    }

    private fun isJvmTestSource(path: Path): Boolean {
        if (!path.isRegularFile()) return false
        if (path.fileName.toString() !in setOf("TestPurposeArchitectureTest.kt") &&
            !path.fileName.toString().endsWith("Test.java") &&
            !path.fileName.toString().endsWith("Test.kt")
        ) return false
        val parts = path.iterator().asSequence().map { it.toString() }.toList()
        return parts.none { it in setOf("build", "bin", ".gradle", ".external") } &&
            parts.any { it == "test" }
    }

    private fun isPurpose(value: String): Boolean = isAcceptanceId(value) || isGenericPurpose(value)

    private fun isAnnotationOrBlank(line: String): Boolean = line.trim().isEmpty() || line.trim().startsWith("@")

    private fun isAcceptanceId(value: String): Boolean =
        Regex("(?:[A-Z][A-Z0-9]*-)+\\d+[a-z]?").matches(value)

    private fun isGenericPurpose(value: String): Boolean = value in setOf("Unit", "Sanity", "Arch")
}
