package dev.robocode.tankroyale.botproject

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

/** One problem found in a bot folder, with a stable [code] and the change that fixes it in plain words. */
data class Finding(val code: String, val file: String, val message: String, val fix: String)

/** What validating a folder found. [notes] are things to know that do not stop a submission. */
data class ValidationReport(val findings: List<Finding>, val notes: List<String> = emptyList()) {
    val passes: Boolean get() = findings.isEmpty()
}

/**
 * Applies the bot repository's per-folder checks to a bot or team folder on the author's computer, so the author can
 * fix problems before submitting. It lists every problem at once. It does not check what needs the catalog (name
 * clashes, ownership, versions), does not run the bot, and does not check the Unix launcher's executable bit, because
 * a Windows author cannot set it; the bit is set when the submission is staged. Passing these checks does not promise
 * acceptance, because the bot repository applies the final decision.
 */
object BotValidator {
    private val rules get() = ValidationRules.current

    fun validate(directory: Path): ValidationReport =
        when (val inspection = BotProjects.inspect(directory)) {
            is Inspection.NotAProject -> ValidationReport(
                listOf(Finding(inspection.code, inspection.file, inspection.reason, inspection.reason))
            )
            is Inspection.Found -> validate(inspection.project)
        }

    private fun validate(project: BotProject): ValidationReport {
        val findings = mutableListOf<Finding>()
        val notes = mutableListOf<String>()
        checkMetadata(project, findings)
        if (project.isTeam) {
            checkTeam(project, findings)
        } else {
            checkIndividual(project, findings, notes)
        }
        return ValidationReport(findings, notes)
    }

    private fun checkMetadata(project: BotProject, findings: MutableList<Finding>) {
        val file = project.configFile
        for (field in listOf("name", "version", "authors", "license")) {
            if (isMissing(project.config[field])) {
                findings += Finding("MISSING_FIELD", file, "$file has no `$field`.", "Add `$field` to $file.")
            }
        }
        val name = project.name
        if (name != null && name != project.folderName) {
            findings += Finding(
                "NAME_MISMATCH", file, "The folder is named ${project.folderName} but the bot's name is $name.",
                "Rename the folder to $name, or change `name` in $file to ${project.folderName}.",
            )
        }
        val authors = project.config["authors"]
        if (!isMissing(authors) && !(authors is JsonArray && authors.all { it is JsonPrimitive && it.isString && it.content.isNotEmpty() })) {
            findings += Finding("AUTHORS_INVALID", file, "`authors` must be a list of names.", "Write `authors` in $file as a list of display names.")
        }
        val license = project.license
        if (license != null && license.isNotEmpty() && license !in rules.allowedLicenses) {
            findings += Finding(
                "LICENSE_NOT_ALLOWED", file, "The license $license is not accepted by the Rumble.",
                "Use one of ${rules.allowedLicenses.joinToString(", ")} as `license` in $file.",
            )
        }
    }

    private fun checkTeam(project: BotProject, findings: MutableList<Finding>) {
        val file = project.configFile
        val members = project.config["teamMembers"]
        val valid = members is JsonArray && members.size == 2 && members.all { it is JsonPrimitive && it.isString && it.content.isNotEmpty() }
        if (!valid) {
            findings += Finding(
                "TEAM_MEMBERS_INVALID", file, "`teamMembers` must list exactly two members.",
                "List exactly two members in $file, each written as `<name> <version>`.",
            )
        }
        for (extra in files(project.directory).filter { it != file }) {
            findings += Finding(
                "TEAM_HAS_EXTRA_FILES", extra, "A team folder may hold only $file, but it also holds $extra.",
                "Remove $extra from the team folder.",
            )
        }
    }

    private fun checkIndividual(project: BotProject, findings: MutableList<Finding>, notes: MutableList<String>) {
        val file = project.configFile
        val declared = project.platform
        val language: String? = when {
            declared == null || declared.isEmpty() -> {
                findings += Finding("PLATFORM_MISSING", file, "$file has no `platform`.", "Add `platform` to $file: one of ${platformNames()}.")
                languageBySource(project.directory)
            }
            project.language == null -> {
                findings += Finding(
                    "PLATFORM_UNKNOWN", file, "The platform $declared is not one of the official languages.",
                    "Set `platform` in $file to one of ${platformNames()}.",
                )
                languageBySource(project.directory)
            }
            else -> project.language
        }
        val everything = files(project.directory)

        if (language != null) {
            checkLaunchers(project, findings, notes)
            checkSources(project, language, findings)
            if (language == "typescript") checkTypescript(project.directory)?.let { findings += it }
        }
        checkContent(project, everything, language, findings)
    }

    private fun checkLaunchers(project: BotProject, findings: MutableList<Finding>, notes: MutableList<String>) {
        for (suffix in rules.launcherSuffixes) {
            val launcher = project.folderName + suffix
            if (!Files.isRegularFile(project.directory.resolve(launcher))) {
                findings += Finding(
                    "MISSING_LAUNCHER", launcher, "The launcher script $launcher is missing.",
                    "Add $launcher to the folder. The starter project for your language has an example.",
                )
            } else if (suffix == ".sh") {
                notes += "$launcher will be marked executable when the submission is staged. You do not need to set that."
            }
        }
    }

    private fun checkSources(project: BotProject, language: String, findings: MutableList<Finding>) {
        val rule = rules.platforms.getValue(language)
        val sources = files(project.directory).filter { it.endsWith(rule.extension) }
        if (sources.isEmpty()) {
            findings += Finding(
                "NO_SOURCE", project.folderName, "The folder holds no ${rule.extension} source file.",
                "Add your bot's ${rule.extension} source file to the folder.",
            )
            return
        }
        val text = sources.joinToString("\n") { readText(project.directory.resolve(it)) }.lowercase()
        if (rule.apiToken.lowercase() !in text) {
            findings += Finding(
                "NO_API_REFERENCE", sources.first(), "No source file refers to the official Tank Royale ${rule.platform} Bot API.",
                "Make your bot use the official Bot API for ${rule.platform}, as the starter project does.",
            )
        }
    }

    private fun checkContent(project: BotProject, everything: List<String>, language: String?, findings: MutableList<Finding>) {
        val directories = everything.flatMap { path -> path.split('/').dropLast(1) }.toSortedSet()
        for (generated in directories.filter { it in rules.forbiddenDirectories }) {
            findings += Finding(
                "GENERATED_DIRECTORY", generated, "The folder $generated holds generated or downloaded files.",
                "Delete every folder named $generated from your bot. They are rebuilt when the bot runs.",
            )
        }
        for (path in everything) {
            val suffix = path.substringAfterLast('.', "").let { if (it.isEmpty()) "" else ".${it.lowercase()}" }
            if ("__pycache__" in path.split('/')) continue
            if (suffix in rules.forbiddenSuffixes) {
                findings += Finding(
                    "FORBIDDEN_FILE", path, "$path is a binary or archive file, which the Rumble does not accept.",
                    "Remove $path. Submit source files only.",
                )
            }
        }
        if (language != null) {
            val extension = rules.platforms.getValue(language).extension
            val text = everything.filter { it.endsWith(extension) }.joinToString("\n") { readText(project.directory.resolve(it)) }.lowercase()
            for (token in rules.forbiddenTokens.filter { it in text }) {
                findings += Finding(
                    "RESTRICTED_CONSTRUCT", project.folderName, "The source uses `$token`, which is sent to a moderator for review.",
                    "Remove `$token` from your source to avoid a manual review, or expect the moderators to check it.",
                )
            }
        }
    }

    private fun checkTypescript(directory: Path): Finding? {
        val allowed = setOf(rules.typescriptApi) + rules.typescriptRuntimePeers
        val api = rules.typescriptApi
        fun problem(code: String, file: String, message: String, fix: String) = Finding(code, file, message, fix)
        val sections = listOf("dependencies", "devDependencies", "optionalDependencies", "peerDependencies")

        val manifest = readObject(directory.resolve("package.json"))
            ?: return problem("TS_PACKAGE_UNREADABLE", "package.json", "package.json is missing or is not a JSON object.", "Add the starter project's package.json.")
        for (section in sections) {
            val value = manifest[section] ?: continue
            if (value !is JsonObject) {
                return problem("TS_SECTION_INVALID", "package.json", "`$section` in package.json must be an object.", "Write `$section` as an object of package names and versions.")
            }
            for ((name, version) in value) {
                if (name !in allowed) {
                    return problem("TS_UNRELATED_DEPENDENCY", "package.json", "The dependency `$name` is not allowed.", "Remove `$name` from package.json. Only $api and its runtime peer are accepted.")
                }
                val text = (version as? JsonPrimitive)?.takeIf { it.isString }?.content
                if (text == null || listOf(":", "/", "\\").any { it in text }) {
                    return problem("TS_NOT_REGISTRY_VERSION", "package.json", "`$name` must use a registry version.", "Set `$name` in package.json to a plain version number, not a path, URL or alias.")
                }
            }
        }
        val dependencies = manifest["dependencies"] as? JsonObject ?: JsonObject(emptyMap())
        if (api !in dependencies) {
            return problem("TS_API_MISSING", "package.json", "package.json does not depend on $api.", "Add $api to `dependencies` in package.json.")
        }
        val lock = readObject(directory.resolve("package-lock.json"))
            ?: return problem("TS_LOCK_UNREADABLE", "package-lock.json", "package-lock.json is missing or is not a JSON object.", "Add the starter project's package-lock.json, or regenerate it with npm.")
        val packages = lock["packages"] as? JsonObject
        val root = packages?.get("") as? JsonObject
        if (packages == null || root == null) {
            return problem("TS_LOCK_INVALID", "package-lock.json", "package-lock.json has no npm package data.", "Regenerate package-lock.json with npm.")
        }
        for (section in sections) {
            if ((root[section] ?: JsonObject(emptyMap())) != (manifest[section] ?: JsonObject(emptyMap()))) {
                return problem("TS_LOCK_MISMATCH", "package-lock.json", "package-lock.json does not match package.json for `$section`.", "Regenerate package-lock.json from package.json with npm.")
            }
        }
        val peers = (packages["node_modules/$api"] as? JsonObject)?.get("peerDependencies")
        if (peers != null && peers !is JsonObject) {
            return problem("TS_PEERS_INVALID", "package-lock.json", "The locked peer dependencies of $api are not an object.", "Regenerate package-lock.json with npm.")
        }
        for (peer in rules.typescriptRuntimePeers) {
            if (peers == null || peer !in (peers as JsonObject) || peer !in dependencies || "node_modules/$peer" !in packages) {
                return problem("TS_PEER_MISSING", "package.json", "$api needs its runtime peer `$peer` in package.json and package-lock.json.", "Add `$peer` to package.json and regenerate package-lock.json.")
            }
        }
        for ((path, entry) in packages) {
            if (path.isEmpty()) continue
            val name = path.substringAfterLast("node_modules/")
            if (name !in allowed) {
                return problem("TS_UNRELATED_LOCKED", "package-lock.json", "The locked package `$name` is not allowed.", "Remove `$name` and regenerate package-lock.json.")
            }
            val locked = entry as? JsonObject
            val resolved = (locked?.get("resolved") as? JsonPrimitive)?.takeIf { it.isString }?.content
            if (locked == null || resolved == null) {
                return problem("TS_LOCK_NO_RESOLUTION", "package-lock.json", "The locked `$name` has no registry address.", "Regenerate package-lock.json with npm.")
            }
            val version = (locked["version"] as? JsonPrimitive)?.takeIf { it.isString }?.content
            if (version == null || !Regex("""\d+\.\d+\.\d+(?:-[A-Za-z0-9.-]+)?""").matches(version)) {
                return problem("TS_LOCK_VERSION", "package-lock.json", "The locked `$name` has no concrete version.", "Regenerate package-lock.json with npm.")
            }
            val expected = "https://registry.npmjs.org/$name/-/${name.substringAfterLast('/')}-$version.tgz"
            val integrity = (locked["integrity"] as? JsonPrimitive)?.takeIf { it.isString }?.content
            if (URLDecoder.decode(resolved.replace("+", "%2B"), Charsets.UTF_8) != expected || integrity.isNullOrEmpty()) {
                return problem("TS_LOCK_REGISTRY", "package-lock.json", "The locked `$name` does not come from the official npm registry with an integrity check.", "Regenerate package-lock.json with npm from the official registry.")
            }
        }
        return null
    }

    private fun languageBySource(directory: Path): String? {
        val present = rules.platforms.filter { (_, rule) -> files(directory).any { it.endsWith(rule.extension) } }.keys
        return present.singleOrNull()
    }

    private fun platformNames() = rules.platforms.values.joinToString(", ") { it.platform }

    private fun isMissing(element: JsonElement?): Boolean = when (element) {
        null, JsonNull -> true
        is JsonPrimitive -> (element.isString && element.content.isEmpty()) ||
            (!element.isString && (element.content == "false" || element.content == "0"))
        is JsonArray -> element.isEmpty()
        is JsonObject -> element.isEmpty()
    }

    private fun readObject(file: Path): JsonObject? =
        try {
            Json.parseToJsonElement(Files.readString(file)) as? JsonObject
        } catch (e: Exception) {
            null
        }

    private fun readText(file: Path): String = String(Files.readAllBytes(file), Charsets.UTF_8)

    /** Slash-separated paths of all files below [directory], sorted. */
    private fun files(directory: Path): List<String> =
        Files.walk(directory).use { paths ->
            paths.filter { Files.isRegularFile(it) }.toList().map { directory.relativize(it).joinToString("/") }.sorted()
        }
}
