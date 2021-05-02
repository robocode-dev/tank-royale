import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val title = "Robocode Tank Royale GUI"
description = "Desktop application for Robocode Tank Royale"

group = "dev.robocode.tankroyale"
val artifactId = "robocode-tankroyale-gui"
version = "0.6.23"


val serverVersion = "0.8.3"
val bootstrapVersion = "0.7.1"


plugins {
    `java-library`
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    `maven-publish`
    idea
    id("com.github.ben-manes.versions") version "0.38.0"
}

tasks.withType<KotlinCompile> {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()

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
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")

    implementation("org.java-websocket:Java-WebSocket:1.5.2")

    implementation("com.miglayout:miglayout-swing:5.3")

    runtimeOnly("dev.robocode.tankroyale:robocode-tankroyale-server:${serverVersion}") {
        exclude("ch.qos.logback")
    }
    runtimeOnly("dev.robocode.tankroyale:robocode-tankroyale-bootstrap:${bootstrapVersion}")
}

val copyServerJar = task<Copy>("copyServerJar") {
    from(configurations.runtimeClasspath)
    into(idea.module.outputDir)
    include("robocode-tankroyale-server-*.jar")
    rename("(.*)-[0-9]+\\..*.jar", "\$1.jar")
}

val copyBootstrapJar = task<Copy>("copyBootstrapJar") {
    from(configurations.runtimeClasspath)
    into(idea.module.outputDir)
    include("robocode-tankroyale-bootstrap-*.jar")
    rename("(.*)-[0-9]+\\..*.jar", "\$1.jar")
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
        attributes["Main-Class"] = "dev.robocode.tankroyale.gui.ui.MainWindowKt"
    }
    from(
        configurations.compileClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) },
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    )
    exclude("*.kotlin_metadata")
    with(tasks["jar"] as CopySpec)
    duplicatesStrategy = DuplicatesStrategy.WARN
}

tasks.named("build") {
    dependsOn(copyServerJar)
    dependsOn(copyBootstrapJar)
//    dependsOn(fatJar)
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
