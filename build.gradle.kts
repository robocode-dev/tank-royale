import build.release.createRelease
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

description = "Robocode: Build the best - destroy the rest!"

group = "dev.robocode.tankroyale"

val ossrhUsername: String? by project
val ossrhPassword: String? by project
val tankRoyaleGitHubToken: String? by project

// Validate JDK version for building
val javaVersion = JavaVersion.current()
val minJdkVersion = JavaVersion.VERSION_17
val maxJdkVersion = JavaVersion.VERSION_21

if (javaVersion < minJdkVersion) {
    throw GradleException(
        """
        ================================================================================
        ERROR: JDK version ${javaVersion} is too old for building Robocode Tank Royale.
        
        Required: JDK 17-21
        Current:  JDK ${javaVersion.majorVersion}
        
        Please install JDK 17 or 21:
        - Eclipse Temurin JDK 17: https://adoptium.net/temurin/releases/?version=17
        - Eclipse Temurin JDK 21: https://adoptium.net/temurin/releases/?version=21
        
        Note: End users only need Java 11+ to run Robocode, but developers need
        JDK 17-21 to build it.
        ================================================================================
        """.trimIndent()
    )
} else if (javaVersion > maxJdkVersion) {
    logger.warn(
        """
        ================================================================================
        WARNING: JDK version ${javaVersion} may cause issues with ProGuard.
        
        Recommended: JDK 17-21
        Current:     JDK ${javaVersion.majorVersion}
        
        If you encounter build errors, please switch to JDK 17 or 21:
        - Eclipse Temurin JDK 17: https://adoptium.net/temurin/releases/?version=17
        - Eclipse Temurin JDK 21: https://adoptium.net/temurin/releases/?version=21
        ================================================================================
        """.trimIndent()
    )
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.nexus.publish)

    // Publishing with signing
    `maven-publish`
    signing

    // Dependency management providing task: dependencyUpdates
    alias(libs.plugins.benmanes.versions)
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

subprojects {

    // Apply common Java configuration to all subprojects with a Java plugin
    plugins.withId("java") {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(11)) // Java 11
            }
            // required for publishing:
            withJavadocJar()
            withSourcesJar()
        }
    }

    // Set Java compile encoding to UTF-8 for all subprojects
    // Yes, it must be done!
    tasks.withType<JavaCompile> {
        options.release.set(11) // Java 11
        options.encoding = "UTF-8"
    }

    // Common publishing configuration for all subprojects with maven-publish plugin
    plugins.withId("maven-publish") {
        apply(plugin = "signing")
        publishing {
            publications {
                create<MavenPublication>("maven") {
                    // Set coordinates, resolve lazily to avoid order issues
                    groupId = "dev.robocode.tankroyale"
                    // Initial artifactId; we will re-apply after project is evaluated, see afterEvaluate below
                    artifactId = if (project.extensions.findByType<BasePluginExtension>() != null) {
                        project.extensions.getByType<BasePluginExtension>().archivesName.get()
                    } else {
                        project.name
                    }
                    version = project.version.toString()

                    pom {
                        name.set(project.name)
                        description.set(providers.provider {
                            val d = project.description?.trim()
                            if (!d.isNullOrEmpty()) d else "Robocode Tank Royale - ${project.name}"
                        })
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
                                url.set("https://github.com/flemming-n-larsen")
                                organization.set("robocode.dev")
                                organizationUrl.set("https://robocode-dev.github.io/tank-royale/")
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
        // Ensure coordinates are correct after all project configuration has been evaluated
        afterEvaluate {
            extensions.configure<PublishingExtension> {
                publications.withType<MavenPublication> {
                    groupId = "dev.robocode.tankroyale"
                    val baseExt = project.extensions.findByType<BasePluginExtension>()
                    artifactId = baseExt?.archivesName?.get() ?: project.name
                }
            }
        }
    }

    // Configure signing for all subprojects with signing plugin
    plugins.withId("signing") {
        signing {
            val signingKey: String? by project
            val signingPassword: String? by project

            // Add debug info to check if key exists
            if (signingKey.isNullOrBlank()) {
                logger.warn("Signing key is null or blank. Signing will not work.")
            } else {
                logger.info("Signing key found with length: ${signingKey?.length}")
            }

            if (!signingPassword.isNullOrBlank()) {
                logger.info("Signing password is present")
            }

            if (!signingKey.isNullOrBlank()) {
                useInMemoryPgpKeys(signingKey, signingPassword)
            } else {
                // Avoid calling useInMemoryPgpKeys with a null/blank key
                logger.info("Skipping useInMemoryPgpKeys because signing key is missing")
            }

            // Make signing required only when a key is provided
            isRequired = !signingKey.isNullOrBlank()

            if (isRequired) {
                sign(publishing.publications)
            }
        }
    }

    tasks {
        withType<KotlinJvmCompile>().configureEach {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_11 // Java 11.(
            }
        }

        // Make sure to replace $version token in version.txt when processing the resources
        withType<ProcessResources> {
            filesMatching("version.properties") {
                expand(mapOf("version" to version))
            }
        }
    }
}

val schemaReadmeFile = file("schema/schemas/README.md")

val generateSchemaDiagrams by tasks.registering {
    group = "documentation"
    description = "Regenerates Mermaid diagrams in schema/schemas/README.md (non-critical - won't fail build)"

    val readmeFile = schemaReadmeFile
    inputs.dir("schema/scripts/diagram-gen/src")
    inputs.file("schema/scripts/diagram-gen/build.gradle.kts")
    inputs.file("schema/scripts/diagram-gen/settings.gradle.kts")
    outputs.file(readmeFile)

    doLast {
        try {
            val gradlew = if (System.getProperty("os.name").lowercase().contains("win")) "gradlew.bat" else "gradlew"
            val process = ProcessBuilder(
                file(gradlew).absolutePath,
                "-p", "schema/scripts/diagram-gen",
                "-P", "schemaReadmePath=${readmeFile.absolutePath}",
                "updateSchemaReadme"
            )
                .directory(rootDir)
                .inheritIO()
                .start()

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                logger.lifecycle("Schema diagram generation completed successfully")
            } else {
                logger.warn("WARNING: Schema diagram generation failed with exit code $exitCode, but continuing build.")
                logger.warn("This is not critical for releases - diagrams will be updated manually if needed.")
            }
        } catch (e: Exception) {
            logger.warn("WARNING: Schema diagram generation failed with exception, but continuing build. Error: ${e.message}")
            logger.warn("This is not critical for releases - diagrams will be updated manually if needed.")
        }
    }
}

tasks {
    val docTasks = listOf(
        generateSchemaDiagrams.name,        // Update mermaid diagrams in schema/schemas/README.md
        "bot-api:dotnet:copyDotnetApiDocs", // Docfx documentation for .NET Bot API
        "bot-api:java:copyJavaApiDocs",     // Javadocs for Java Bot API
        "bot-api:python:copyPythonApiDocs"  // Sphinx documentation for Python Bot API
    )

    register("build-release") {
        description = "Builds a release"
        dependsOn(
            "bot-api:java:assemble",     // Bot API for Java VM
            "bot-api:dotnet:assemble",   // Bot API for .NET
            "booter:assemble",           // Booter (for booting up bots locally)
            "server:assemble",           // Server
            "gui:assemble",              // GUI
            "sample-bots:zip",           // Sample bots
        )
        finalizedBy(*docTasks.toTypedArray())
    }

    register("upload-docs") {
        description = "Generate and upload all documentation"
        dependsOn(*docTasks.toTypedArray())
    }

    register("create-release") {
        description = "Creates a release"
        dependsOn("build-release")
        dependsOn("upload-docs") // Make sure documentation is generated for releases

        doLast {
            val version = libs.versions.tankroyale.get()
            if (tankRoyaleGitHubToken.isNullOrBlank()) {
                throw IllegalStateException("'token' is null or blank meaning that it is missing")
            }
            createRelease(projectDir, version, tankRoyaleGitHubToken!!)
        }
    }
}

nexusPublishing {
    repositories.apply {
        sonatype {
            // Publishing By Using the Portal OSSRH Staging API:
            // https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))

            username.set(ossrhUsername)
            password.set(ossrhPassword)
        }
    }
}

val initializeSonatypeStagingRepository by tasks.existing
subprojects {
    initializeSonatypeStagingRepository {
        shouldRunAfter(tasks.withType<Sign>())
    }

    // Include Tank.ico in the published artifacts without cross-project output conflicts
    // We copy the icon into a subproject-local build directory, so each module signs its own copy.
    plugins.withId("maven-publish") {
        // Create a task per subproject that copies the shared icon into the module's buildDir
        val preparePublicationIcon = tasks.register<Copy>("preparePublicationIcon") {
            val srcIcon = file("${rootProject.projectDir}/gfx/Tank/Tank.ico")
            val destDir = layout.buildDirectory.dir("publication-resources/icon").get().asFile
            from(srcIcon)
            into(destDir)
            outputs.file(file("${destDir}/Tank.ico"))
        }

        configure<PublishingExtension> {
            publications.withType<MavenPublication> {
                val copiedIcon = layout.buildDirectory.file("publication-resources/icon/Tank.ico").get().asFile
                artifact(copiedIcon) {
                    builtBy(preparePublicationIcon)
                    classifier = "icon"
                    extension = "ico"
                }
            }
        }
    }
}
