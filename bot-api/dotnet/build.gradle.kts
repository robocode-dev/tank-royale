import org.apache.tools.ant.filters.ReplaceTokens
import kotlin.text.lowercase

description = "Robocode Tank Royale Bot API for .Net"

val artifactName = "Robocode.TankRoyale.BotApi"
version = libs.versions.tankroyale.get()

plugins {
    base
}

tasks {
    named("clean") {
        dependsOn(":bot-api:dotnet:schema:clean")

        doLast {
            delete(
                "build",
                "api/obj",
                "api/bin",
                "test/obj",
                "test/bin",
            )
        }
    }

    val prepareNugetDocs by registering(Copy::class) {
        doFirst {
            delete("docs")
        }
        from("nuget-docs-template") {
            filter<ReplaceTokens>("tokens" to mapOf("VERSION" to version))
        }
        into("docs")
    }

    val buildDotnetBotApi by registering(Exec::class) {
        dependsOn(prepareNugetDocs)
        dependsOn(":bot-api:dotnet:schema:build")

        workingDir("api")
        commandLine("dotnet", "build", "--configuration", "Release", "-p:Version=$version")
    }

    register<Exec>("test") {
        workingDir("test")
        commandLine("dotnet", "test")
    }

    named("build") {
        dependsOn(buildDotnetBotApi)
    }

    val docfxMetadata by registering(Exec::class) {
        dependsOn(":bot-api:dotnet:schema:build")

        workingDir("docfx-project")
        commandLine("docfx", "metadata")
    }

    val docfxClean by register("docfxClean") {
        doLast {
            delete(
                "docfx-project/_site",
                "docfx-project/api",
                "docfx-project/obj",
            )
        }
    }

    val docfxBuild by registering(Exec::class) {
        dependsOn(docfxClean, prepareNugetDocs)

        workingDir("docfx-project")
        commandLine("docfx")
    }

    val docfx by registering {
        dependsOn(docfxMetadata, docfxBuild)
    }

    register<Copy>("copyDotnetApiDocs") {
        dependsOn(docfx)

        val dotnetApiDir = "../../docs/api/dotnet"

        delete(dotnetApiDir)
        mkdir(dotnetApiDir)

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from("docfx-project/_site")
        into(dotnetApiDir)
    }

    // Make sure documentation tasks are not part of the build task
    afterEvaluate {
        tasks.named("build").configure {
            setDependsOn(dependsOn.filterNot {
                it.toString().contains("docfx") || it.toString().contains("copyDotnetApiDocs")
            })
        }
    }

    register<Exec>("pushLocal") {
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