description = "Robocode Tank Royale Intent Diagnostics"

group = "dev.robocode.tankroyale"

base {
    archivesName = "robocode-tankroyale-intent-diagnostics"
}

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.java.websocket)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(platform(testLibs.junit.bom))
    testImplementation(testLibs.kotest.junit6)
    testImplementation(testLibs.bundles.junit)
    testImplementation(testLibs.assertj)
}

tasks {
    test {
        useJUnitPlatform()
        failFast = true
    }
}
