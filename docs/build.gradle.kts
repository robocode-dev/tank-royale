import com.github.gradle.node.npm.task.NpmTask
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler

apply(from = "../groovy.gradle")

val buildArchiveDirProvider: Provider<Directory> = layout.buildDirectory
val buildArchivePath = buildArchiveDirProvider.get().toString()

val htmlRoot: String by rootProject.extra
val docsPath = "$htmlRoot/docs"
val archiveFilename = "docs.zip"


@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.node.gradle)
    alias(libs.plugins.hidetake.ssh)
}

node {
    version.set(libs.versions.node.version)
}

tasks {
    register("clean") {
        delete(project.buildDir)
    }

    val npmBuild by registering(NpmTask::class) {
        dependsOn(npmInstall)

        args.set(listOf("run", "build"))
    }

    register("build") {
        dependsOn(npmBuild)
    }

    val zip by registering(Zip::class) {
        dependsOn(npmBuild)

        archiveFileName.set(archiveFilename)
        destinationDirectory.set(File("build"))

        from(file("build/docs"))
    }

    register("uploadDocs") {
        dependsOn(zip)

        doLast {
            ssh.run(delegateClosureOf<RunHandler> {
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
    }
}