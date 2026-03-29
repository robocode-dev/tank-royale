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
    npmInstall {
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
