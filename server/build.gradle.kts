import proguard.gradle.ProGuardTask
import java.util.Collections.singletonList
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

val baseArchiveName = "${base.libsDirectory.get()}/${base.archivesName.get()}-${project.version}"

buildscript {
    dependencies {
        classpath(libs.proguard.gradle)
    }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.jsonschema2pojo)
    `maven-publish`
    signing
}

dependencies {
    implementation(libs.java.websocket)
    implementation(libs.picocli)
    implementation(libs.jansi)
    implementation(libs.gson)
    implementation(libs.slf4j.api)

    testImplementation(testLibs.kotest.junit5)
    testImplementation(testLibs.kotest.datatest)
    testImplementation(testLibs.mockk)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withJavadocJar() // required for uploading to Sonatype
    withSourcesJar()
}

jsonSchema2Pojo {
    setSourceType(SourceType.YAMLSCHEMA.toString())
    setSource(singletonList(layout.projectDirectory.dir("../schema/schemas").asFile))
    setAnnotationStyle(AnnotationStyle.GSON.toString())
    targetPackage = schemaPackage
    targetDirectory = layout.buildDirectory.dir("classes/java/main").get().asFile
    setFileExtensions("schema.yaml", "schema.json")
}

tasks {
    compileKotlin {
        dependsOn(generateJsonSchema2Pojo)
    }

    test {
        useJUnitPlatform()
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        manifest {
            attributes["Main-Class"] = jarManifestMainClass
            attributes["Implementation-Title"] = title
            attributes["Implementation-Version"] = archiveVersion
            attributes["Implementation-Vendor"] = "robocode.dev"
            attributes["Package"] = project.group
        }
        archiveClassifier.set("all") // the final archive will not have this classifier

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    val runJar by registering(JavaExec::class) {
        dependsOn(jar)
        classpath = files(jar)
    }

    val proguard by registering(ProGuardTask::class) { // used for compacting and code-shaking
        dependsOn(jar)

        configuration("proguard-rules.pro")

        injars("$baseArchiveName-all.jar")
        outjars("$baseArchiveName.jar")
    }

    assemble {
        dependsOn(proguard)
        doLast {
            delete("$baseArchiveName-all.jar")
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

    publishing {
        publications {
            create<MavenPublication>("server") {
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
    sign(publishing.publications["server"])
}