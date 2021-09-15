import com.github.gradle.node.npm.task.NpmTask
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler

plugins {
    id("com.github.node-gradle.node") version "3.1.0"
    id("org.hidetake.ssh") version "2.10.1"
}

node {
    version.set("15.5.1")
}

val npmBuild = tasks.register<NpmTask>("npmBuild") {
    dependsOn(tasks.npmInstall)

    args.set(listOf("run", "build"))
}

tasks.register("build") {
    dependsOn(npmBuild)
}

val zipDocs = tasks.register<Zip>("zipDocs") {
    dependsOn(npmBuild)

    archiveFileName.set("docs.zip")
    destinationDirectory.set(File("build"))

    from(file("build/docs"))
}

val sshServer = remotes.create("sshServer") {
    withGroovyBuilder {
        setProperty("host", project.properties["tankroyale.ssh.host"])
        setProperty("port", (project.properties["tankroyale.ssh.port"] as String).toInt())
        setProperty("user", project.properties["tankroyale.ssh.user"])
        setProperty("password", project.properties["tankroyale.ssh.pass"])
    }
}

val uploadDocs by tasks.registering {
    dependsOn(zipDocs)

    doLast {
        ssh.run (delegateClosureOf<RunHandler> {
            session(sshServer, delegateClosureOf<SessionHandler> {
                print("Uploading docs...")

                val filename = "docs.zip"

                put(hashMapOf("from" to "${project.projectDir}/build/$filename", "into" to "tmp"))

                execute("rm -rf ~/public_html/tankroyale/docs_new")
                execute("rm -rf ~/public_html/tankroyale/docs_old")

                execute("unzip ~/tmp/$filename -d ~/public_html/tankroyale/docs_new")

                execute("mkdir -p ~/public_html/tankroyale/docs")
                execute("mv ~/public_html/tankroyale/docs ~/public_html/tankroyale/docs_old")
                execute("mv ~/public_html/tankroyale/docs_new ~/public_html/tankroyale/docs")
                execute("rm -f ~/tmp/$filename")

                println("done")
            })
        })
    }
}