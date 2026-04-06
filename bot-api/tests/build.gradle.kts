import build.csproj.generateBotCsprojFile
import java.io.PrintWriter
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

description = "Test bots used by runner integration tests to verify event delivery"

plugins {
    base
}

tasks {
    val javaArchiveDir: Path = layout.buildDirectory.dir("archive/java").get().asFile.toPath()
    val csharpArchiveDir: Path = layout.buildDirectory.dir("archive/csharp").get().asFile.toPath()
    val typescriptArchiveDir: Path = layout.buildDirectory.dir("archive/typescript").get().asFile.toPath()
    val botsSourceDir: Path = projectDir.toPath().resolve("bots")

    // --- Java test bot preparation ---

    fun prepareJavaBot(botName: String) {
        val srcDir = botsSourceDir.resolve("java/$botName")
        val dstDir = javaArchiveDir.resolve(botName)
        mkdir(dstDir)
        list(srcDir).forEach { f -> copy(f, dstDir.resolve(f.fileName), REPLACE_EXISTING) }

        fun script(ext: String, nl: String) {
            val javaExe = if (ext == "cmd") "javaw" else "java"
            val file = dstDir.resolve("$botName.$ext").toFile()
            val pw = object : PrintWriter(file) { override fun println() { write(nl) } }
            pw.use {
                if (ext == "sh") it.println("#!/bin/sh")
                it.println("$javaExe -cp ../lib/* $botName.java")
            }
        }
        script("cmd", "\r\n")
        script("sh", "\n")
    }

    val copyBotApiJar by registering(Copy::class) {
        mkdir(javaArchiveDir.resolve("lib"))
        dependsOn(":bot-api:java:jar")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(project(":bot-api:java").file("build/libs/robocode-tankroyale-bot-api-${project.version}.jar"))
        into(javaArchiveDir.resolve("lib").toFile())
    }

    // --- C# test bot preparation ---

    fun prepareCsharpBot(botName: String) {
        val srcDir = botsSourceDir.resolve("csharp/$botName")
        val dstDir = csharpArchiveDir.resolve(botName)
        mkdir(dstDir)
        list(srcDir).forEach { f -> copy(f, dstDir.resolve(f.fileName), REPLACE_EXISTING) }

        generateBotCsprojFile(dstDir.resolve("$botName.csproj"), botName, "${project.version}")

        // nuget.config — path is relative to where the file lands in the archive:
        // bot-api/tests/build/archive/csharp/<botName>/ → 6 levels up to root
        val localBotApiPath = "../../../../../../bot-api/dotnet/api/bin/Release"
        dstDir.resolve("nuget.config").toFile().writeText(
            """<?xml version="1.0" encoding="utf-8"?>
<configuration>
  <packageSources>
    <add key="local-bot-api" value="$localBotApiPath"/>
    <add key="nuget.org" value="https://api.nuget.org/v3/index.json"/>
  </packageSources>
</configuration>
""")

        // Launch scripts — use -c Release to avoid Windows Defender DLL-lock on Debug builds
        dstDir.resolve("$botName.sh").toFile().writeText(
            "#!/bin/sh\nif [ ! -d \"bin/Release\" ]; then\n  dotnet build -c Release\nfi\ndotnet run -c Release --no-build\n")
        dstDir.resolve("$botName.cmd").toFile().writeText(
            "if not exist bin\\Release\\ (\r\n    dotnet build -c Release\r\n)\r\ndotnet run -c Release --no-build\r\n")
    }

    // --- TypeScript test bot preparation ---

    fun createTsShellScript(botName: String): String = """
        #!/bin/sh
        set -e
        cd -- "${'$'}(dirname -- "${'$'}0")"
        ../deps/install-dependencies.sh
        export NODE_OPTIONS="--disable-warning=ExperimentalWarning"
        exec "../node_modules/.bin/tsx" "$botName.ts"
        """.trimIndent()

    fun createTsBatchScript(botName: String): String = """
        @echo off
        cd /d "%~dp0"
        call ..\deps\install-dependencies.cmd
        set NODE_OPTIONS=--disable-warning=ExperimentalWarning
        ..\node_modules\.bin\tsx $botName.ts
        """.trimIndent()

    fun writeTsLaunchScript(botDir: Path, botName: String, ext: String, lineEnding: String) {
        val content = if (ext == "sh") createTsShellScript(botName) else createTsBatchScript(botName)
        val file = botDir.resolve("$botName.$ext").toFile()
        val pw = object : PrintWriter(file) { override fun println() { write(lineEnding) } }
        pw.use { it.print(content) }
        if (ext == "sh") file.setExecutable(true, false)
    }

    fun prepareTypescriptBot(botName: String) {
        val srcDir = botsSourceDir.resolve("typescript/$botName")
        val dstDir = typescriptArchiveDir.resolve(botName)
        createDirectories(dstDir)
        list(srcDir).forEach { f -> copy(f, dstDir.resolve(f.fileName), REPLACE_EXISTING) }
        writeTsLaunchScript(dstDir, botName, "sh", "\n")
        writeTsLaunchScript(dstDir, botName, "cmd", "\r\n")
    }

    fun prepareTsDependencies() {
        val depsDir = typescriptArchiveDir.resolve("deps")
        createDirectories(depsDir)

        val assetsDir = rootProject.projectDir.toPath().resolve("sample-bots/typescript/assets")
        copy(assetsDir.resolve("install-dependencies.sh"), depsDir.resolve("install-dependencies.sh"), REPLACE_EXISTING)
        copy(assetsDir.resolve("install-dependencies.cmd"), depsDir.resolve("install-dependencies.cmd"), REPLACE_EXISTING)

        val botApiDir = rootProject.projectDir.toPath().resolve("bot-api/typescript")
        val tarball = list(botApiDir).use { paths ->
            paths.filter { it.fileName.toString().endsWith(".tgz") }
                .findFirst()
                .orElseThrow { GradleException("No .tgz tarball found in $botApiDir. Run :bot-api:typescript:npmPack first.") }
        }
        val tarballName = tarball.fileName.toString()
        copy(tarball, depsDir.resolve(tarballName), REPLACE_EXISTING)

        typescriptArchiveDir.resolve("package.json").toFile().writeText(
            """{
  "private": true,
  "type": "module",
  "dependencies": {
    "@robocode.dev/tank-royale-bot-api": "file:./deps/$tarballName",
    "tsx": "^4.19.2",
    "ws": "^8.18.1"
  }
}
""")
    }

    val prepareArchive by registering {
        dependsOn(copyBotApiJar, ":bot-api:typescript:npmPack")
        doLast {
            mkdir(javaArchiveDir)
            mkdir(csharpArchiveDir)
            mkdir(typescriptArchiveDir)
            prepareJavaBot("WonRoundCounterJava")
            prepareCsharpBot("WonRoundCounterCSharp")
            prepareTsDependencies()
            prepareTypescriptBot("WonRoundCounterTs")
        }
    }

    named("build") {
        dependsOn(prepareArchive)
    }
}
