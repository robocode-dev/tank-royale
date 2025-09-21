plugins {
    base
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
        commandLine("pip", "install", "-r", "requirements.txt")
    }

    val `generate-schema` by registering(Exec::class) {
        dependsOn(`install-requirements`)

        commandLine("python", "scripts/schema_to_python.py", "-d", "../../schema/schemas", "-o", "generated/robocode_tank_royale/schema")
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

        commandLine("pip", "install", "-e", ".")
    }

    val `pip-install-test-requirements` by registering(Exec::class) {
        commandLine("pip", "install", "-r", "requirements-test.txt")
    }

    // run pytest
    val test by registering(Exec::class) {
        group = "verification"
        description = "Runs Python tests with pytest"
        dependsOn(`pip-install`)
        dependsOn(`pip-install-test-requirements`)
        commandLine("python", "-m", "pytest")
    }

    // make it part of the standard verification lifecycle
    named("check") {
        dependsOn(test)
    }

    // Install Python build tooling (build + wheel)
    val `install-build-tools` by registering(Exec::class) {
        group = "build"
        description = "Installs Python build tooling"
        commandLine("python", "-m", "pip", "install", "build", "wheel")
    }

    // Build distributable artifacts (wheel + sdist)
    val `build-dist` by registering(Exec::class) {
        group = "build"
        description = "Builds wheel and sdist into dist/ using PEP 517"
        dependsOn(`generate-schema`)
        dependsOn(`generate-version`)
        dependsOn(`install-build-tools`)
        commandLine("python", "-m", "build")
    }

    named("build") {
        dependsOn(`pip-install`)
        dependsOn(`build-dist`)
    }
}