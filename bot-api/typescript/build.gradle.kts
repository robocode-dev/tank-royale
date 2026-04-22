plugins {
    base
    alias(libs.plugins.node.gradle)
}

node {
    download = true
    version = "22.14.0"
    workDir = layout.projectDirectory.dir(".gradle/nodejs")
    npmWorkDir = layout.projectDirectory.dir(".gradle/npm")
}

tasks {
    val syncVersion by registering {
        group = "build"
        description = "Synchronises package.json version with gradle.properties"
        val pkgFile = file("package.json")
        val newVersion = project.version.toString()
        inputs.property("version", newVersion)
        outputs.file(pkgFile)
        doLast {
            val content = pkgFile.readText()
            val updated = content.replace(
                Regex(""""version":\s*"[^"]+""""),
                """"version": "$newVersion""""
            )
            pkgFile.writeText(updated)
            logger.lifecycle("Updated package.json version to $newVersion")
        }
    }

    npmInstall {
        dependsOn(syncVersion)
        inputs.file("package.json")
        outputs.dir("node_modules")
    }

    val npmBuild by registering(com.github.gradle.node.npm.task.NpmTask::class) {
        dependsOn(npmInstall)
        args = listOf("run", "build")
        inputs.dir("src")
        inputs.file("tsconfig.json")
        inputs.file("package.json")
        outputs.dir("dist")
    }

    register("npmPack", com.github.gradle.node.npm.task.NpmTask::class) {
        dependsOn(npmBuild)
        args = listOf("pack")
        inputs.dir("dist")
        inputs.file("package.json")
        outputs.files(fileTree(projectDir) { include("*.tgz") })
        doFirst {
            fileTree(projectDir) { include("*.tgz") }.forEach { it.delete() }
        }
    }

    register("npmPublish", com.github.gradle.node.npm.task.NpmTask::class) {
        dependsOn(npmBuild)
        val token = project.findProperty("npmjs-api-key") as String?
        args = listOf("publish", "--access", "public")
        inputs.dir("dist")
        inputs.file("package.json")
        doFirst {
            if (token.isNullOrBlank()) error("npmjs-api-key is not set in gradle.properties")
            file(".npmrc").writeText("//registry.npmjs.org/:_authToken=$token\n")
        }
        doLast {
            file(".npmrc").delete()
        }
    }

    named("clean") {
        doLast {
            fileTree(projectDir) { include("*.tgz") }.forEach { it.delete() }
        }
    }

    val npmTest by registering(com.github.gradle.node.npm.task.NpmTask::class) {
        dependsOn(npmInstall)
        args = listOf("run", "test")
        inputs.dir("src")
        inputs.dir("test")
        inputs.file("tsconfig.json")
        inputs.file("package.json")
    }

    val generateTypescriptApiDocs by registering(com.github.gradle.node.npm.task.NpmTask::class) {
        dependsOn(npmInstall)
        args = listOf("run", "docs")
        inputs.dir("src")
        inputs.file("tsconfig.json")
        inputs.file("package.json")
        inputs.file("typedoc.json")
        outputs.dir("build/docs/typedoc")

        onlyIf {
            gradle.startParameter.taskNames.any {
                it.contains("upload-docs") ||
                it == "generateTypescriptApiDocs" ||
                it.endsWith(":generateTypescriptApiDocs")
            }
        }
    }

    register<Copy>("copyTypescriptApiDocs") {
        dependsOn(generateTypescriptApiDocs)

        // Only copy docs when explicitly asked for via upload-docs task or this task itself
        onlyIf {
            gradle.startParameter.taskNames.any {
                it.contains("upload-docs") ||
                it == "copyTypescriptApiDocs" ||
                it.endsWith(":copyTypescriptApiDocs")
            }
        }

        val typescriptApiDir = layout.projectDirectory.dir("../../docs/api/typescript")

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from(layout.buildDirectory.dir("docs/typedoc"))
        into(typescriptApiDir)

        doFirst {
            // Clean target directory only when task actually runs
            delete(typescriptApiDir)
            mkdir(typescriptApiDir)
        }
    }

    named("build") {
        dependsOn(npmBuild)
    }

    register("test") {
        dependsOn(npmTest)
    }

    // Make sure documentation tasks are not part of the build task
    afterEvaluate {
        tasks.named("build").configure {
            setDependsOn(dependsOn.filterNot {
                it.toString().contains("generateTypescriptApiDocs") || it.toString().contains("copyTypescriptApiDocs")
            })
        }
    }
}
