import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.SourceType
import java.util.Collections.singletonList

description = "Robocode Tank Royale schema for Java"

group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val schemaPackage = "dev.robocode.tankroyale.schema"

plugins {
    java
    alias(libs.plugins.jsonschema2pojo)
}

dependencies {
    implementation(libs.gson)
    implementation(libs.java.websocket)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// https://github.com/joelittlejohn/jsonschema2pojo/tree/master/jsonschema2pojo-gradle-plugin
jsonSchema2Pojo {
    setSourceType(SourceType.YAMLSCHEMA.toString())
    setSource(singletonList(File("$projectDir/../schemas")))
    setAnnotationStyle(AnnotationStyle.GSON.toString())
    targetPackage = schemaPackage

    setFileExtensions("schema.yaml", "schema.json")
}