import build.csproj.generateBotCsprojFile
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

description = "Robocode Tank Royale sample bots for C#"

version = libs.versions.tankroyale.get()

plugins {
    base // for the clean and build task
}

tasks {
    val archiveDir = layout.buildDirectory.dir("archive")
    val archiveDirPath = archiveDir.get().asFile.toPath()
    val libDirPath = archiveDirPath.resolve("lib")

    fun Path.botName() = fileName.toString()

    // Shared helpers provided by parent sample-bots/build.gradle.kts
    @Suppress("UNCHECKED_CAST")
    val isBotProjectDir = rootProject.extra["isBotProjectDir"] as (Path) -> Boolean
    @Suppress("UNCHECKED_CAST")
    val copyBotFiles = rootProject.extra["copyBotFiles"] as (Path, Path) -> Unit

    fun hasBase(botDir: Path): Boolean {
        val botName = botDir.botName()
        val jsonPath = botDir.resolve("$botName.json")
        if (!exists(jsonPath)) return false
        val content = jsonPath.toFile().readText()
        return content.contains("\"base\"")
    }

    fun generateShellScript(): String = """
        #!/bin/sh
        if [ ! -d "bin/Release" ]; then
          dotnet build -c Release
        fi
        dotnet run -c Release --no-build
        """.trimIndent()

    fun generateBatchScript(): String = """
        if not exist bin\Release\ (
            dotnet build -c Release
        )
        dotnet run -c Release --no-build
        """.trimIndent()

    fun generateNuGetConfig(): String = """
        <?xml version="1.0" encoding="utf-8"?>
        <configuration>
          <packageSources>
            <add key="local-lib" value="../lib" />
            <add key="nuget.org" value="https://api.nuget.org/v3/index.json" protocolVersion="3" />
          </packageSources>
        </configuration>
        """.trimIndent()

    fun createScriptFile(projectDir: Path, botArchivePath: Path, fileExt: String, newLine: String) {
        val botName = projectDir.botName()
        val scriptContent = when (fileExt) {
            "sh" -> generateShellScript()
            "cmd" -> generateBatchScript()
            else -> throw IllegalArgumentException("Unsupported file extension: $fileExt")
        }

        val file = botArchivePath.resolve("$botName.$fileExt").toFile()
        file.writeText(scriptContent.replace("\n", newLine))
    }

    fun isTeam(botDir: Path): Boolean {
        val botName = botDir.botName()
        val jsonPath = botDir.resolve("$botName.json")
        if (!exists(jsonPath)) return false
        val content = jsonPath.toFile().readText()
        return content.contains("\"teamMembers\"")
    }

    fun prepareBotFiles() {
        list(project.projectDir.toPath()).forEach { botDir ->
            if (isDirectory(botDir) && isBotProjectDir(botDir)) {
                val botName = botDir.botName()
                val botArchivePath: Path = archiveDirPath.resolve(botName)

                mkdir(botArchivePath)
                copyBotFiles(botDir, botArchivePath)

                if (isTeam(botDir)) {
                    createScriptFile(botDir, botArchivePath, "cmd", "\r\n")
                    createScriptFile(botDir, botArchivePath, "sh", "\n")
                } else {
                    generateBotCsprojFile(botArchivePath.resolve("$botName.csproj"), botName, "${project.version}")

                    // Add NuGet.Config for scriptless bots to find local .nupkg
                    val configPath = botArchivePath.resolve("NuGet.Config")
                    configPath.toFile().writeText(generateNuGetConfig())
                }
            }
        }
    }

    fun copyNugetPackage() {
        val nupkgSourceDir = rootProject.projectDir.toPath().resolve("bot-api/dotnet/api/bin/Release")
        if (!exists(nupkgSourceDir)) {
            throw GradleException("NuGet package source directory not found: $nupkgSourceDir. Run ':bot-api:dotnet:build' first.")
        }
        
        mkdir(libDirPath)
        list(nupkgSourceDir).forEach { nupkgFile ->
            if (nupkgFile.toString().endsWith(".nupkg")) {
                copy(nupkgFile, libDirPath.resolve(nupkgFile.fileName), REPLACE_EXISTING)
            }
        }
    }

    val prepareArchive by registering {
        dependsOn(":bot-api:dotnet:build")
        doLast {
            prepareBotFiles()
            copyNugetPackage()
        }
    }

    named("build") {
        dependsOn(prepareArchive)
    }
}
