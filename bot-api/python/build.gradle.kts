import build.isWindows

plugins {
    base
}

// Resolve venv Python path (creates OS-specific path under .venv)
fun venvPythonPath(): String {
    val venvDir = project.layout.projectDirectory.dir(".venv").asFile
    val py = if (isWindows()) venvDir.resolve("Scripts/python.exe") else venvDir.resolve("bin/python")
    return py.absolutePath
}

// Task to ensure a local virtual environment exists and has base requirements installed
val setupVenv by tasks.registering(Exec::class) {
    group = "python"
    description = "Creates local Python virtual environment and installs base requirements"
    if (isWindows()) {
        commandLine("powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", "scripts/create-venv.ps1")
    } else {
        commandLine("bash", "scripts/create-venv.sh")
        workingDir = file(projectDir)
    }
}

tasks {
    named("clean") {
        doLast {
            delete(
                ".venv",
                "dist",
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

        commandLine(
            venvPythonPath(),
            "scripts/schema_to_python.py",
            "-d",
            "../../schema/schemas",
            "-o",
            "generated/robocode_tank_royale/schema"
        )
    }

    val `generate-version` by registering {
        group = "build"
        description = "Generates VERSION file from root gradle.properties"
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
        commandLine(venvPythonPath(), "-m", "pip", "install", "build", "wheel", "twine")
    }

    // Build distributable artifacts (wheel + sdist)
    val `build-dist` by registering(Exec::class) {
        group = "build"
        description = "Builds wheel and sdist into dist/ using PEP 517"
        dependsOn(`generate-schema`)
        dependsOn(`generate-version`)
        dependsOn(`install-build-tools`)
        commandLine(venvPythonPath(), "-m", "build")
        finalizedBy("verify-dist")
    }

    // Verify distributable artifacts with twine
    val `verify-dist` by registering(Exec::class) {
        group = "build"
        description = "Verifies built distributions with twine"
        dependsOn(`install-build-tools`)
        doFirst {
            val distDir = file("dist")
            if (!distDir.exists()) {
                throw GradleException("Distribution directory not found: ${distDir.absolutePath}")
            }
            val files = distDir.listFiles { f -> f.isFile && (f.name.endsWith(".whl") || f.name.endsWith(".tar.gz")) }
                ?.map { it.absolutePath } ?: emptyList()
            if (files.isEmpty()) {
                throw GradleException("No distribution files found in ${distDir.absolutePath}")
            }
            commandLine(venvPythonPath(), "-m", "twine", "check", *files.toTypedArray())
        }
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

        // Only copy docs when explicitly asked for via upload-docs task or this task itself
        onlyIf {
            gradle.startParameter.taskNames.any {
                it.contains("upload-docs") ||
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

    // Upload built wheel to TestPyPI using twine
    val `upload-testpypi` by registering(Exec::class) {
        group = "publishing"
        description =
            "Uploads the built wheel to TestPyPI using twine. Requires TWINE_USERNAME and TWINE_PASSWORD, a TestPyPI token (-PtestpypiToken / TESTPYPI_API_TOKEN), or a configured .pypirc"
        dependsOn(`install-build-tools`)
        // Ensure artifacts are built before attempting upload
        dependsOn(`build-dist`)
        doFirst {
            val distDir = file("dist")
            if (!distDir.exists()) {
                throw GradleException("Distribution directory not found: ${distDir.absolutePath}")
            }
            val wheels =
                distDir.listFiles { f -> f.isFile && f.name.endsWith(".whl") }?.map { it.absolutePath } ?: emptyList()
            if (wheels.isEmpty()) {
                throw GradleException("No wheel files found in ${distDir.absolutePath}. Run the build-dist task first.")
            }

            // Resolve credentials for non-interactive twine upload
            fun prop(name: String): String? =
                if (project.hasProperty(name)) project.property(name)?.toString() else null

            val usernameFromProp = prop("twineUsername")
            val passwordFromProp = prop("twinePassword")
            val tokenFromProp = prop("testpypiToken")

            val envMap = System.getenv()
            val usernameFromEnv = envMap["TWINE_USERNAME"]
            val passwordFromEnv = envMap["TWINE_PASSWORD"]
            val tokenFromEnv = envMap["TESTPYPI_API_TOKEN"] ?: envMap["PYPI_TOKEN"] ?: envMap["TEST_PYPI_API_TOKEN"]

            var username = usernameFromProp ?: usernameFromEnv
            var password = passwordFromProp ?: passwordFromEnv

            // If only a token is provided, use TestPyPI token semantics
            if ((username == null || password == null) && !tokenFromProp.isNullOrBlank()) {
                username = "__token__"
                password = tokenFromProp
            } else if ((username == null || password == null) && !tokenFromEnv.isNullOrBlank()) {
                username = "__token__"
                password = tokenFromEnv
            }

            if (username.isNullOrBlank() || password.isNullOrBlank()) {
                // Allow using ~/.pypirc when explicit credentials are not provided
                val home = System.getProperty("user.home")
                val pypirc = File(home, ".pypirc")
                if (!pypirc.exists()) {
                    throw GradleException(
                        "Twine credentials not found. Provide TWINE_USERNAME and TWINE_PASSWORD env vars, " +
                                "a TestPyPI token via -PtestpypiToken=<token> (or TESTPYPI_API_TOKEN / PYPI_TOKEN), " +
                                "or configure credentials in %USERPROFILE%/.pypirc (Windows) or ~/.pypirc (Unix). " +
                                "When using a token, the username must be __token__."
                    )
                }
            }

            // Only set environment variables when explicit credentials are supplied
            if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
                val exec = this as Exec
                exec.environment("TWINE_USERNAME", username)
                exec.environment("TWINE_PASSWORD", password)
            }

            // Use non-interactive mode so CI fails fast if credentials are missing
            commandLine(
                venvPythonPath(),
                "-m",
                "twine",
                "upload",
                "--non-interactive",
                "--repository",
                "testpypi",
                *wheels.toTypedArray()
            )
        }
    }

    // Upload built wheel to PyPI using twine
    val `upload-pypi` by registering(Exec::class) {
        group = "publishing"
        description =
            "Uploads the built wheel to the real PyPI repository using twine. Requires TWINE_USERNAME and TWINE_PASSWORD, a PyPI token (-PpypiToken / PYPI_TOKEN), or a configured .pypirc"
        dependsOn(`install-build-tools`)
        // Ensure artifacts are built before attempting upload
        dependsOn(`build-dist`)
        doFirst {
            val distDir = file("dist")
            if (!distDir.exists()) {
                throw GradleException("Distribution directory not found: ${distDir.absolutePath}")
            }
            val wheels =
                distDir.listFiles { f -> f.isFile && f.name.endsWith(".whl") }?.map { it.absolutePath } ?: emptyList()
            if (wheels.isEmpty()) {
                throw GradleException("No wheel files found in ${distDir.absolutePath}. Run the build-dist task first.")
            }

            // Resolve credentials for non-interactive twine upload
            fun prop(name: String): String? =
                if (project.hasProperty(name)) project.property(name)?.toString() else null

            val usernameFromProp = prop("twineUsername")
            val passwordFromProp = prop("twinePassword")
            val tokenFromProp = prop("pypiToken")

            val envMap = System.getenv()
            val usernameFromEnv = envMap["TWINE_USERNAME"]
            val passwordFromEnv = envMap["TWINE_PASSWORD"]
            val tokenFromEnv = envMap["PYPI_TOKEN"] ?: envMap["TEST_PYPI_API_TOKEN"]

            var username = usernameFromProp ?: usernameFromEnv
            var password = passwordFromProp ?: passwordFromEnv

            // If only a token is provided, use PyPI token semantics
            if ((username == null || password == null) && !tokenFromProp.isNullOrBlank()) {
                username = "__token__"
                password = tokenFromProp
            } else if ((username == null || password == null) && !tokenFromEnv.isNullOrBlank()) {
                username = "__token__"
                password = tokenFromEnv
            }

            if (username.isNullOrBlank() || password.isNullOrBlank()) {
                // Allow using ~/.pypirc when explicit credentials are not provided
                val home = System.getProperty("user.home")
                val pypirc = File(home, ".pypirc")
                if (!pypirc.exists()) {
                    throw GradleException(
                        "Twine credentials not found. Provide TWINE_USERNAME and TWINE_PASSWORD env vars, " +
                                "a PyPI token via -PpypiToken=<token> (or PYPI_TOKEN), or configure credentials in %USERPROFILE%/.pypirc (Windows) or ~/.pypirc (Unix). " +
                                "When using a token, the username must be __token__."
                    )
                }
            }

            // Only set environment variables when explicit credentials are supplied
            if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
                val exec = this as Exec
                exec.environment("TWINE_USERNAME", username)
                exec.environment("TWINE_PASSWORD", password)
            }

            // Use non-interactive mode so CI fails fast if credentials are missing
            commandLine(
                venvPythonPath(),
                "-m",
                "twine",
                "upload",
                "--non-interactive",
                "--verbose",
                "--repository",
                "pypi",
                *wheels.toTypedArray()
            )
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
