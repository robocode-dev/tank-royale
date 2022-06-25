import dev.robocode.tankroyale.tasks.FatJar
import proguard.gradle.ProGuardTask

val archiveTitle = "Robocode Tank Royale GUI"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()
description = "Graphical user interface (GUI) for Robocode Tank Royale"

val jarManifestMainClass = "dev.robocode.tankroyale.gui.GuiAppKt"

val archiveBaseName = "robocode-tankroyale-gui"
val archiveFileName = "$buildDir/libs/$archiveBaseName-$version.jar"

buildscript {
    dependencies {
        classpath(libs.proguard.gradle)
    }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.miglayout.swing)
}

tasks {
    val copyBooterJar by registering(Copy::class) {
        dependsOn(inspectClassesForKotlinIC, ":booter:proguard")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":booter").file("./build/libs"))
        into(file("./build/classes/kotlin/main"))
        include("robocode-tankroyale-booter-*-proguard.jar")
        rename(".*", "robocode-tankroyale-booter.jar")
    }

    val copyServerJar by registering(Copy::class) {
        dependsOn(inspectClassesForKotlinIC, ":server:proguard")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":server").file("./build/libs"))
        into(file("./build/classes/kotlin/main"))
        include("robocode-tankroyale-server-*-proguard.jar")
        rename(".*", "robocode-tankroyale-server.jar")
    }

    val copyJars = register("copyJars") {
        dependsOn(copyBooterJar, copyServerJar)
    }

    val fatJar by registering(FatJar::class) {
        dependsOn(classes, copyJars)

        title.set(archiveTitle)
        mainClass.set(jarManifestMainClass)

        outputFilename.set(archiveFileName)
    }

    val proguard by registering(ProGuardTask::class) {
        dependsOn(fatJar)

        injars("$buildDir/libs/${project.name}-$version.jar")
        outjars(archiveFileName)
        configuration("proguard-rules.pro")
    }

    jar {
        enabled = false
        dependsOn(
            proguard
        )
    }

    withType<AbstractPublishToMaven>() {
        dependsOn(jar)
    }
}

publishing {
    publications {
        create<MavenPublication>("gui-app") {
            artifact(archiveFileName)

            groupId = group as String?
            artifactId = archiveBaseName
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