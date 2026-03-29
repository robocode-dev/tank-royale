plugins {
    base
    alias(libs.plugins.node.gradle)
}

version = libs.versions.tankroyale.get()

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

    named("build") {
        dependsOn(npmBuild)
    }

    register("test") {
        dependsOn(npmTest)
    }
}
