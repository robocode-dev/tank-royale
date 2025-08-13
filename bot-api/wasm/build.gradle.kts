import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import com.github.gradle.node.task.NodeTask

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.node.gradle)
}

node {
    version = "24.5.0"
    download = true
}

// Add the Kotlin configuration with wasm target
kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        nodejs() // we use node.js to run the tests (instead of using a browser as test runtime)
    }

    sourceSets {
        wasmJsMain {
            dependencies {
                implementation(kotlin("stdlib-wasm-js"))
            }
        }
        wasmJsTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

// Add a task to run the compiled WebAssembly with Node.js
tasks.register<NodeTask>("run") {
    group = "application"
    description = "Runs the compiled WebAssembly with Node.js"

    // This task depends on the build task to ensure the code is compiled
    dependsOn("build")

    // The directory containing our WebAssembly files
    val wasmDir = "${layout.buildDirectory.get()}/compileSync/wasmJs/main/productionExecutable/kotlin"

    val resolvedScriptFile = providers.provider {
        val jsFiles = fileTree(wasmDir) {
            include("**/*.mjs")
        }.files
        if (jsFiles.isEmpty()) {
            throw GradleException("No JavaScript files found in $wasmDir to load WebAssembly")
        }
        jsFiles.first()
    }

    // Set the NodeTask script to a provider-backed file (deferred evaluation).
    script.set(layout.file(resolvedScriptFile.map { it.absolutePath }.map { File(it) }))

    doFirst {
        // Log which script will be used; do NOT attempt to reassign script (it's final once set).
        val scriptFile = resolvedScriptFile.get()
        logger.info("Running Node.js with: ${scriptFile.absolutePath}")
    }

    // Working directory
    workingDir.set(file(wasmDir))
}
