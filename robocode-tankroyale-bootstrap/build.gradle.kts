import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val title = "Robocode Tank Royale Bootstrap"
description = "Bootstrap utility for booting up bots for Robocode Tank Royale"

group = "dev.robocode.tankroyale"
val artifactId = "robocode-tankroyale-bootstrap"
version = "0.8.0"


plugins {
    `java-library`
    kotlin("jvm") version "1.5.20-M1"
    kotlin("plugin.serialization") version "1.5.20-M1"
    `maven-publish`
    idea
    id("com.github.ben-manes.versions") version "0.38.0"
}

tasks.withType<KotlinCompile> {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

idea {
    module {
        outputDir = file("$buildDir/classes/kotlin/main")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.20-M1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.20-M1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")

    implementation("info.picocli:picocli:4.6.1")
}

tasks.processResources {
    with(copySpec {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("/src/main/resources")
        include("version.txt")
        filter(ReplaceTokens::class, "tokens" to mapOf("version" to version))
    })
}

val fatJar = task<Jar>("fatJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Implementation-Title"] = title
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "dev.robocode.tankroyale.bootstrap.BootstrapKt"
    }
    from(
        configurations.compileClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) },
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    )
    exclude("*.kotlin_metadata")
    with(tasks["jar"] as CopySpec)
}

tasks.named("build") {
    dependsOn(fatJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(fatJar)
            groupId = group as String?
            artifactId
            version
        }
    }
}
