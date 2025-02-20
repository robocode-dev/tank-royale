plugins {
    base
}

tasks {
    named("clean") {
        doLast {
            delete(
                "python/build",
                "python/dist",
                "python/generated",
            )
        }
    }

    val pipInstallRequirements by registering(Exec::class) {
        commandLine("pip", "install", "-r", "requirements.txt")
    }

    val pipInstall by registering(Exec::class) {
        dependsOn(pipInstallRequirements)

        commandLine("pip", "install", "e", ".")
    }

    named("build") {
        dependsOn(pipInstall)
    }
}