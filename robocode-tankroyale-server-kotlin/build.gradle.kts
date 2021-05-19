import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val title = "Robocode Tank Royale Server"
description = "Server for running Robocode Tank Royale"

group = "dev.robocode.tankroyale"
version = "0.8.5"


plugins {
    application
    kotlin("jvm") version "1.5.0"
    `maven-publish`
    idea
    id("com.github.ben-manes.versions") version "0.38.0"
}

tasks.withType<KotlinCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

idea {
    module {
        outputDir = file("$buildDir/classes/kotlin/main")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.0")

    implementation("dev.robocode.tankroyale:robocode-tankroyale-schema:0.7.0")
    implementation("org.danilopianini:gson-extras:0.2.2")

    implementation("org.java-websocket:Java-WebSocket:1.5.2")

    implementation("info.picocli:picocli:4.6.1")

    implementation("org.fusesource.jansi:jansi:2.3.2")

    implementation("ch.qos.logback:logback-classic:1.3.0-alpha5")
    implementation("ch.qos.logback:logback-core:1.3.0-alpha5")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.5.0.RC1")
    testImplementation("io.mockk:mockk:1.11.0")
}

tasks.processResources {
    with(copySpec {
        from("/src/main/resources")
        include("version.txt")
        filter(ReplaceTokens::class, "tokens" to mapOf("version" to version))
        duplicatesStrategy = DuplicatesStrategy.WARN
    })
}

val fatJar = task<Jar>("fatJar") {
    manifest {
        attributes["Implementation-Title"] = title
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "dev.robocode.tankroyale.server.ServerKt"
    }
    from(
        configurations.compileClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) },
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    )
    exclude("*.kotlin_metadata")
    with(tasks["jar"] as CopySpec)
    duplicatesStrategy = DuplicatesStrategy.WARN
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
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}