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
        val wasmJsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-wasm-js"))
            }
        }
        val wasmJsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
