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

@Suppress("DSL_SCOPE_VIOLATION") // remove later when IntelliJ supports the `libs.` DSL
plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow.jar)
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
    jar {
        manifest {
            attributes["Implementation-Title"] = jarManifestTitle
            attributes["Implementation-Version"] = archiveVersion
            attributes["Implementation-Vendor"] = "robocode.dev"
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact("${buildDir}/libs/java-$version-all.jar") {
                    builtBy(shadowJar)
                }
                groupId = group as String?
                artifactId = artifactBaseName
                version
            }
        }
    }
}