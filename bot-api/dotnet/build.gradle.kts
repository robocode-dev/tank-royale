import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly


val artifactName = "Robocode.TankRoyale.BotApi"
version = libs.versions.tankroyale.get()

val docfxArchiveFilename = "docfx.zip"

val buildArchiveDirProvider: Provider<Directory> = layout.buildDirectory.dir("archive")
val buildArchivePath = buildArchiveDirProvider.get().toString()

val apiPath: String by rootProject.extra
val dotnetApiPath = "$apiPath/dotnet"


@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.itiviti.dotnet)
}

dotnet {
    solution = "bot-api/bot-api.csproj"

    projectName = artifactName

    build {
        version = libs.versions.tankroyale.get()
        packageVersion = version
    }

    test {
        solution = "bot-api.tests/bot-api.tests.csproj"
    }

    nugetPush {
        // api key is set by `nuget setApiKey <key>`
        source = "nuget.org"
    }
}

tasks {
    clean {
        doLast {
            delete(
                "build",
                "bot-api/obj",
                "bot-api/bin",
                "bot-api.tests/obj",
                "bot-api.tests/bin",
                "docfx_project/_site",
                "docfx_project/api",
                "docfx_project/obj"
            )
        }
    }

    val docfx by registering {
        dependsOn(assemble)

        doFirst {
            exec {
                workingDir("docfx_project")
                commandLine("docfx", "metadata") // build /api before building the _site
            }
        }
        doLast {
            exec {
                workingDir("docfx_project")
                commandLine("docfx", "build")    // build /_site
            }
        }
    }
/*
    register<Copy>("uploadDocs") {
        dependsOn(docfx)

        val dotnetApiDir = "../../docs/api/dotnet"

        delete(dotnetApiDir)
        mkdir(dotnetApiDir)

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from("docfx_project/_site")
        into(dotnetApiDir)
    }
*/
    register("pushLocal") {
        dependsOn(build)

        doLast {
            val userprofile = System.getenv("USERPROFILE")
            delete("$userprofile/.nuget/packages/${artifactName.toLowerCaseAsciiOnly()}/$version")
            exec {
                workingDir("bin/Release")
                commandLine("dotnet", "nuget", "push", "$artifactName.$version.nupkg", "-s", "local")
            }
        }
    }
}
