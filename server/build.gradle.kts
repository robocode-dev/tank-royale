import proguard.gradle.ProGuardTask
import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.SourceType

description = "Robocode Tank Royale Server"

val title = "Robocode Tank Royale Server"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val jarManifestMainClass = "dev.robocode.tankroyale.server.ServerKt"

val schemaPackage = "$group.schema"

base {
    archivesName = "robocode-tankroyale-server" // renames _all_ archive names
}

val artifactBasePath = "${base.libsDirectory.get()}/${base.archivesName.get()}-${project.version}"
val finalJar = "$artifactBasePath.jar" // Final artifact path
val intermediateJar = "$artifactBasePath-all.jar"


buildscript {
    dependencies {
        classpath(libs.proguard.gradle)
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
    implementation(libs.picocli)
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
        kotlin {
            srcDir(layout.buildDirectory.dir("generated-sources/schema"))
        }
    }
}

tasks {
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

        manifest {
            attributes["Main-Class"] = jarManifestMainClass
            attributes["Implementation-Title"] = title
            attributes["Implementation-Version"] = project.version
            attributes["Implementation-Vendor"] = "robocode.dev"
            attributes["Package"] = project.group
        }

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    val proguard by registering(ProGuardTask::class) { // used for compacting and code-shaking,
        dependsOn(jar)

        doFirst {
            if (!file(intermediateJar).exists()) {
                logger.error("Intermediate JAR not found at expected location: $intermediateJar")
                throw GradleException("Cannot proceed with ProGuard. Ensure the 'jar' task successfully creates $intermediateJar.")
            }
            logger.lifecycle("Found intermediate JAR: $intermediateJar. Proceeding with ProGuard.")
        }

        configuration(file("proguard-rules.pro")) // Path to your ProGuard rules file

        injars(intermediateJar) // Input JAR to process
        outjars(finalJar)       // Output JAR after ProGuard processing

        doLast {
            if (!file(finalJar).exists()) {
                logger.error("ProGuard task completed, but final JAR is missing: $finalJar")
                throw GradleException("ProGuard did not produce the expected output.")
            }
            logger.lifecycle("ProGuard task completed successfully. Final JAR available at: $finalJar")
        }
    }

    register("runJar", JavaExec::class) {
        dependsOn(proguard)
        classpath = files(proguard.get().outJarFiles)
    }

    assemble {
        dependsOn(proguard)
        doLast {
            delete(intermediateJar) // Ensure intermediate JAR is cleaned
        }
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

    // Configure the maven publication to use the ProGuard jar as the main artifact
    publishing {
        publications {
            named<MavenPublication>("maven") {
                val outJars = proguard.get().outJarFiles
                if (outJars.isEmpty()) {
                    throw GradleException("Proguard did not produce output artifacts")
                }

                artifact(proguard.get().outJarFiles[0]) {
                    builtBy(proguard)
                }
                artifact(javadocJar)
                artifact(sourcesJar)

                // Override the name in the POM with the title variable
                pom.name.set(title)
            }
        }
    }
}