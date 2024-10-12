import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

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
                "docfx_project/_site",
                "docfx_project/api",
                "docfx_project/obj",
                "Robocode.TankRoyale.BotApi/obj",
                "Robocode.TankRoyale.BotApi/bin",
                "Robocode.TankRoyale.BotApi.Tests/obj",
                "Robocode.TankRoyale.BotApi.Tests/bin",
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

    assemble {
        dependsOn(":schema:dotnet:build", prepareNugetDocs)

        doLast {
            exec {
                workingDir("Robocode.TankRoyale.BotApi")
                commandLine("dotnet", "build", "--configuration", "Release", "-p:Version=$version")
            }
        }
    }

    register("test") {
        doLast {
            exec {
                workingDir("Robocode.TankRoyale.BotApi.Tests")
                commandLine("dotnet", "test")
            }
        }
    }

    val docfx by registering {
        doLast {
            exec {
                workingDir("docfx_project")
                commandLine("docfx", "metadata") // build /api before building the _site
            }
            exec {
                workingDir("docfx_project")
                delete("_site", "api", "obj")
                commandLine("docfx", "build")    // build /_site
            }
        }
    }

    register<Copy>("uploadDocs") {
        dependsOn(clean, docfx)

        val dotnetApiDir = "../../docs/api/dotnet"

        delete(dotnetApiDir)
        mkdir(dotnetApiDir)

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from("docfx_project/_site")
        into(dotnetApiDir)
    }

    register("pushLocal") {
        dependsOn(build, prepareNugetDocs)

        doLast {
            val userhome = System.getenv("USERPROFILE") ?: System.getenv("HOME")
            println("$userhome/.nuget/packages/${artifactName.toLowerCaseAsciiOnly()}/$version")
            delete("$userhome/.nuget/packages/${artifactName.toLowerCaseAsciiOnly()}/$version")
            exec {
                workingDir("Robocode.TankRoyale.BotApi/bin/Release")
                commandLine("dotnet", "nuget", "push", "$artifactName.$version.nupkg", "--source", "$userhome/.nuget/packages")
            }
        }
    }
}