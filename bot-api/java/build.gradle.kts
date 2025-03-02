import java.nio.file.Files
import java.util.Collections.singletonList
import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.SourceType

description = "Robocode Tank Royale Bot API for Java"

val javadocTitle = "Robocode Tank Royale Bot API"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val schemaPackage = "$group.schema"

base {
    archivesName = "robocode-tankroyale-bot-api" // renames _all_ archive names
}

plugins {
    `java-library`
    alias(libs.plugins.jsonschema2pojo)
    alias(libs.plugins.shadow.jar)
    `maven-publish`
    signing
}

dependencies {
    implementation(libs.gson)
    implementation(libs.gson.extras)
    implementation(libs.nv.i18n)
    implementation(libs.batik.svggen)
    implementation(libs.batik.dom)

    testImplementation(testLibs.junit.api)
    testImplementation(testLibs.junit.params)
    testImplementation(testLibs.junit.engine)
    testImplementation(testLibs.assertj)
    testImplementation(testLibs.system.stubs)
    testImplementation(libs.java.websocket) // for mocked server
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withJavadocJar()
    withSourcesJar()
}

jsonSchema2Pojo {
    setSourceType(SourceType.YAMLSCHEMA.toString())
    setSource(singletonList(layout.projectDirectory.dir("../../schema/schemas").asFile))
    setAnnotationStyle(AnnotationStyle.GSON.toString())
    targetPackage = schemaPackage
    targetDirectory = layout.buildDirectory.dir("generated-sources/schema").get().asFile
    setFileExtensions("schema.yaml", "schema.json")
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated-sources/schema"))
        }
    }
}

tasks {
    jar {
        enabled = false
        dependsOn(shadowJar)
    }

    shadowJar {
        manifest {
            attributes["Implementation-Title"] = javadocTitle
            attributes["Implementation-Version"] = project.version
            attributes["Implementation-Vendor"] = "robocode.dev"
            attributes["Package"] = project.group
        }
        minimize()
        archiveClassifier.set("")
    }

    javadoc {
        title = "$javadocTitle $version"
        source(sourceSets.main.get().allJava)

        (options as StandardJavadocDocletOptions).apply {
            memberLevel = JavadocMemberLevel.PROTECTED
            overview = layout.projectDirectory.file("src/main/javadoc/overview.html").asFile.path

            addFileOption("-add-stylesheet", layout.projectDirectory.file("src/main/javadoc/themes/prism.css").asFile)
            addBooleanOption("-allow-script-in-comments", true)
            addStringOption("Xdoclint:none", "-quiet")
        }
        exclude(
            "**/dev/robocode/tankroyale/schema/**",
            "**/dev/robocode/tankroyale/botapi/internal/**",
            "**/dev/robocode/tankroyale/botapi/mapper/**",
            "**/dev/robocode/tankroyale/botapi/util/**",
        )
        doLast {
            val sourceFile = layout.projectDirectory.file("src/main/javadoc/prism.js").asFile.toPath()
            val targetFile = layout.buildDirectory.file("docs/javadoc/prism.js").get().asFile.toPath()
            Files.copy(sourceFile, targetFile)
        }
    }

    test {
        useJUnitPlatform()
        failFast = true
//        testLogging.showStandardStreams = true
    }

    val uploadDocs by registering(Copy::class) {
        dependsOn(javadoc)

        val javadocDir = layout.projectDirectory.dir("../../docs/api/java")

        delete(javadocDir)
        mkdir(javadocDir)

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from(layout.buildDirectory.dir("docs/javadoc"))
        into(javadocDir)
    }

    val javadocJar = named("javadocJar")
    val sourcesJar = named("sourcesJar") {
        dependsOn(compileJava)
    }

    publishing {
        publications {
            create<MavenPublication>("bot-api") {
                artifact(shadowJar)
                artifact(javadocJar)
                artifact(sourcesJar)

                groupId = group as String?
                artifactId = base.archivesName.get()
                version

                pom {
                    name.set(javadocTitle)
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
    sign(publishing.publications["bot-api"])
}