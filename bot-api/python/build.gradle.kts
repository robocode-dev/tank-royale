plugins {
    base
}

// Resolve venv Python path (creates OS-specific path under .venv)
fun venvPythonPath(): String {
    val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows
    val venvDir = project.layout.projectDirectory.dir(".venv").asFile
    val py = if (isWindows) venvDir.resolve("Scripts/python.exe") else venvDir.resolve("bin/python")
    return py.absolutePath
}

// Task to ensure a local virtual environment exists and has base requirements installed
val setupVenv by tasks.registering(Exec::class) {
    group = "python"
    description = "Creates local Python virtual environment and installs base requirements"
    val os = org.gradle.internal.os.OperatingSystem.current()
    if (os.isWindows) {
        commandLine("powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", "scripts/create-venv.ps1")
    } else {
        commandLine("bash", "scripts/create-venv.sh")
    }
}

tasks {
    named("clean") {
        doLast {
            delete(
                "generated",
                "robocode_tank_royale.egg-info",
            )
        }
    }

    val `install-requirements` by registering(Exec::class) {
        dependsOn(setupVenv)
        commandLine(venvPythonPath(), "-m", "pip", "install", "-r", "requirements.txt")
    }

    val `generate-schema` by registering(Exec::class) {
        dependsOn(`install-requirements`)

        dependsOn(setupVenv)
        commandLine(venvPythonPath(), "scripts/schema_to_python.py", "-d", "../../schema/schemas", "-o", "generated/robocode_tank_royale/schema")
    }

    val `generate-version` by registering {
        inputs.file("../../gradle.properties")
        outputs.file("VERSION")
        doLast {
            val propsFile = file("../../gradle.properties")
            if (!propsFile.exists()) {
                throw GradleException("version properties file not found: ${propsFile.absolutePath}")
            }
            val versionLine = propsFile.readLines().firstOrNull { it.trim().startsWith("version=") }
                ?: throw GradleException("No 'version=' entry found in ${propsFile.absolutePath}")
            val ver = versionLine.substringAfter("version=").trim()
            file("VERSION").writeText(ver)
            println("Wrote VERSION file with version $ver")
        }
    }

    val `pip-install` by registering(Exec::class) {
        dependsOn(`generate-schema`)
        dependsOn(`generate-version`)

        dependsOn(setupVenv)
        commandLine(venvPythonPath(), "-m", "pip", "install", "-e", ".")
    }

    val `pip-install-test-requirements` by registering(Exec::class) {
        dependsOn(setupVenv)
        commandLine(venvPythonPath(), "-m", "pip", "install", "-r", "requirements-test.txt")
    }

    // run pytest
    val test by registering(Exec::class) {
        group = "verification"
        description = "Runs Python tests with pytest"
        dependsOn(`pip-install`)
        dependsOn(`pip-install-test-requirements`)
        dependsOn(setupVenv)
        commandLine(venvPythonPath(), "-m", "pytest")
    }

    // make it part of the standard verification lifecycle
    named("check") {
        dependsOn(test)
    }

    // Install Python build tooling (build + wheel)
    val `install-build-tools` by registering(Exec::class) {
        group = "build"
        description = "Installs Python build tooling"
        dependsOn(setupVenv)
        commandLine(venvPythonPath(), "-m", "pip", "install", "build", "wheel")
    }

    // Build distributable artifacts (wheel + sdist)
    val `build-dist` by registering(Exec::class) {
        group = "build"
        description = "Builds wheel and sdist into dist/ using PEP 517"
        dependsOn(`generate-schema`)
        dependsOn(`generate-version`)
        dependsOn(`install-build-tools`)
        commandLine(venvPythonPath(), "-m", "build")
    }

    named("build") {
        dependsOn(`pip-install`)
        dependsOn(`build-dist`)
    }

    // ---------------------------- Documentation (Sphinx) ----------------------------

    val `install-sphinx` by registering(Exec::class) {
        group = "documentation"
        description = "Installs Sphinx for building Python API docs"
        // Also install sphinxawesome-theme so the sphinxawesome_theme is available
        // during sphinx-build: pip install sphinx sphinxawesome-theme
        dependsOn(setupVenv)
        commandLine(venvPythonPath(), "-m", "pip", "install", "sphinx", "sphinxawesome-theme")
    }

    val prepareSphinxSource by registering(Copy::class) {
        group = "documentation"
        description = "Prepares Sphinx source directory"
        val srcDir = file("sphinx")
        val outDir = layout.buildDirectory.dir("sphinx/source").get().asFile
        doFirst { delete(outDir) }
        from(srcDir)
        into(outDir)
    }

    val `sphinx-apidoc` by registering(Exec::class) {
        group = "documentation"
        description = "Generates Sphinx reStructuredText files from Python modules"
        dependsOn(prepareSphinxSource)
        dependsOn(`install-sphinx`)
        // Output API docs alongside the prepared Sphinx source under build/
        val outApiDir = layout.buildDirectory.dir("sphinx/source/api").get().asFile.path
        commandLine(
            venvPythonPath(), "-m", "sphinx.ext.apidoc",
            "-o", outApiDir,
            "src/robocode_tank_royale"
        )
    }

    val `sphinx-build` by registering(Exec::class) {
        group = "documentation"
        description = "Builds HTML documentation with Sphinx"
        dependsOn(`sphinx-apidoc`)
        // Ensure package is importable for autodoc
        dependsOn(`pip-install`)
        val sourceDir = layout.buildDirectory.dir("sphinx/source").get().asFile.path
        val htmlDir = layout.buildDirectory.dir("sphinx/html").get().asFile.path
        commandLine(venvPythonPath(), "-m", "sphinx", "-b", "html", sourceDir, htmlDir)
    }

    register<Copy>("copyPythonApiDocs") {
        group = "documentation"
        description = "Copies generated Python API docs into the /docs folder"
        dependsOn(`sphinx-build`)

        // Only copy docs when explicitly asked for via release/docs tasks or this task itself
        onlyIf {
            gradle.startParameter.taskNames.any {
                it.contains("build-release") ||
                it.contains("upload-docs") ||
                it.contains("create-release") ||
                it == "copyPythonApiDocs" ||
                it.endsWith(":copyPythonApiDocs")
            }
        }

        val pythonApiDir = layout.projectDirectory.dir("../../docs/api/python")

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from(layout.buildDirectory.dir("sphinx/html"))
        into(pythonApiDir)

        doFirst {
            // Clean target directory only when task actually runs
            delete(pythonApiDir)
            mkdir(pythonApiDir)
        }
    }

    // Make sure documentation tasks are not part of the build task
    afterEvaluate {
        tasks.named("build").configure {
            setDependsOn(dependsOn.filterNot {
                it.toString().contains("sphinx") || it.toString().contains("copyPythonApiDocs")
            })
        }
    }
}
