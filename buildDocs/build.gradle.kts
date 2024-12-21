import com.github.gradle.node.npm.task.NpmTask

description = "Robocode Tank Royale build documentation sources"

plugins {
    base
    alias(libs.plugins.node.gradle)
}

node {
    download = true
    version = "22.11.0"
}

tasks {
    clean {
        doLast {
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

    // Install or update to `docfx`
    val updateDocfx by registering(Exec::class) {
        commandLine("dotnet", "tool", "update", "-g", "docfx", "--version", "2.77.0")
    }

    val npmBuild by registering(NpmTask::class) {
        dependsOn(npmInstall)

        args = listOf("run", "build")
    }

    val run by registering(NpmTask::class) {
        dependsOn(npmInstall)

        args = listOf("run", "dev")
    }

    val uploadDocs by registering(Copy::class) {
        dependsOn(clean, npmBuild)
        dependsOn(updateDocfx)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from("build/docs")
        into("../docs")
    }
}