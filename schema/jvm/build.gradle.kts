import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.SourceType
import org.jsonschema2pojo.gradle.JsonSchemaExtension
import java.util.Collections.singletonList

description = "Robocode Tank Royale schema for Java"

val title = "Robocode Tank Royale schema for Java"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val artifactBaseName = "robocode-tankroyale-schema"
val archiveFileName = "$buildDir/libs/$artifactBaseName-$version.jar"

val schemaPackage = "dev.robocode.tankroyale.schema"

buildscript {
    dependencies {
        classpath(libs.jsonschema2pojo)
    }
}

@Suppress("DSL_SCOPE_VIOLATION") // remove later when IntelliJ supports the `libs.` DSL
plugins {
    java
}

apply(plugin = "jsonschema2pojo")

dependencies {
    implementation(libs.gson)
    implementation(libs.java.websocket)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// https://github.com/joelittlejohn/jsonschema2pojo/blob/master/jsonschema2pojo-gradle-plugin/src/main/groovy/org/jsonschema2pojo/gradle/JsonSchemaExtension.groovy
configure<JsonSchemaExtension> {
    setSourceType(SourceType.YAMLSCHEMA.toString())
    setSource(singletonList(File("$projectDir/../schemas")))
    setAnnotationStyle(AnnotationStyle.GSON.toString())
    targetPackage = schemaPackage
}