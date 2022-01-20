import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

val artifactName = "Robocode.TankRoyale.BotApi"
version = "0.9.11"

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

val sshServer = remotes.create("sshServer") {
    withGroovyBuilder {
        setProperty("host", project.properties["tankroyale.ssh.host"])
        setProperty("port", (project.properties["tankroyale.ssh.port"] as String).toInt())
        setProperty("user", project.properties["tankroyale.ssh.user"])
        setProperty("password", project.properties["tankroyale.ssh.pass"])
    }
}

tasks {
    val docfx = register("docfx") {
        dependsOn(clean)

        doFirst {
            delete("docfx_project/_site")
            delete("docfx_project/obj")
        }

        doLast {
            exec {
                workingDir("docfx_project")
                commandLine("docfx", "build")
            }
        }
    }

    val zip = register<Zip>("zip") {
        dependsOn(docfx)

        doLast {
            archiveFileName.set("docfx.zip")
            destinationDirectory.set(layout.buildDirectory.dir("tmp"))

            from(file("docfx_project/_site"))
        }
    }

    register("uploadDocs") {
        dependsOn(zip)

        doLast {
            ssh.run(delegateClosureOf<RunHandler> {
                session(sshServer, delegateClosureOf<SessionHandler> {
                    print("Uploading docs...")

                    val filename = "docfx.zip"

                    put(hashMapOf("from" to "${project.projectDir}/build/tmp/$filename", "into" to "tmp"))

                    execute("rm -rf ~/public_html/tankroyale/api/dotnet_new")
                    execute("rm -rf ~/public_html/tankroyale/api/dotnet_old")

                    execute("unzip ~/tmp/$filename -d ~/public_html/tankroyale/api/dotnet_new")

                    execute("mkdir -p ~/public_html/tankroyale/api/dotnet")
                    execute("mv ~/public_html/tankroyale/api/dotnet ~/public_html/tankroyale/api/dotnet_old")
                    execute("mv ~/public_html/tankroyale/api/dotnet_new ~/public_html/tankroyale/api/dotnet")
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
