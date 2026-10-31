package dev.robocode.tankroyale.botproject

import java.nio.file.Files
import java.nio.file.Path

/** Builds small bot folders for the tests. A folder made here passes validation until a test breaks it. */
object Fixtures {
    val languages = listOf("java", "csharp", "python", "typescript")

    private val platform = mapOf("java" to "JVM", "csharp" to "C#", "python" to "Python", "typescript" to "TypeScript")
    private val extension = mapOf("java" to ".java", "csharp" to ".cs", "python" to ".py", "typescript" to ".ts")
    private val apiLine = mapOf(
        "java" to "import dev.robocode.tankroyale.botapi.Bot;",
        "csharp" to "using Robocode.TankRoyale.BotApi;",
        "python" to "from robocode_tank_royale.bot_api import Bot",
        "typescript" to "import { Bot } from \"@robocode.dev/tank-royale-bot-api\";",
    )

    const val TS_API = "@robocode.dev/tank-royale-bot-api"

    fun config(name: String, language: String, extra: String = ""): String = """
        {
          "name": "$name", "version": "1.0.0", "authors": ["A. Author"], "license": "MIT",
          "description": "A test bot", "platform": "${platform.getValue(language)}"$extra
        }
    """.trimIndent()

    fun write(file: Path, text: String) {
        Files.createDirectories(file.parent)
        Files.writeString(file, text)
    }

    /** A valid individual bot named [name] in [language], below [parent]; returns its folder. */
    fun bot(parent: Path, language: String, name: String = "MyBot"): Path {
        val dir = parent.resolve(name)
        write(dir.resolve("$name.json"), config(name, language))
        write(dir.resolve("$name.sh"), "#!/bin/sh\n")
        write(dir.resolve("$name.cmd"), "@echo off\r\n")
        write(dir.resolve("src").resolve(name + extension.getValue(language)), apiLine.getValue(language) + "\nclass $name\n")
        if (language == "typescript") {
            write(dir.resolve("package.json"), packageJson())
            write(dir.resolve("package-lock.json"), packageLock())
        }
        return dir
    }

    /** A valid team folder named [name]. */
    fun team(parent: Path, name: String = "MyTeam"): Path {
        val dir = parent.resolve(name)
        write(
            dir.resolve("$name.json"),
            """{ "name": "$name", "version": "1.0.0", "authors": ["A. Author"], "license": "MIT",
                "teamMembers": ["MyBot 1.0.0", "MyBot 1.0.0"] }""",
        )
        return dir
    }

    fun packageJson(extraDependency: String = "", wsVersion: String = "8.18.0") = """
        { "name": "bot", "version": "1.0.0", "private": true,
          "dependencies": { "$TS_API": "1.3.1", "ws": "$wsVersion"$extraDependency } }
    """.trimIndent()

    fun packageLock(wsResolved: String = "https://registry.npmjs.org/ws/-/ws-8.18.0.tgz", withWsPeer: Boolean = true, wsVersion: String = "8.18.0") = """
        { "lockfileVersion": 3, "packages": {
            "": { "dependencies": { "$TS_API": "1.3.1", "ws": "$wsVersion" } },
            "node_modules/$TS_API": {
              "version": "1.3.1",
              "resolved": "https://registry.npmjs.org/$TS_API/-/tank-royale-bot-api-1.3.1.tgz",
              "integrity": "sha512-abc"${if (withWsPeer) ", \"peerDependencies\": { \"ws\": \"^8.0.0\" }" else ""}
            },
            "node_modules/ws": { "version": "8.18.0", "resolved": "$wsResolved", "integrity": "sha512-def" }
        } }
    """.trimIndent()

    fun codes(report: ValidationReport): List<String> = report.findings.map { it.code }
}
