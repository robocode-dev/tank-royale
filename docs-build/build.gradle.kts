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
        commandLine("dotnet", "tool", "update", "-g", "docfx", "--version", "2.78.3")
    }

    val npmBuild by registering(NpmTask::class) {
        dependsOn(npmInstall)

        args = listOf("run", "build")
    }

    val build = named("build") {
        dependsOn(npmBuild)
    }

    register<NpmTask>("run") {
        dependsOn(npmInstall)

        args = listOf("run", "dev")
    }

    var clean = named("clean") {
        delete(fileTree("../docs").matching {
            exclude("api/**")
            exclude("CNAME")
        })
        delete(
            "./docs/.vitepress/cache",
            "./docs/.vitepress/dist"
        )
    }

    register<Copy>("copy-generated-docs") {
        dependsOn(clean)
        dependsOn(build)
        dependsOn(updateDocfx)

        doLast {
            // Ensure GitHub Pages won't try to apply Jekyll processing
            val noJekyll = File(rootProject.projectDir, "docs/.nojekyll")
            noJekyll.parentFile.mkdirs()
            if (!noJekyll.exists()) {
                noJekyll.createNewFile()
            }
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from("./docs/.vitepress/dist")
        into("../docs")
    }
}