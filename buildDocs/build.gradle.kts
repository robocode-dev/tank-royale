import com.github.gradle.node.npm.task.NpmTask

description = "Robocode Tank Royale build documentation sources"

plugins {
    base
    alias(libs.plugins.node.gradle)
}

node {
    version = "18.16.0"
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

        args = listOf("run", "build")
    }

    register<Copy>("uploadDocs") {
        dependsOn(clean, npmBuild)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from("build/docs")
        into("../docs")
    }
}