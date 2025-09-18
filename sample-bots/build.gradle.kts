import org.gradle.api.tasks.bundling.Zip

description = "Robocode Tank Royale sample bots"

plugins {
    base // for clean/build lifecycle tasks
}

// Common configuration shared by all sample-bots subprojects
subprojects {
    // Align Zip tasks with shared defaults
    tasks.withType<Zip>().configureEach {
        destinationDirectory.set(layout.buildDirectory)
        filePermissions {
            user {
                read = true
                execute = true
            }
            other {
                execute = true
            }
        }
    }

    // Shared task to copy ReadMe.md from assets into the archive directory
    val copySampleBotsReadme by tasks.register<Copy>("copySampleBotsReadme") {
        val readmeFile = project.layout.projectDirectory.file("assets/ReadMe.md").asFile
        onlyIf { readmeFile.exists() }
        from(readmeFile)
        into(layout.buildDirectory.dir("archive"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    // Provide a standardized `zip` task in each subproject
    tasks.register<Zip>("zip") {
        group = "distribution"
        description = "Packages ${project.name} sample bots as a zip"
        dependsOn("build")
        dependsOn(copySampleBotsReadme)

        // Use a consistent archive file name across subprojects
        archiveFileName.set("sample-bots-${project.name}-${version}.zip")
        destinationDirectory.set(layout.buildDirectory)

        // Zip the prepared archive directory
        from(layout.buildDirectory.dir("archive"))
    }
}

tasks {
    named("clean") {
        dependsOn(subprojects.map { "${it.path}:clean" })
    }

    val zip = register("zip") {
        dependsOn(subprojects.map { "${it.path}:zip" })
    }

    named("build") {
        dependsOn(zip)
    }
}
