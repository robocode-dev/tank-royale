package build.tasks

import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

abstract class FatJar : Jar() {
    private val jarManifestVendor = "robocode.dev"

    @get:Input
    abstract val title: Property<String>

    @get:Input
    @get:Optional
    abstract val mainClass: Property<String>

    @get:Input
    @get:Optional
    abstract val outputFilename: Property<String?>

    // Declare configurations as inputs
    @get:InputFiles
    protected val compileClasspath = project.configurations.getByName("compileClasspath")

    @get:InputFiles
    protected val runtimeClasspath = project.configurations.getByName("runtimeClasspath")

    init {
        // Move configuration to init block
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    @TaskAction
    override fun copy() {  // Change to override copy() instead of custom taskAction
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

        from(
            project.files("build/classes/kotlin/main"),
            project.files("build/classes/java/main"), // Bot API is written in Java
            project.files("build/resources/main"),

            compileClasspath.filter { it.name.endsWith(".jar") }.map { project.zipTree(it) },
            runtimeClasspath.filter { it.name.endsWith(".jar") }.map { project.zipTree(it) },
        )
        exclude("*.kotlin_metadata")

        super.copy() // important to call the original action
    }
}