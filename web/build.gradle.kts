import com.github.gradle.node.npm.task.NpmTask
import build.docs.documentationOutputDirectory

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
    // Predicate: run docs-related generation only when explicitly requested
    fun isDocsRequested(): Boolean = gradle.startParameter.taskNames.any {
        it.contains("upload-docs") ||
                it == "copy-generated-docs" ||
                it.endsWith(":copy-generated-docs") ||
                it == ":web:build" ||
                it.endsWith(":web:build")
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

    val clean = named<Delete>("clean") {
        delete(
            "./docs/.vitepress/cache",
            "./docs/.vitepress/dist"
        )
    }

    register<Copy>("copy-vitepress-docs") {
        description = "Copies VitePress docs into the Pages staging directory without API generation"
        dependsOn(build, ":cleanDocumentationOutput")

        doLast {
            // Ensure GitHub Pages won't try to apply Jekyll processing
            val noJekyll = documentationOutputDirectory(".nojekyll").get().asFile
            noJekyll.parentFile.mkdirs()
            if (!noJekyll.exists()) {
                noJekyll.createNewFile()
            }
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from("./docs/.vitepress/dist")
        into(documentationOutputDirectory())
    }

    register("copy-generated-docs") {
        description = "Copies the generated VitePress site into the Pages staging directory"
        dependsOn("copy-vitepress-docs")
    }
}
