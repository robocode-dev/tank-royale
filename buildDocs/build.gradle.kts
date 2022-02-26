import com.github.gradle.node.npm.task.NpmTask
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler

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
    version.set(libs.versions.node)
}

tasks {
    register("clean") {
        delete(project.buildDir)
    }

    val npmBuild by registering(NpmTask::class) {
        dependsOn(npmInstall)

        args.set(listOf("run", "build"))
    }

    val build by register("build") {
        dependsOn(npmBuild)
    }

    register<Copy>("uploadDocs") {
//        dependsOn(build)

        val dotnetApiDir = "../docs"

        delete(fileTree(dotnetApiDir).matching {
            exclude("api/**")
        })

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from("build/docs")
        into(dotnetApiDir)
    }
}