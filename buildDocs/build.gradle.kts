import com.github.gradle.node.npm.task.NpmTask

description = "Robocode Tank Royale build documentation sources"

plugins {
    base // for clean task
    alias(libs.plugins.node.gradle)
}

node {
    version.set(libs.versions.node)
}

tasks {
    val npmBuild by registering(NpmTask::class) {
        dependsOn(npmInstall)

        args.set(listOf("run", "build"))
    }

    register<Copy>("uploadDocs") {
        dependsOn(npmBuild)

        val dotnetApiDir = "../docs"

        delete(fileTree(dotnetApiDir).matching {
            exclude("api/**")
        })

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from("build/docs")
        into(dotnetApiDir)
    }
}