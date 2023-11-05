import com.github.gradle.node.npm.task.NpmTask

description = "Robocode Tank Royale build documentation sources"

plugins {
    alias(libs.plugins.node.gradle)
}

node {
    version.set(libs.versions.node)
}

tasks {
    val cleanDocs by registering  {
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

    register<Copy>("uploadDocs") {
        dependsOn(cleanDocs, npmBuild)

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from("build/docs")
        into("../docs")
    }
}