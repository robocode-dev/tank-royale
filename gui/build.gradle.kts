import build.tasks.FatJar
import proguard.gradle.ProGuardTask

description = "Robocode Tank Royale GUI"

val archiveTitle = "Robocode Tank Royale GUI"
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
    implementation(project(":lib:client"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.miglayout.swing)
    implementation(libs.jsvg)

    testImplementation(testLibs.kotest.junit5)
}

tasks {
    val copyBooterJar by registering(Copy::class) {
        dependsOn(":booter:proguard")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":booter").file("./build/libs"))
        into(file("./build/classes/kotlin/main"))
        include("robocode-tankroyale-booter-*.jar")
        exclude("*-javadoc.jar", "*-sources.jar", "*-all.jar")
        rename(".*", "robocode-tankroyale-booter.jar")
    }

    val copyServerJar by registering(Copy::class) {
        dependsOn(":server:proguard")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":server").file("./build/libs"))
        into(file("./build/classes/kotlin/main"))
        include("robocode-tankroyale-server-*.jar")
        exclude("*-javadoc.jar", "*-sources.jar", "*-all.jar")
        rename(".*", "robocode-tankroyale-server.jar")
    }

    val copyRecorderJar by registering(Copy::class) {
        dependsOn(":recorder:proguard")

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

    val proguard by registering(ProGuardTask::class) { // used for compacting and code-shaking,
        dependsOn(fatJar)

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

    jar {
        enabled = false
        dependsOn(proguard)
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
                artifact(javadocJar) // required at Sonatype
                artifact(sourcesJar)

                // Override the name in the POM with the archiveTitle variable
                pom.name.set(archiveTitle)
            }
        }
    }
}