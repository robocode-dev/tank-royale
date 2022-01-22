import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.SourceType
import java.util.Collections.singletonList
import org.jsonschema2pojo.gradle.JsonSchemaExtension
import java.lang.reflect.Field

val title = "Robocode Tank Royale Schema"
description = "Schema for Robocode Tank Royale"

group = "dev.robocode.tankroyale"
version = "0.8.1"

val artifactBaseName = "robocode-tankroyale-schema"

buildscript {
    dependencies {
        classpath(libs.jsonschema2pojo)
    }
}

plugins {
    `java-library`
    idea
}

dependencies {
    implementation(libs.gson)
}

apply(plugin = "java")
apply(plugin = "jsonschema2pojo")

// https://github.com/joelittlejohn/jsonschema2pojo/blob/master/jsonschema2pojo-gradle-plugin/src/main/groovy/org/jsonschema2pojo/gradle/JsonSchemaExtension.groovy
configure<JsonSchemaExtension> {
    setSourceType(SourceType.YAMLSCHEMA.toString())
    setSource(singletonList(File("$projectDir/../schemas")))
    setAnnotationStyle(AnnotationStyle.GSON.toString())
    targetPackage = "dev.robocode.tankroyale.schema"
}

val fatJar = task<Jar>("fatJar") {
    archiveBaseName.set(artifactBaseName)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Implementation-Title"] = title
        attributes["Implementation-Version"] = archiveVersion
    }
    from(
        configurations.compileClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) },
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    )
    with(tasks["jar"] as CopySpec)
}

tasks.named("build") {
    dependsOn(fatJar)
}