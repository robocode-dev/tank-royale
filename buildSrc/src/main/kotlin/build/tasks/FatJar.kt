package build.tasks

import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import javax.inject.Inject

abstract class FatJar : Jar() {
    private val jarManifestVendor = "robocode.dev"

    @get:Input
    abstract val title: Property<String>

    @get:Input
    @get:Optional
    abstract val mainClass: Property<String>

    @get:Input
    @get:Optional
    abstract val outputFilename: Property<String>

    @get:Inject
    abstract val archiveOperations: ArchiveOperations

    // Declare configurations as inputs (accessed at configuration time — fine)
    @get:InputFiles
    protected val compileClasspath = project.configurations.getByName("compileClasspath")

    @get:InputFiles
    protected val runtimeClasspath = project.configurations.getByName("runtimeClasspath")

    init {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        // Register source directories at configuration time (project access at config time is fine)
        from(
            project.files("build/classes/kotlin/main"),
            project.files("build/classes/java/main"), // Bot API is written in Java
            project.files("build/resources/main"),
        )
    }

    @TaskAction
    override fun copy() {
        if (outputFilename.isPresent) {
            archiveFileName.set(outputFilename)
        }

        manifest {
            it.attributes(mapOf(
                "Implementation-Title" to title.get(),
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to jarManifestVendor
            ))
            if (mainClass.isPresent) {
                it.attributes["Main-Class"] = mainClass.get()
            }
        }

        // Use injected ArchiveOperations instead of project.zipTree() to avoid project access at execution time
        from(
            compileClasspath.filter { it.name.endsWith(".jar") }.map { archiveOperations.zipTree(it) },
            runtimeClasspath.filter { it.name.endsWith(".jar") }.map { archiveOperations.zipTree(it) },
        )
        exclude("*.kotlin_metadata")

        super.copy() // important to call the original action
    }
}
