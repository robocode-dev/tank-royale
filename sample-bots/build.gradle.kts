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

    // Provide a standardized `zip` task in each subproject
    tasks.register<Zip>("zip") {
        group = "distribution"
        description = "Packages ${project.name} sample bots as a zip"
        dependsOn("build")

        // Use a consistent archive file name across subprojects
        archiveFileName.set("sample-bots-${project.name}-${version}.zip")
        destinationDirectory.set(layout.buildDirectory)

        // Zip the prepared archive directory
        from(layout.buildDirectory.dir("archive"))
    }
}

tasks.register("zip") {
    description = "Builds the zip archives for all sample-bots subprojects"
    group = "distribution"
    // Depend explicitly on known zip tasks in subprojects
    dependsOn(
        ":sample-bots:csharp:zip",
        ":sample-bots:java:zip",
    )
}
