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

        commandLine("python", "scripts/schema_to_python.py", "-d", "../../schema/schemas", "-o", "generated/robocode_tank_royale/schema");
    }

    val `pip-install` by registering(Exec::class) {
        dependsOn(`generate-schema`)

        commandLine("pip", "install", "-e", ".")
    }

    named("build") {
        dependsOn(`pip-install`)
    }
}