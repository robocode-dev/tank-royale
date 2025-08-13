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
