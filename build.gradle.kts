import build.release.createRelease
import build.release.dispatchWorkflow
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Copy
import org.gradle.api.Project

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
                jvmTarget = JvmTarget.JVM_11 // Java 11
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

// --- Centralized jpackage convention and task registration ---

fun Project.registerJpackageTasks(
    appName: String,
    packageName: String,
    mainJarPath: String,
    dependsOnTaskName: String = "proguard"
) {
    val jpackageExecutable: String by lazy {
        val javaHome = System.getenv("JAVA_HOME") ?: System.getProperty("java.home")
        val bin = if (org.gradle.internal.os.OperatingSystem.current().isWindows) "bin/jpackage.exe" else "bin/jpackage"
        val candidate = file("$javaHome/$bin")
        if (candidate.exists()) candidate.absolutePath else "jpackage"
    }

    val jlinkExecutable: String by lazy {
        val javaHome = System.getenv("JAVA_HOME") ?: System.getProperty("java.home")
        val bin = if (org.gradle.internal.os.OperatingSystem.current().isWindows) "bin/jlink.exe" else "bin/jlink"
        val candidate = file("$javaHome/$bin")
        if (candidate.exists()) candidate.absolutePath else "jlink"
    }

    val jpackageOutputDir = layout.buildDirectory.dir("jpackage").get().asFile
    val installersStageDir = rootProject.layout.buildDirectory.dir("dist/${project.name}").get().asFile
    val jlinkRuntimeDirWin = rootProject.layout.buildDirectory.dir("jlink/${project.name}-runtime-win").get().asFile

    fun Exec.configureCommonJpackageArgs(
        installerType: String,
        packageNameLocal: String,
        iconPath: String,
        mainClass: String,
        extra: List<String> = emptyList()
    ) {
        val inputDir = file(mainJarPath).parentFile
        val mainJarFile = file(mainJarPath).name
        // jpackage (especially on macOS) is strict about app-version format: digits and dots only
        val rawVersion = project.version.toString()
        val appVersionSanitized = rawVersion.replace(Regex("[^0-9.]"), ".").trim('.')

        // macOS requires CFBundleVersion of the form 1..3 integers and the first must be >= 1
        val isMac = org.gradle.internal.os.OperatingSystem.current().isMacOsX
        val macVersion = run {
            val parts = appVersionSanitized.split('.')
                .filter { it.isNotBlank() }
                .map { it.toIntOrNull() ?: 0 }
                .toMutableList()
            if (parts.isEmpty()) parts.add(1)
            if (parts[0] < 1) parts[0] = 1
            while (parts.size > 3) parts.removeLast()
            parts.joinToString(".")
        }
        val effectiveAppVersion = if (isMac) macVersion else appVersionSanitized

        val appDescription = project.description ?: "Robocode Tank Royale - ${project.name}"
        val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows

        executable = jpackageExecutable
        workingDir = project.projectDir
        args = listOf(
            "--type", installerType,
            "--name", packageNameLocal,
            "--app-version", effectiveAppVersion,
            "--vendor", "robocode.dev",
            "--input", inputDir.absolutePath,
            "--main-jar", mainJarFile,
            "--main-class", mainClass,
            "--icon", file(iconPath).absolutePath,
            "--dest", jpackageOutputDir.absolutePath,
            "--license-file", rootProject.file("LICENSE").absolutePath
        ) +
                (if (isWindows) listOf(
            "--description", appDescription,
            "--win-menu",
            "--win-shortcut",
            "--win-menu-group", "Robocode Tank Royale"
                ) else emptyList()) +
                extra
    }

    // Resolve icons from repo gfx assets (actual locations)
    val iconWin = rootProject.file("gfx/Tank/Tank.ico").absolutePath
    val iconLinux = rootProject.file("gfx/Tank/Tank.png").absolutePath
    val iconMac = rootProject.file("gfx/Tank/Tank.icns").absolutePath

    // Try to wire up dependency if task exists
    val dependsOnProvider = runCatching { tasks.named(dependsOnTaskName) }.getOrNull()

    tasks {
        // Build a trimmed runtime image for Windows to avoid "Failed to launch VM" issues on some machines.
        // This keeps behavior stable and deterministic by not relying on auto-detected runtimes.
        register<Exec>("jlinkRuntimeWin") {
            group = "distribution"
            description = "Create trimmed runtime image for Windows using jlink"
            onlyIf { org.gradle.internal.os.OperatingSystem.current().isWindows }
            // jlink requires that the output directory does NOT already exist.
            // Ensure a clean state before executing jlink.
            doFirst {
                if (jlinkRuntimeDirWin.exists()) {
                    project.delete(jlinkRuntimeDirWin)
                }
            }
            executable = jlinkExecutable
            // Module set chosen for Swing/Kotlin desktop apps; extend if logs indicate more are needed
            // Common extras: java.xml, java.datatransfer, java.prefs, java.net.http, jdk.crypto.ec
            val modules = listOf(
                "java.base",
                "java.desktop",
                "java.logging",
                "java.scripting",
                "java.xml",
                "java.datatransfer",
                "java.prefs",
                "java.net.http",
                "jdk.crypto.ec"
            ).joinToString(",")
            args = listOf(
                "--add-modules", modules,
                "--no-header-files",
                "--no-man-pages",
                "--strip-debug",
                "--compress", "2",
                "--output", jlinkRuntimeDirWin.absolutePath
            )
        }

        register<Exec>("jpackageWin") {
            group = "distribution"
            description = "Create Windows installer (MSI) using jpackage"
            dependsOnProvider?.let { dependsOn(it) }
            // Ensure our explicit runtime image exists before packaging to avoid VM launch issues
            dependsOn("jlinkRuntimeWin")
            onlyIf { org.gradle.internal.os.OperatingSystem.current().isWindows }
            doFirst { jpackageOutputDir.mkdirs() }
            val winExtra = buildList {
                // Use the explicit runtime image built by jlink
                addAll(listOf("--runtime-image", jlinkRuntimeDirWin.absolutePath))
                // Allow turning on a console for diagnostics with -PwinConsole=true
                if (project.findProperty("winConsole") == "true") add("--win-console")
            }
            configureCommonJpackageArgs(
                installerType = "msi",
                packageNameLocal = packageName,
                iconPath = iconWin,
                mainClass = (project.extra["jpackageMainClass"] as String),
                extra = winExtra
            )
        }

        // Linux: DEB
        register<Exec>("jpackageLinuxDeb") {
            group = "distribution"
            description = "Create Linux DEB installer using jpackage"
            dependsOnProvider?.let { dependsOn(it) }
            onlyIf { org.gradle.internal.os.OperatingSystem.current().isLinux }
            doFirst { jpackageOutputDir.mkdirs() }
            configureCommonJpackageArgs(
                installerType = "deb",
                packageNameLocal = packageName,
                iconPath = iconLinux,
                mainClass = (project.extra["jpackageMainClass"] as String),
                extra = listOf(
                    "--linux-deb-maintainer", "Flemming N. Larsen <flemming.n.larsen@gmail.com>"
                )
            )
        }

        // Linux: RPM
        register<Exec>("jpackageLinuxRpm") {
            group = "distribution"
            description = "Create Linux RPM installer using jpackage"
            dependsOnProvider?.let { dependsOn(it) }
            onlyIf { org.gradle.internal.os.OperatingSystem.current().isLinux }
            doFirst { jpackageOutputDir.mkdirs() }
            configureCommonJpackageArgs(
                installerType = "rpm",
                packageNameLocal = packageName,
                iconPath = iconLinux,
                mainClass = (project.extra["jpackageMainClass"] as String)
            )
        }

        // Backward-compatible aggregate Linux task
        register("jpackageLinux") {
            group = "distribution"
            description = "Create Linux installers (DEB and RPM) using jpackage"
            onlyIf { org.gradle.internal.os.OperatingSystem.current().isLinux }
            dependsOn("jpackageLinuxDeb", "jpackageLinuxRpm")
        }

        register<Exec>("jpackageMac") {
            group = "distribution"
            description = "Create macOS installer (DMG) using jpackage"
            dependsOnProvider?.let { dependsOn(it) }
            onlyIf { org.gradle.internal.os.OperatingSystem.current().isMacOsX }
            doFirst { jpackageOutputDir.mkdirs() }
            // Add macOS-specific flags for better diagnostics and stable naming
            configureCommonJpackageArgs(
                installerType = "dmg",
                packageNameLocal = packageName,
                iconPath = iconMac,
                mainClass = (project.extra["jpackageMainClass"] as String),
                extra = listOf(
                    "--mac-package-name", appName,
                    "--mac-package-identifier", "dev.robocode.tankroyale.${project.name}",
                    "--mac-dmg-volume-name", appName
                ) + if ((project.findProperty("ciVerbose") == "true")) listOf("--verbose") else emptyList()
            )
            // Print effective jpackage args just before execution for easier debugging on CI
            doFirst {
                if (project.findProperty("ciVerbose") == "true") {
                    println("[jpackageMac] App: $appName  Identifier: dev.robocode.tankroyale.${project.name}")
                    println("[jpackageMac] Icon exists: ${file(iconMac).exists()} → ${file(iconMac).absolutePath}")
                    println("[jpackageMac] Main JAR: ${file(mainJarPath).name} in ${file(mainJarPath).parentFile.absolutePath}")
                }
            }
        }

        // Alternative macOS package: PKG (often more reliable on CI than DMG)
        register<Exec>("jpackageMacPkg") {
            group = "distribution"
            description = "Create macOS installer (PKG) using jpackage"
            dependsOnProvider?.let { dependsOn(it) }
            onlyIf { org.gradle.internal.os.OperatingSystem.current().isMacOsX }
            doFirst { jpackageOutputDir.mkdirs() }
            configureCommonJpackageArgs(
                installerType = "pkg",
                packageNameLocal = packageName,
                iconPath = iconMac,
                mainClass = (project.extra["jpackageMainClass"] as String),
                extra = listOf(
                    "--mac-package-name", appName,
                    "--mac-package-identifier", "dev.robocode.tankroyale.${project.name}"
                ) + if ((project.findProperty("ciVerbose") == "true")) listOf("--verbose") else emptyList()
            )
            doFirst {
                if (project.findProperty("ciVerbose") == "true") {
                    println("[jpackageMacPkg] App: $appName  Identifier: dev.robocode.tankroyale.${project.name}")
                    println("[jpackageMacPkg] Icon exists: ${file(iconMac).exists()} → ${file(iconMac).absolutePath}")
                    println("[jpackageMacPkg] Main JAR: ${file(mainJarPath).name} in ${file(mainJarPath).parentFile.absolutePath}")
                }
            }
        }

        register<Copy>("stageInstallers") {
            group = "distribution"
            description = "Copy generated installers to root build/dist/${project.name}"
            // Prefer PKG on macOS to avoid occasional DMG hangs in CI
            dependsOn("jpackageWin", "jpackageLinuxDeb", "jpackageLinuxRpm", "jpackageMacPkg")
            from(jpackageOutputDir)
            include("*.msi", "*.exe", "*.deb", "*.rpm", "*.pkg", "*.dmg")
            into(installersStageDir)
        }
    }
}

subprojects {
    // Opt-in signal from subprojects via extra properties to avoid repeating task definitions
    afterEvaluate {
        val extras = extensions.extraProperties
        val enabled = extras.has("useJpackage") && (extras["useJpackage"] as? Boolean == true)
        if (enabled) {
            val appName = (extras["jpackageAppName"] as? String) ?: project.name
            val packageName = (extras["jpackagePackageName"] as? String) ?: appName.lowercase().replace(" ", "-")
            val mainJar = (extras["jpackageMainJar"] as? String)
            val dependsOn = (extras["jpackageDependsOn"] as? String) ?: "proguard"
            if (mainJar == null) {
                logger.warn("jpackage enabled for ${project.path}, but 'jpackageMainJar' not provided – skipping task registration")
            } else {
                registerJpackageTasks(appName, packageName, mainJar, dependsOn)
            }
        }
    }
}

// Aggregate task at root to stage installers from all enabled subprojects
val stageAllInstallers = tasks.register("stageAllInstallers") {
    group = "distribution"
    description = "Produce and stage installers for all enabled subprojects on this OS"
}

gradle.projectsEvaluated {
    subprojects.forEach { p ->
        val t = p.tasks.findByName("stageInstallers")
        if (t != null) {
            stageAllInstallers.configure { dependsOn(t) }
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

            // Optionally trigger the GitHub Actions workflow that builds native installers via jpackage
            val trigger = (findProperty("triggerPackageReleaseWorkflow")?.toString()?.toBoolean() ?: false)
            if (trigger) {
                // Run package workflow against the main branch by default and pass the release version
                dispatchWorkflow(
                    token = tankRoyaleGitHubToken!!,
                    workflowFileName = "package-release.yml",
                    ref = "main",
                    inputs = mapOf("version" to version)
                )
            } else {
                println("Skipping package-release workflow dispatch. Enable with -PtriggerPackageReleaseWorkflow=true")
            }
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
