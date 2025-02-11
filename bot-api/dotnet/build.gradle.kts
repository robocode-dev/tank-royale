import org.apache.tools.ant.filters.ReplaceTokens
import kotlin.text.lowercase

description = "Robocode Tank Royale Bot API for .Net"

val artifactName = "Robocode.TankRoyale.BotApi"
version = libs.versions.tankroyale.get()

plugins {
    base
}

tasks {
    clean {
        doLast {
            delete(
                "build",
                "api/obj",
                "api/bin",
                "test/obj",
                "test/bin",
                "docfx_project/_site",
                "docfx_project/api",
                "docfx_project/obj",
            )
        }
    }

    val prepareNugetDocs by registering(Copy::class) {
        delete("docs")
        from("nuget_docs") {
            filter<ReplaceTokens>("tokens" to mapOf("VERSION" to version))
        }
        into("docs")
    }

    val buildDotnetBotApi by registering(Exec::class) {
        dependsOn(prepareNugetDocs)
        dependsOn(":schema:dotnet:build")

        workingDir("api")
        commandLine("dotnet", "build", "--configuration", "Release", "-p:Version=$version")
    }

    val test by registering(Exec::class) {
        workingDir("test")
        commandLine("dotnet", "test")
    }

    build {
        dependsOn(":schema:dotnet:build", buildDotnetBotApi)
    }

    val docfxMetadata by registering(Exec::class) {
        workingDir("docfx_project")
        commandLine("docfx", "metadata")
    }

    val docfxBuild by registering(Exec::class) {
        workingDir("docfx_project")
        delete("_site", "api", "obj")
        commandLine("docfx", "build")
    }

    val docfx by registering {
        dependsOn(docfxMetadata, docfxBuild)
    }

    val uploadDocs by registering(Copy::class) {
        dependsOn(clean, docfx)

        val dotnetApiDir = "../../docs/api/dotnet"

        delete(dotnetApiDir)
        mkdir(dotnetApiDir)

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from("docfx_project/_site")
        into(dotnetApiDir)
    }

    val pushLocal by registering(Exec::class) {
        dependsOn(prepareNugetDocs)

        val userHome = System.getenv("USERPROFILE") ?: System.getenv("HOME")

        doFirst {
            println("$userHome/.nuget/packages/${artifactName.lowercase()}/$version")
            delete("$userHome/.nuget/packages/${artifactName.lowercase()}/$version")
        }

        workingDir("api/bin/Release")
        commandLine("dotnet", "nuget", "push", "$artifactName.$version.nupkg", "--source", "$userHome/.nuget/packages")
    }
}