import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler

version = "0.9.10"

plugins {
    id("com.itiviti.dotnet") version "1.7.2"
    id("org.hidetake.ssh") version "2.10.1"
}

dotnet {
    build {
        version = project.version as String
        packageVersion = version
    }
}

val docfx = tasks.register("docfx") {
    exec {
        workingDir("docfx_project")
        executable("docfx")
    }
}

val zipApi = tasks.register<Zip>("zipApi") {
    dependsOn(docfx)

    archiveFileName.set("docfx.zip")
    destinationDirectory.set(layout.buildDirectory.dir("tmp"))

    from(file("docfx_project/_site"))
}

val sshServer = remotes.create("sshServer") {
    withGroovyBuilder {
        setProperty("host", project.properties["tankroyale.ssh.host"])
        setProperty("port", (project.properties["tankroyale.ssh.port"] as String).toInt())
        setProperty("user", project.properties["tankroyale.ssh.user"])
        setProperty("password", project.properties["tankroyale.ssh.pass"])
    }
}

val uploadDoc by tasks.registering {
    dependsOn(zipApi)

    doLast {
        ssh.run (delegateClosureOf<RunHandler> {
            session(sshServer, delegateClosureOf<SessionHandler> {
                print("Uploading doc...")

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