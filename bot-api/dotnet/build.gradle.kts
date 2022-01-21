import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

apply(from = "../../groovy.gradle")


val artifactName = "Robocode.TankRoyale.BotApi"
version = "0.9.11"

val docfxArchiveFilename = "docfx.zip"

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
    val docfx by registering {
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

    val zip by registering(Zip::class) {
        dependsOn(docfx)

        archiveFileName.set(docfxArchiveFilename)
        destinationDirectory.set(buildArchiveDirProvider)

        from(file("docfx_project/_site"))
    }

    register("uploadDocs") {
        dependsOn(zip)

        doLast {
            ssh.run(delegateClosureOf<RunHandler> {
                session(remotes["sshServer"], delegateClosureOf<SessionHandler> {
                    print("Uploading docs...")

                    put(hashMapOf("from" to "${buildArchivePath}/$docfxArchiveFilename", "into" to "tmp"))

                    execute("rm -rf $newDotnetApiPath")
                    execute("rm -rf $oldDotnetApiPath")

                    execute("unzip ~/tmp/$docfxArchiveFilename -d $newDotnetApiPath")

                    execute("mv $dotnetApiPath $oldDotnetApiPath")
                    execute("mv $newDotnetApiPath $dotnetApiPath")
                    execute("rm -f ~/tmp/$docfxArchiveFilename")

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
