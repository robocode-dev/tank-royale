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
        // Ensure artifact follows <base.archivesName>-<version>.jar
        archiveBaseName.set(base.archivesName)
        archiveClassifier = ""
    }

    javadoc {
        title = "$javadocTitle $version"
        source = fileTree("src/main/java") { include("**/*.java") }

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

        // Only copy docs when explicitly asked for via release/docs tasks or this task itself
        onlyIf {
            gradle.startParameter.taskNames.any {
                it.contains("build-release") ||
                it.contains("upload-docs") ||
                it.contains("create-release") ||
                it == "copyJavaApiDocs" ||
                it.endsWith(":copyJavaApiDocs")
            }
        }

        val javadocDir = layout.projectDirectory.dir("../../docs/api/java")

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from(layout.buildDirectory.dir("docs/javadoc"))
        into(javadocDir)

        doFirst {
            // Clean target directory only when task actually runs
            delete(javadocDir)
            mkdir(javadocDir)
        }
    }

    // Make sure javadoc is only generated when specifically requested
    javadoc {
        onlyIf {
            gradle.startParameter.taskNames.any {
                it.contains("build-release") ||
                it.contains("upload-docs") ||
                it.contains("create-release") ||
                it == "javadoc" ||
                it.endsWith(":javadoc")
            }
        }
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

    // Configure the maven publication to use the shadow jar as the main artifact
    publishing {
        publications {
            named<MavenPublication>("maven") {
                val outJars = shadowJar.get().outputs.files
                if (outJars.isEmpty) {
                    throw GradleException("Shadow jar did not produce output artifacts")
                }

                artifact(shadowJar)
                artifact(javadocJar)
                artifact(sourcesJar)

                // Override the name in the POM with the javadocTitle variable
                pom.name.set(javadocTitle)
            }
        }
    }
}