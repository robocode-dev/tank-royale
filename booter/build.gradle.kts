import proguard.gradle.ProGuardTask

description = "Robocode Tank Royale Booter"

val title = "Robocode Tank Royale Booter"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val jarManifestMainClass = "dev.robocode.tankroyale.booter.BooterKt"

base {
    archivesName = "robocode-tankroyale-booter" // renames _all_ archive names
}

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
    implementation(libs.picocli)
    implementation(libs.jansi)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withSourcesJar()
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = jarManifestMainClass
            attributes["Implementation-Title"] = title
            attributes["Implementation-Version"] = archiveVersion
            attributes["Implementation-Vendor"] = "robocode.dev"
            attributes["Package"] = project.group
        }
    }

    shadowJar.configure {
        dependsOn(jar)
        archiveClassifier.set("all")
    }

    val proguard by registering(ProGuardTask::class) { // used for compacting and code-shaking
        dependsOn(shadowJar)
        injars("${base.libsDirectory.get()}/${base.archivesName}-${project.version}-all.jar")
        outjars("${base.libsDirectory.get()}/${base.archivesName}-${project.version}.jar")
        configuration("proguard-rules.pro")
    }

    val  sourcesJar = named("sourcesJar")

    publishing {
        publications {
            create<MavenPublication>("booter") {
                artifact(proguard.get().outJarFiles[0]) {
                    builtBy(proguard)
                }
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