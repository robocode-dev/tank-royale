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
        doLast {
            delete(fileTree("../docs").matching {
                exclude("api/**")
            })
        }
    }

    val npmBuild by registering(NpmTask::class) {
        dependsOn(npmInstall)

        args.set(listOf("run", "build"))
    }

    register<Copy>("uploadDocs") {
        dependsOn(clean)
        dependsOn(npmBuild)

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from("build/docs")
        into("../docs")
    }
}