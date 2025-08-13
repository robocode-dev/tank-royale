import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
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
tasks.register<Exec>("runWithNodeJs") {
    group = "application"
    description = "Runs the compiled WebAssembly with Node.js"

    // This task depends on the build task to ensure the code is compiled
    dependsOn("build")

    // The directory containing our WebAssembly files
    val wasmDir = "${layout.buildDirectory.get()}/compileSync/wasmJs/main/productionExecutable"

    // The JavaScript loader file that Node.js can execute
    val jsLoaderFile = "$wasmDir/kotlin.js"

    doFirst {
        if (!file(jsLoaderFile).exists()) {
            logger.error("JavaScript loader not found at: $jsLoaderFile")
            logger.error("Looking for alternative loaders...")

            val jsFiles = fileTree(wasmDir) {
                include("**/*.mjs")
            }.files

            if (jsFiles.isEmpty()) {
                throw GradleException("No JavaScript files found in $wasmDir to load WebAssembly")
            } else {
                logger.info("Found JavaScript files: ${jsFiles.joinToString { it.name }}")
                // Use the first JavaScript file found as a fallback
                commandLine("node", jsFiles.first().absolutePath)
                return@doFirst
            }
        }

        logger.info("Running Node.js with: $jsLoaderFile")
    }

    // The command to execute (will be overridden in doFirst if jsLoaderFile doesn't exist)
    commandLine("node", jsLoaderFile)

    // Working directory
    workingDir = file(wasmDir)
}
