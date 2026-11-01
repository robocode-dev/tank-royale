package dev.robocode.tankroyale.botproject

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFilePermissions

/** One file of a staged copy. */
class StagedFile(
    /** Slash-separated path inside the bot's folder. */
    val path: String,
    val size: Long,
    /** True for the Unix launcher, which the bot repository requires to be committed executable. */
    val executable: Boolean,
    /** True when the file's Windows line endings were changed to Unix line endings in the copy. */
    val lineEndingsChanged: Boolean,
)

/** Something in the project that was left out of the copy, grouped by folder, with the reason. */
class NotIncluded(val path: String, val fileCount: Int, val reason: String)

/** A copy of a bot project that passes the checks, described exactly. */
class StagedSubmission(
    val name: String,
    val version: String?,
    /** `java`, `csharp`, `python` or `typescript`: the folder below `bots/` that the bot goes in. */
    val language: String,
    val license: String?,
    val authors: List<String>,
    val isTeam: Boolean,
    /** The staging folder; the bot is at `bots/<language>/<name>` below it. */
    val stagingFolder: Path,
    val botFolder: Path,
    val files: List<StagedFile>,
    val notIncluded: List<NotIncluded>,
    /** The hash the bot repository will record for this folder. */
    val treeHash: String,
)

sealed interface StagingResult {
    data class Staged(val submission: StagedSubmission) : StagingResult

    /** The project, or its copy, did not pass the checks; no usable copy was left. */
    data class Rejected(val findings: List<Finding>) : StagingResult

    /** Staging could not start safely, for example because of the staging folder; nothing was changed. */
    data class Refused(val message: String) : StagingResult
}

/**
 * Prepares a bot project for submission as a separate copy, laid out like the bot repository. The project is only
 * read. The copy holds only what a bot is made of: its configuration file and launcher scripts, the source files of
 * its language, the files its language needs to restore dependencies, and an optional license file. Everything else
 * is listed as not included. Included files get Unix line endings in the copy, which is what the bot repository
 * holds, and the Unix launcher is recorded as executable. The copy is then checked with [BotValidator].
 */
object BotStaging {
    const val MARKER = ".rumble-staging"

    private val rules get() = ValidationRules.current

    /** Folders that never hold a bot's own files, whatever they contain. */
    private val SKIPPED_FOLDERS = setOf(".git", ".svn", ".hg", ".idea", ".vs", ".vscode", "__pycache__") +
        ValidationRules.current.forbiddenDirectories

    private val DECLARATION_FILES = mapOf(
        "typescript" to setOf("package.json", "package-lock.json"),
        "python" to setOf("requirements.txt"),
    )
    private val LICENSE_FILES = setOf("LICENSE", "LICENSE.txt", "LICENSE.md")

    fun stage(project: Path, stagingFolder: Path, teamLanguage: String = "java"): StagingResult {
        val found = when (val inspection = BotProjects.inspect(project)) {
            is Inspection.NotAProject -> return StagingResult.Rejected(
                listOf(Finding(inspection.code, inspection.file, inspection.reason, inspection.reason))
            )
            is Inspection.Found -> inspection.project
        }
        val language = if (found.isTeam) teamLanguage else found.language
        if (language == null || language !in rules.platforms) {
            val file = found.configFile
            val finding = if (found.platform.isNullOrEmpty()) {
                Finding("PLATFORM_MISSING", file, "$file has no `platform`.", "Add `platform` to $file.")
            } else {
                Finding("PLATFORM_UNKNOWN", file, "The platform ${found.platform} is not one of the official languages.", "Set `platform` in $file to an official language.")
            }
            return StagingResult.Rejected(listOf(finding))
        }

        val source = project.toRealPath()
        val target = stagingFolder.toAbsolutePath().normalize()
        folderProblem(source, target)?.let { return StagingResult.Refused(it) }

        try {
            prepareStagingFolder(target)
            val botFolder = target.resolve("bots").resolve(language).resolve(found.folderName)
            val notIncluded = mutableListOf<NotIncluded>()
            val files = copyIncluded(source, botFolder, found, language, notIncluded)

            val report = BotValidator.validate(botFolder)
            if (!report.passes) {
                deleteContents(target)
                return StagingResult.Rejected(report.findings)
            }
            return StagingResult.Staged(
                StagedSubmission(
                    name = found.name ?: found.folderName,
                    version = found.version,
                    language = language,
                    license = found.license,
                    authors = found.authors,
                    isTeam = found.isTeam,
                    stagingFolder = target,
                    botFolder = botFolder,
                    files = files,
                    notIncluded = notIncluded.sortedBy { it.path },
                    treeHash = TreeHash.ofDirectory(botFolder),
                )
            )
        } catch (e: IOException) {
            return StagingResult.Refused("The bot could not be copied to the staging folder: ${e.javaClass.simpleName}.")
        }
    }

    private fun folderProblem(source: Path, target: Path): String? = when {
        target == source || target.startsWith(source) -> "The staging folder must not be inside the project folder."
        source.startsWith(target) -> "The staging folder must not contain the project folder."
        Files.exists(target) && !Files.isDirectory(target) -> "The staging folder is a file, not a folder."
        Files.isDirectory(target) && !Files.exists(target.resolve(MARKER)) && Files.list(target).use { it.findAny().isPresent } ->
            "The staging folder is not empty and was not made by staging, so it was left alone. Choose an empty folder."
        else -> null
    }

    private fun prepareStagingFolder(target: Path) {
        Files.createDirectories(target)
        deleteContents(target)
        Files.writeString(target.resolve(MARKER), "Made by Tank Royale staging. This folder may be emptied and reused.\n")
    }

    private fun deleteContents(folder: Path) {
        if (!Files.isDirectory(folder)) return
        Files.list(folder).use { entries ->
            entries.filter { it.fileName.toString() != MARKER }.forEach { deleteRecursively(it) }
        }
    }

    private fun deleteRecursively(path: Path) {
        if (Files.isDirectory(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
            Files.list(path).use { children -> children.forEach { deleteRecursively(it) } }
        }
        Files.deleteIfExists(path)
    }

    private fun copyIncluded(
        source: Path,
        botFolder: Path,
        project: BotProject,
        language: String,
        notIncluded: MutableList<NotIncluded>,
    ): List<StagedFile> {
        val name = project.folderName
        val extension = rules.platforms.getValue(language).extension
        val rootNames: Set<String> =
            if (project.isTeam) setOf("$name.json")
            else setOf("$name.json") + rules.launcherSuffixes.map { name + it }
        val declarations = DECLARATION_FILES[language].orEmpty()

        fun included(relative: String): Boolean {
            val atRoot = !relative.contains('/')
            val file = relative.substringAfterLast('/')
            if (atRoot && file in rootNames) return true
            if (project.isTeam) return false
            if (file.endsWith(extension)) return true
            if (!atRoot) return false
            return file in declarations || file in LICENSE_FILES || (language == "csharp" && file.endsWith(".csproj"))
        }

        val chosen = mutableListOf<String>()
        val left = linkedMapOf<String, Pair<Int, String>>() // path -> count, reason
        fun leave(path: String, count: Int, reason: String) {
            val old = left[path]
            left[path] = ((old?.first ?: 0) + count) to reason
        }

        Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (dir != source && dir.fileName.toString() in SKIPPED_FOLDERS) {
                    leave(source.relativize(dir).joinToString("/") + "/", countFiles(dir), "build, dependency, version-control, editor or cache folder")
                    return FileVisitResult.SKIP_SUBTREE
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val relative = source.relativize(file).joinToString("/")
                when {
                    attrs.isSymbolicLink || attrs.isOther -> leave(relative, 1, "symbolic link or special file")
                    included(relative) -> chosen += relative
                    else -> {
                        val parent = relative.substringBeforeLast('/', "")
                        val key = if (parent.isEmpty()) relative else "$parent/"
                        leave(key, 1, "not a source, launcher, dependency declaration or license file")
                    }
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                leave(source.relativize(file).joinToString("/"), 1, "could not be read")
                return FileVisitResult.CONTINUE
            }
        })

        left.forEach { (path, value) -> notIncluded += NotIncluded(path, value.first, value.second) }

        Files.createDirectories(botFolder)
        return chosen.sorted().map { relative ->
            val original = Files.readAllBytes(source.resolve(relative))
            val converted = toUnixLineEndings(original)
            val target = botFolder.resolve(relative)
            Files.createDirectories(target.parent)
            Files.write(target, converted)
            val executable = relative == "$name.sh"
            if (executable) makeExecutable(target)
            StagedFile(relative, converted.size.toLong(), executable, converted.size != original.size)
        }
    }

    private fun countFiles(folder: Path): Int =
        Files.walk(folder).use { paths -> paths.filter { !Files.isDirectory(it, java.nio.file.LinkOption.NOFOLLOW_LINKS) }.count().toInt() }

    /** Changes each carriage return followed by a line feed into a line feed. A file with a zero byte is left as it is. */
    private fun toUnixLineEndings(bytes: ByteArray): ByteArray {
        if (bytes.any { it == 0.toByte() }) return bytes
        val out = java.io.ByteArrayOutputStream(bytes.size)
        var i = 0
        while (i < bytes.size) {
            if (bytes[i] == '\r'.code.toByte() && i + 1 < bytes.size && bytes[i + 1] == '\n'.code.toByte()) {
                i++
            }
            out.write(bytes[i].toInt())
            i++
        }
        return out.toByteArray()
    }

    private fun makeExecutable(file: Path) {
        if ("posix" in file.fileSystem.supportedFileAttributeViews()) {
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rwxr-xr-x"))
        }
    }
}
