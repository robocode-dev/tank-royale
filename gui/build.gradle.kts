import build.tasks.FatJar

description = "Robocode Tank Royale GUI"

val archiveTitle = "Robocode Tank Royale GUI"
val packageName = "robocode-tank-royale-gui" // Used for installer filenames (lowercase with hyphens)
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val jarManifestMainClass = "dev.robocode.tankroyale.gui.GuiAppKt"

base {
    archivesName = "robocode-tankroyale-gui" // renames _all_ archive names
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
    implementation(project(":lib:client"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.miglayout.swing)
    implementation(libs.jsvg)

    testImplementation(testLibs.kotest.junit5)
}

tasks {
    val copyBooterJar by registering(Copy::class) {
        dependsOn(":booter:r8ShrinkTask")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":booter").file("./build/libs"))
        into(file("./build/classes/kotlin/main"))
        include("robocode-tankroyale-booter-*.jar")
        exclude("*-javadoc.jar", "*-sources.jar", "*-all.jar")
        rename(".*", "robocode-tankroyale-booter.jar")
    }

    val copyServerJar by registering(Copy::class) {
        dependsOn(":server:r8ShrinkTask")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":server").file("./build/libs"))
        into(file("./build/classes/kotlin/main"))
        include("robocode-tankroyale-server-*.jar")
        exclude("*-javadoc.jar", "*-sources.jar", "*-all.jar")
        rename(".*", "robocode-tankroyale-server.jar")
    }

    val copyRecorderJar by registering(Copy::class) {
        dependsOn(":recorder:r8ShrinkTask")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":recorder").file("./build/libs"))
        into(file("./build/classes/kotlin/main"))
        include("robocode-tankroyale-recorder-*.jar")
        exclude("*-javadoc.jar", "*-sources.jar", "*-all.jar")
        rename(".*", "robocode-tankroyale-recorder.jar")
    }

    val copyJars = register("copyJars") {
        dependsOn(copyBooterJar, copyServerJar, copyRecorderJar)

        // Make copyJars properly declare its outputs
        outputs.dir(file("./build/classes/kotlin/main"))
    }

    val fatJar by registering(FatJar::class) {
        dependsOn(classes, copyJars)

        inputs.files(copyJars)
        inputs.files(configurations.runtimeClasspath)

        // Ensure the fat jar goes to the libs directory
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveFileName.set("$artifactBasePath-all.jar")

        title.set(archiveTitle)
        mainClass.set(jarManifestMainClass)
    }

    val r8ShrinkTask by registering(JavaExec::class) { // R8 shrinking task (kept name for compatibility)
        dependsOn(fatJar)

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

    jar {
        enabled = false
        dependsOn(r8ShrinkTask)
    }

    test {
        useJUnitPlatform()
    }

    withType<AbstractPublishToMaven> {
        dependsOn(jar)
    }

    javadoc {
        dependsOn("copyJars")
    }

    val javadocJar = named<Jar>("javadocJar") {
        dependsOn("copyJars")
    }
    val sourcesJar = named<Jar>("sourcesJar") {
        dependsOn("copyJars")
    }

    // Configure the maven publication to use the R8 jar as the main artifact
    publishing {
        publications {
            named<MavenPublication>("maven") {
                artifact(file(finalJar)) {
                    builtBy(r8ShrinkTask)
                }
                artifact(javadocJar) // required at Sonatype
                artifact(sourcesJar)

                // Override the name in the POM with the archiveTitle variable
                pom.name.set(archiveTitle)
            }
        }
    }

    // Opt-in to centralized jpackage tasks (configured in root build.gradle.kts)
    extra["useJpackage"] = true
    extra["jpackageAppName"] = archiveTitle
    extra["jpackagePackageName"] = packageName // For consistent installer filenames
    extra["jpackageMainJar"] = finalJar
    extra["jpackageMainClass"] = jarManifestMainClass
    extra["jpackageDependsOn"] = "r8ShrinkTask"
}
