import dev.robocode.tankroyale.tasks.FatJar
import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.SourceType
import org.jsonschema2pojo.gradle.JsonSchemaExtension
import java.util.Collections.singletonList

group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()
description = "Schema for Robocode Tank Royale"

val jarManifestTitle = "Robocode Tank Royale Schema"

val artifactBaseName = "robocode-tankroyale-schema"
val archiveFileName = "$buildDir/libs/$artifactBaseName-$version.jar"


buildscript {
    dependencies {
        classpath(libs.jsonschema2pojo)
    }
}

plugins {
    `java-library`
    `maven-publish`
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

tasks {
    val fatJar by registering(FatJar::class) {
        dependsOn(classes)

        title.set(jarManifestTitle)
        outputFilename.set(archiveFileName)
    }

    jar { // Replace jar task
        actions = emptyList()
        finalizedBy(fatJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(archiveFileName)
            groupId = group as String?
            artifactId = artifactBaseName
            version
        }
    }
}