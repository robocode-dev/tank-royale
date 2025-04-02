import proguard.gradle.ProGuardTask

description = "Robocode Tank Royale Booter"

val title = "Robocode Tank Royale Booter"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val jarManifestMainClass = "dev.robocode.tankroyale.booter.BooterKt"

base {
    archivesName = "robocode-tankroyale-booter" // renames _all_ archive names
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
    signing
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.picocli)
    implementation(libs.jansi)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }

    withJavadocJar() // required for uploading to Sonatype
    withSourcesJar()
}

tasks {
    jar {
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

    val runJar by registering(JavaExec::class) {
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

    publishing {
        publications {
            create<MavenPublication>("booter") {
                val outJars = proguard.get().outJarFiles
                if (outJars.isEmpty()) {
                    throw GradleException("Proguard did not produce output artifacts")
                }

                artifact(proguard.get().outJarFiles[0]) {
                    builtBy(proguard)
                }
                artifact(javadocJar)
                artifact(sourcesJar)

                groupId = group as String?
                artifactId = base.archivesName.get()
                version

                pom {
                    name.set(title)
                    description.set(project.description)
                    url.set("https://github.com/robocode-dev/tank-royale")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("fnl")
                            name.set("Flemming Nørnberg Larsen")
                            organization.set("flemming-n-larsen")
                            organizationUrl.set("https://github.com/flemming-n-larsen")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/robocode-dev/tank-royale.git")
                        developerConnection.set("scm:git:ssh://github.com:robocode-dev/tank-royale.git")
                        url.set("https://github.com/robocode-dev/tank-royale/tree/master")
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["booter"])
}