import java.nio.file.Files
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
    alias(libs.plugins.shadow)
    `maven-publish`
    signing
}

dependencies {
    implementation(libs.gson)
    implementation(libs.gson.extras)
    implementation(libs.nv.i18n)

    testImplementation(testLibs.bundles.junit)
    testImplementation(testLibs.assertj)
    testImplementation(testLibs.system.stubs)
    testImplementation(libs.java.websocket) // for mocked server
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }

    withJavadocJar()
    withSourcesJar()
}

jsonSchema2Pojo {
    val schemaDir = layout.projectDirectory.dir("../../schema/schemas").asFile
    if (!schemaDir.exists() || !schemaDir.isDirectory) {
        throw GradleException("Schema directory '${schemaDir.absolutePath}' does not exist or is not a directory.")
    }

    setSource(listOf(schemaDir))
    setSourceType(SourceType.YAMLSCHEMA.name)
    setAnnotationStyle(AnnotationStyle.GSON.name)
    setFileExtensions("schema.yaml", "schema.json")

    targetPackage = schemaPackage
    targetDirectory = layout.buildDirectory.dir("generated-sources/schema").get().asFile
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated-sources/schema"))
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        failFast = true
//        testLogging.showStandardStreams = true
    }

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
        archiveClassifier = ""
    }

    javadoc {
        title = "$javadocTitle $version"
        source(sourceSets.main.get().allJava)

        (options as StandardJavadocDocletOptions).apply {
            memberLevel = JavadocMemberLevel.PROTECTED
            overview = layout.projectDirectory.file("src/main/javadoc/overview.html").asFile.path

            charSet = "UTF-8"
            encoding = "UTF-8"
            docEncoding = "UTF-8"

            addFileOption("-add-stylesheet", layout.projectDirectory.file("src/main/javadoc/themes/prism.css").asFile)
            addBooleanOption("-allow-script-in-comments", true)
            addStringOption("Xdoclint:none", "-quiet")
            addStringOption("noqualifier", "all")
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

    register<Copy>("copyJavaApiDocs") {
        dependsOn(javadoc)

        val javadocDir = layout.projectDirectory.dir("../../docs/api/java")

        delete(javadocDir)
        mkdir(javadocDir)

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from(layout.buildDirectory.dir("docs/javadoc"))
        into(javadocDir)
    }

    // Make sure documentation tasks are not part of the build task
    afterEvaluate {
        tasks.named("build").configure {
            setDependsOn(dependsOn.filterNot {
                it.toString().contains("javadoc") || it.toString().contains("copyJavaApiDocs")
            })
        }
    }

    val javadocJar = named("javadocJar")
    val sourcesJar = named("sourcesJar") {
        dependsOn(compileJava)
    }

    publishing {
        publications {
            create<MavenPublication>("bot-api") {
                val outJars = shadowJar.get().outputs.files
                if (outJars.isEmpty) {
                    throw GradleException("Proguard did not produce output artifacts")
                }

                artifact(shadowJar)
                artifact(javadocJar)
                artifact(sourcesJar)

                groupId = group as String?
                artifactId = base.archivesName.get()
                version

                pom {
                    name = javadocTitle
                    description = project.description
                    url = "https://github.com/robocode-dev/tank-royale"

                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }
                    developers {
                        developer {
                            id = "fnl"
                            name = "Flemming Nørnberg Larsen"
                            url = "https://github.com/flemming-n-larsen"
                            organization = "robocode.dev"
                            organizationUrl = "https://robocode-dev.github.io/tank-royale/"
                        }
                    }
                    scm {
                        connection = "scm:git:git://github.com/robocode-dev/tank-royale.git"
                        developerConnection = "scm:git:ssh://github.com:robocode-dev/tank-royale.git"
                        url = "https://github.com/robocode-dev/tank-royale/tree/master"
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["bot-api"])
}