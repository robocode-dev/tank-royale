import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

apply(from = "../../groovy.gradle")

val artifactName = "Robocode.TankRoyale.BotApi"
version = "0.9.11"


val buildArchiveDirProvider: Provider<Directory> = layout.buildDirectory.dir("archive")
val buildArchivePath = buildArchiveDirProvider.get().toString()

val apiPath: String by rootProject.extra
val dotnetApiPath = "$apiPath/dotnet"
val oldDotnetApiPath = dotnetApiPath + "_old"
val newDotnetApiPath = dotnetApiPath + "_new"


plugins {
    alias(libs.plugins.itiviti.dotnet)
    alias(libs.plugins.hidetake.ssh)
}

dotnet {
    projectName = artifactName

    build {
        version = project.version as String
        packageVersion = version
    }

    nugetPush {
        apiKey = ""
        source = ""
    }
}

tasks {
    val docfx = register("docfx") {
        dependsOn(clean, build)

        doLast {
            delete("docfx_project/_site")
            delete("docfx_project/obj")

            exec {
                workingDir("docfx_project")
                commandLine("docfx", "build")
            }
        }
    }

    val zip = register<Zip>("zip") {
        dependsOn(docfx)

        archiveFileName.set("docfx.zip")
        destinationDirectory.set(buildArchiveDirProvider)

        from(file("docfx_project/_site"))
    }

    register("uploadDocs") {
        dependsOn(zip)

        doLast {
            ssh.run(delegateClosureOf<RunHandler> {
                session(remotes["sshServer"], delegateClosureOf<SessionHandler> {
                    print("Uploading docs...")

                    val filename = "docfx.zip"

                    put(hashMapOf("from" to "${buildArchivePath}/$filename", "into" to "tmp"))

                    execute("rm -rf $newDotnetApiPath")
                    execute("rm -rf $oldDotnetApiPath")

                    execute("unzip ~/tmp/$filename -d $newDotnetApiPath")

                    execute("mkdir -p ~/public_html/tankroyale/api/dotnet")
                    execute("mv $dotnetApiPath $oldDotnetApiPath")
                    execute("mv $newDotnetApiPath $dotnetApiPath")
                    execute("rm -f ~/tmp/$filename")

                    println("done")
                })
            })
        }
    }

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
