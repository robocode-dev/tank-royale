import proguard.gradle.ProGuardTask

description = "Robocode Tank Royale Recorder"

val title = "Robocode Tank Royale Recorder"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val jarManifestMainClass = "dev.robocode.tankroyale.recorder.RecorderKt"

base {
    archivesName = "robocode-tankroyale-recorder" // renames _all_ archive names
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
    `maven-publish`
}

dependencies {
    implementation(project(":lib:common"))
    implementation(project(":lib:client"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.picocli)
    implementation(libs.slf4j.api)
    implementation(libs.java.websocket)
}

tasks {
    jar {
        dependsOn(":lib:common:jar")
        dependsOn(":lib:client:jar")

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

    val proguard by registering(ProGuardTask::class) { // used for compacting and code-shaking
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
        dependsOn(jar)
        classpath = files(jar)
    }

    assemble {
        dependsOn(proguard)
        doLast {
            delete(intermediateJar) // Ensure intermediate JAR is cleaned
        }
    }

    val javadocJar = named("javadocJar")
    val sourcesJar = named("sourcesJar")

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

                // Add additional developer
                pom.developers {
                    developer {
                        id.set("jandurovec")
                        name.set("Jan Durovec")
                        url.set("https://github.com/jandurovec")
                    }
                }
            }
        }
    }
}