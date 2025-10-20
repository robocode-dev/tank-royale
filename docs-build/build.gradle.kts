import com.github.gradle.node.npm.task.NpmTask

description = "Robocode Tank Royale build documentation sources"

plugins {
    base
    alias(libs.plugins.node.gradle)
}

node {
    version = "22.20.0" // LTS
    download = true
}

tasks {
    // Install or update to `docfx`
    val updateDocfx by registering(Exec::class) {
        commandLine("dotnet", "tool", "update", "-g", "docfx", "--version", "2.77.0")
    }

    val npmBuild by registering(NpmTask::class) {
        dependsOn(npmInstall)

        args = listOf("run", "build")
    }

    register<NpmTask>("run") {
        dependsOn(npmInstall)

        args = listOf("run", "dev")
    }

    register<Copy>("copyGeneratedDocs") {
        dependsOn(npmBuild)
        dependsOn(updateDocfx)

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

        doLast {
            // Ensure GitHub Pages won't try to apply Jekyll processing
            val noJekyll = File(rootProject.projectDir, "docs/.nojekyll")
            noJekyll.parentFile.mkdirs()
            if (!noJekyll.exists()) {
                noJekyll.createNewFile()
            }
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from("build/docs")
        into("../docs")
    }
}