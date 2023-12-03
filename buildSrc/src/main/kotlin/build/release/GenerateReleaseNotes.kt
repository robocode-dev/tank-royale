package build.release

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.IllegalStateException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

fun generateReleaseNotes(projectDir: File, version: String): String {

    val versionsFilename = File(projectDir, "VERSIONS.MD").absolutePath

    val extractedVersion = extractVersion(versionsFilename)
    check(extractedVersion == version) {
        "Version in $versionsFilename is $extractedVersion, which does not match version $version"
    }

    val releaseNotes = extractReleaseNotes(versionsFilename)
    val releaseDoc = getReleaseDocumentation(projectDir, version)

    return releaseNotes + releaseDoc
}

private fun extractVersion(versionsFilename: String): String {
    val reader = BufferedReader(FileReader(versionsFilename))

    for (line in reader.lines()) {
        if (line.trim().startsWith("## ")) {
            return "\\d+\\.\\d+\\.\\d+".toRegex().find(line)?.value ?: throw IllegalStateException("Version was not found")
        }
    }
    throw IllegalStateException("Line containing version could not be located")
}

private fun extractReleaseNotes(versionsFilename: String): String {
    val builder = StringBuilder()
    val reader = BufferedReader(FileReader(versionsFilename, StandardCharsets.UTF_8))

    for (line in reader.lines()) {
        if (line.trim().startsWith("## ")) {
            builder.append(line).append('\n'); break
        }
    }
    for (line in reader.lines()) {
        if (line.trim().startsWith("## ")) break
        builder.append(line).append('\n')
    }
    return builder.toString()
}

private fun getReleaseDocumentation(projectDir: File, version: String) =
    Files.readString(File(projectDir, "buildSrc/src/main/resources/release/release-docs-template.md").toPath())
        .replace("{VERSION}", version)
