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
    clean {
        doFirst {
            delete(fileTree("../docs").matching {
                exclude("api/**")
            })
            delete(
                "docs/.vuepress/.cache",
                "docs/.vuepress/.temp",
                "docs/.vuepress/dist"
            )
        }
    }

    val npmBuild by registering(NpmTask::class) {
        dependsOn(npmInstall)

        args.set(listOf("run", "build"))
    }

    build {
        dependsOn(npmBuild)
    }

    register<Copy>("uploadDocs") {
        dependsOn(npmBuild)

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from("build/docs")
        into("../docs")
    }
}