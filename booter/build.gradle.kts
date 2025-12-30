description = "Robocode Tank Royale Booter"

val title = "Robocode Tank Royale Booter"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val jarManifestMainClass = "dev.robocode.tankroyale.booter.MainKt"

base {
    archivesName = "robocode-tankroyale-booter" // renames _all_ archive names
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
    `maven-publish`
}

dependencies {
    implementation(project(":lib:common"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.clikt)
}

tasks {
    jar {
        dependsOn(":lib:common:jar")

        archiveClassifier = "all" // the final archive will not have this classifier

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

    val proguard by registering(JavaExec::class) { // R8 shrinking task (kept name for compatibility)
        dependsOn(jar)

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
        dependsOn(jar)
        classpath = files(jar)
    }

    val smokeTest by registering(Exec::class) {
        dependsOn(proguard)
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
        dependsOn(proguard)
        doLast {
            delete(intermediateJar) // Ensure intermediate JAR is cleaned
        }
    }

    val javadocJar = named("javadocJar")
    val sourcesJar = named("sourcesJar")

    // Configure the maven publication to use the R8 jar as the main artifact
    publishing {
        publications {
            named<MavenPublication>("maven") {
                artifact(file(finalJar)) {
                    builtBy(proguard)
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
    extra["jpackageMainClass"] = jarManifestMainClass
    extra["jpackageDependsOn"] = "proguard"
}
