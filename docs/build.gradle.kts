import com.github.gradle.node.npm.task.NpmTask
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler

apply(from = "../groovy.gradle")

val buildArchiveDirProvider: Provider<Directory> = layout.buildDirectory
val buildArchivePath = buildArchiveDirProvider.get().toString()

val htmlRoot: String by rootProject.extra
val docsPath = "$htmlRoot/docs"
val archiveFilename = "docs.zip"

plugins {
    alias(libs.plugins.node.gradle)
    alias(libs.plugins.hidetake.ssh)
}

node {
    version.set(libs.versions.node.version)
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

    archiveFileName.set(archiveFilename)
    destinationDirectory.set(File("build"))

    from(file("build/docs"))
}

tasks.register("uploadDocs") {
    dependsOn(zipDocs)

    ssh.run (delegateClosureOf<RunHandler> {
        session(remotes["sshServer"], delegateClosureOf<SessionHandler> {
            print("Uploading docs...")

            put(hashMapOf("from" to "$buildArchivePath/$archiveFilename", "into" to "tmp"))

            val oldDocsPath = docsPath + "_old_" + System.currentTimeMillis()
            val tmpDocsPath = docsPath + "_tmp"

            execute("unzip ~/tmp/$archiveFilename -d $tmpDocsPath")
            execute("rm -f ~/tmp/$archiveFilename")

            execute("mv $docsPath $oldDocsPath")
            execute("mv $tmpDocsPath $docsPath")

            println("done")
        })
    })
}