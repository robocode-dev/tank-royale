import com.github.gradle.node.npm.task.NpmTask

description = "Robocode Tank Royale build documentation sources"

plugins {
    base
    alias(libs.plugins.node.gradle)
}

node {
    download = true
    version = "20.14.0"
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
    val installDocfx = register<Exec>("updateDocfx") {
        commandLine("dotnet", "tool", "update", "-g", "docfx", "--version", "2.77.0")
    }

    val npmBuild by registering(NpmTask::class) {
        dependsOn(npmInstall)

        args = listOf("run", "build")
    }

    register<Copy>("uploadDocs") {
        dependsOn(clean, npmBuild)
        dependsOn(installDocfx)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from("build/docs")
        into("../docs")
    }
}