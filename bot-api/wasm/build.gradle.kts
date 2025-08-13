import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

// Add the Kotlin configuration with wasm target
kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser()
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
