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

    // Predicate: run docs-related generation only when explicitly requested
    fun isDocsRequested(): Boolean = gradle.startParameter.taskNames.any {
        it.contains("build-release") ||
                it.contains("upload-docs") ||
                it.contains("create-release") ||
                it == "copy-generated-docs" ||
                it.endsWith(":copy-generated-docs") ||
                it == ":docs-build:build" ||
                it.endsWith(":docs-build:build")
    }

    val npmBuild by registering(NpmTask::class) {
        dependsOn(npmInstall)

        // Do not build the docs site during a normal root build
        onlyIf { isDocsRequested() }

        args = listOf("run", "build")
    }

    val build = named("build") {
        // Avoid participating in the global `build` unless docs are explicitly requested
        onlyIf { isDocsRequested() }
        dependsOn(npmBuild)
    }

    register<NpmTask>("run") {
        dependsOn(npmInstall)

        args = listOf("run", "dev")
    }

    // Configure the local clean task: never touch ../docs on a plain `clean` run
    val clean = named<Delete>("clean") {
        delete(
            "./docs/.vitepress/cache",
            "./docs/.vitepress/dist"
        )
    }

    register<Copy>("copy-generated-docs") {
        // Only run when explicitly requested by docs/release tasks or this task itself
        onlyIf {
            gradle.startParameter.taskNames.any {
                it.contains("build-release") ||
                        it.contains("upload-docs") ||
                        it.contains("create-release") ||
                        it == "copy-generated-docs" ||
                        it.endsWith(":copy-generated-docs")
            }
        }

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