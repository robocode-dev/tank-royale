import build.tasks.FatJar
import proguard.gradle.ProGuardTask

description = "Robocode Tank Royale GUI Application"

val archiveTitle = "Robocode Tank Royale GUI"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val jarManifestMainClass = "dev.robocode.tankroyale.gui.GuiAppKt"

base {
    archivesName = "robocode-tankroyale-gui" // renames _all_ archive names
}

val baseArchiveName = "${base.archivesName.get()}-${project.version}"

buildscript {
    dependencies {
        classpath(libs.proguard.gradle)
    }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.shadow.jar)
    `maven-publish`
    signing
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.miglayout.swing)
    implementation(libs.jsvg)

    testImplementation(testLibs.kotest.junit5)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withJavadocJar() // required for uploading to Sonatype
    withSourcesJar()
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

    val copyJars = register("copyJars") {
        dependsOn(copyBooterJar, copyServerJar)

        // Make copyJars properly declare its outputs
        outputs.dir(file("./build/classes/kotlin/main"))
    }

    val fatJar by registering(FatJar::class) {
        dependsOn(classes, copyJars)

        inputs.files(copyJars)
        inputs.files(configurations.runtimeClasspath)

        // Ensure the fat jar goes to the libs directory
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveFileName.set("$baseArchiveName-all.jar")

        title.set(archiveTitle)
        mainClass.set(jarManifestMainClass)
    }

    val runJar by registering(JavaExec::class) {
        dependsOn(fatJar)
        classpath = files(fatJar)
    }

    val proguard by registering(ProGuardTask::class) {
        dependsOn(fatJar)

        val libsDir = layout.buildDirectory.dir("libs").get().asFile
        val inputJar = libsDir.resolve("$baseArchiveName-all.jar")
        val outputJar = libsDir.resolve("$baseArchiveName.jar")

        inputs.file(inputJar)
        outputs.file(outputJar)

        configuration("proguard-rules.pro")

        injars(inputJar.absolutePath)
        outjars(outputJar.absolutePath)

        // Add this to ensure the input jar exists
        doFirst {
            if (!inputJar.exists()) {
                throw GradleException("Input jar file not found: ${inputJar.absolutePath}")
            }
        }
    }

    jar {
        enabled = false
        dependsOn(proguard)
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

    val sourcesJar = named("sourcesJar")

    publishing {
        publications {
            create<MavenPublication>("gui-app") {
                artifact(proguard.get().outJarFiles[0]) {
                    builtBy(proguard)
                }
                artifact(javadocJar) // required at Sonatype
                artifact(sourcesJar)

                groupId = group as String?
                artifactId = base.archivesName.get()
                version

                pom {
                    name.set(archiveTitle)
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
    sign(publishing.publications["gui-app"])
}