import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import proguard.gradle.ProGuardTask

val title = "Robocode Tank Royale GUI Application"
description = "GUI application for starting battles for Robocode Tank Royale"

group = "dev.robocode.tankroyale"
val artifactId = "robocode-tankroyale-gui"
version = "0.8.3"

val archiveFileName = "$buildDir/libs/$artifactId-$version.jar"


buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.1.0-beta5")
    }
}

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    idea
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
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")

    implementation("com.miglayout:miglayout-swing:11.0")

    runtimeOnly(project(":server"))
    runtimeOnly(project(":booter"))
}

val copyServerJar = task<Copy>("copyServerJar") {
    dependsOn(":server:proguard")

    from(project(":server").file("/build/libs"))
    into(idea.module.outputDir)
    include("robocode-tankroyale-server-*.jar")
    rename("(.*)-[0-9]+\\..*.jar", "\$1.jar")
}

val copyBooterJar = task<Copy>("copyBooterJar") {
    dependsOn(":booter:proguard")

    from(project(":booter").file("/build/libs"))
    into(idea.module.outputDir)
    include("robocode-tankroyale-booter-*.jar")
    rename("(.*)-[0-9]+\\..*.jar", "\$1.jar")
}

tasks.processResources {
    with(copySpec {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("/src/main/resources")
        include("version.txt")
        filter(ReplaceTokens::class, "tokens" to mapOf("version" to version))
    })
}

tasks.jar {
    dependsOn(copyServerJar)
    dependsOn(copyBooterJar)
}

tasks.inspectClassesForKotlinIC {
    dependsOn(copyServerJar)
    dependsOn(copyBooterJar)
}

val fatJar = task<Jar>("fatJar") {
    dependsOn("copyServerJar")
    dependsOn("copyBooterJar")
    dependsOn(":server:jar")
    dependsOn(":booter:jar")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Implementation-Title"] = title
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "dev.robocode.tankroyale.gui.MainWindowKt"
    }
    from(
        configurations.compileClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) },
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    )
    exclude("*.kotlin_metadata")
    with(tasks["jar"] as CopySpec)
    archiveFileName.set("fat.jar")
}

val proguard = task<ProGuardTask>("proguard") {
    dependsOn(fatJar)
    injars("$buildDir/libs/fat.jar")
    outjars(archiveFileName)
    configuration("proguard-rules.pro")
}

tasks.named("build") {
    dependsOn(proguard)
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
