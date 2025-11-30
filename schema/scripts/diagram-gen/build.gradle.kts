plugins {
    kotlin("jvm") version "1.9.24"
    application
}

group = "dev.robocode.tankroyale"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
}

application {
    mainClass.set("dev.robocode.tankroyale.diagramgen.MainKt")
}

kotlin {
    jvmToolchain(11)
}

val schemaReadmeProperty = "schemaReadmePath"

tasks.register<JavaExec>("updateSchemaReadme") {
    group = "documentation"
    description = "Regenerates Mermaid diagrams in schema/schemas/README.md"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(application.mainClass)

    val readmePath = (project.findProperty(schemaReadmeProperty) as String?)?.ifBlank { null }
        ?: error("Missing -P$schemaReadmeProperty with absolute README path")
    args("--readme", readmePath)
}
