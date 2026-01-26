import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.SourceType

description = "Robocode Tank Royale Server"

val title = "Robocode Tank Royale Server"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val jarManifestMainClass = "dev.robocode.tankroyale.server.MainKt"

val schemaPackage = "$group.schema"

base {
    archivesName = "robocode-tankroyale-server" // renames _all_ archive names
}

val artifactBasePath = "${base.libsDirectory.get()}/${base.archivesName.get()}-${project.version}"
val finalJar = "$artifactBasePath.jar" // Final artifact path
val intermediateJar = "$artifactBasePath-all.jar"


buildscript {
    dependencies {
        classpath(libs.r8)
    }
}

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jsonschema2pojo)
    `maven-publish`
}

dependencies {
    implementation(project(":lib:common"))
    implementation(libs.java.websocket)
    implementation(libs.clikt)
    implementation(libs.gson)
    implementation(libs.slf4j.api)

    testImplementation(testLibs.kotest.junit5)
    testImplementation(testLibs.kotest.datatest)
    testImplementation(testLibs.mockk)
}

jsonSchema2Pojo {
    val schemaDir = layout.projectDirectory.dir("../schema/schemas").asFile
    if (!schemaDir.exists() || !schemaDir.isDirectory) {
        throw GradleException("Schema directory '${schemaDir.absolutePath}' does not exist or is not a directory.")
    }

    setSource(listOf(schemaDir))
    setSourceType(SourceType.YAMLSCHEMA.name)
    setAnnotationStyle(AnnotationStyle.GSON.name)
    setFileExtensions("schema.yaml", "schema.json")

    targetPackage = schemaPackage
    targetDirectory = layout.buildDirectory.dir("generated-sources/schema").get().asFile
}

sourceSets {
    main {
        // Add generated sources to Kotlin source set to avoid Java/Kotlin compile cycles
        kotlin {
            srcDir(layout.buildDirectory.dir("generated-sources/schema"))
        }
    }
}

tasks {
    // Ensure schema diagram generation runs before schema code generation
    // This prevents timing issues when both tasks run in parallel
    named("generateJsonSchema2Pojo") {
        mustRunAfter(rootProject.tasks.named("generateSchemaDiagrams"))
    }

    // Generate sources before compiling Kotlin (which includes generated Java as sources)
    compileKotlin {
        dependsOn(generateJsonSchema2Pojo)
    }

    test {
        useJUnitPlatform()
    }

    jar {
        dependsOn(":lib:common:jar")

        archiveClassifier.set("all") // the final archive will not have this classifier

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        // Ensure the intermediate '-all.jar' is declared as an output so Gradle knows about it
        outputs.file(file(intermediateJar))

        manifest {
            attributes["Main-Class"] = jarManifestMainClass
            attributes["Implementation-Title"] = title
            attributes["Implementation-Version"] = project.version
            attributes["Implementation-Vendor"] = "robocode.dev"
            attributes["Package"] = project.group
        }

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    val r8ShrinkTask by registering(JavaExec::class) { // R8 shrinking task (kept name for compatibility)
        dependsOn(jar)

        outputs.file(finalJar)

        doFirst {
            if (!file(intermediateJar).exists()) {
                logger.error("Intermediate JAR not found at expected location: $intermediateJar")
                throw GradleException("Cannot proceed with R8. Ensure the 'jar' task successfully creates $intermediateJar.")
            }
            logger.lifecycle("Found intermediate JAR: $intermediateJar. Proceeding with R8.")
        }

        mainClass.set("com.android.tools.r8.R8")
        classpath = buildscript.configurations["classpath"]

        args = listOf(
            "--release",
            "--classfile",
            "--lib", System.getProperty("java.home"),
            "--output", finalJar,
            "--pg-conf", file("r8-rules.pro").absolutePath,
            intermediateJar
        )

        doLast {
            if (!file(finalJar).exists()) {
                logger.error("R8 task completed, but final JAR is missing: $finalJar")
                throw GradleException("R8 did not produce the expected output.")
            }
            logger.lifecycle("R8 task completed successfully. Final JAR available at: $finalJar")
        }
    }

    register("runJar", JavaExec::class) {
        dependsOn(r8ShrinkTask)
        classpath = files(finalJar)
    }

    val smokeTest by registering(Exec::class) {
        dependsOn(r8ShrinkTask)
        group = "verification"
        description = "Smoke test the distributable JAR with --version"

        commandLine("java", "-jar", finalJar, "--version")

        doFirst {
            if (!file(finalJar).exists()) {
                throw GradleException("Final JAR not found at: $finalJar")
            }
        }
    }

    assemble {
        dependsOn(r8ShrinkTask)
    }

    val javadocJar = named("javadocJar")
    val sourcesJar = named("sourcesJar") {
        dependsOn(compileJava)
        dependsOn(generateJsonSchema2Pojo)
    }

    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            exclude("dev/robocode/tankroyale/schema/**")
        }
    }

    // Configure the maven publication to use the R8 jar as the main artifact
    publishing {
        publications {
            named<MavenPublication>("maven") {
                artifact(file(finalJar)) {
                    builtBy(r8ShrinkTask)
                }
                artifact(javadocJar)
                artifact(sourcesJar)

                // Override the name in the POM with the title variable
                pom.name.set(title)
            }
        }
    }

    // Opt-in to centralized jpackage tasks (configured in root build.gradle.kts)
    extra["useJpackage"] = false
    extra["jpackageAppName"] = title
    extra["jpackageMainJar"] = finalJar
    extra["jpackageDependsOn"] = "r8ShrinkTask"
    extra["jpackageMainClass"] = jarManifestMainClass
}
