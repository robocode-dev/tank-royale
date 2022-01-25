package dev.robocode.tankroyale.archive

import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import java.io.File

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

    private val compileClasspath = project.configurations.getByName("compileClasspath").resolve()
    private val runtimeClasspath = project.configurations.getByName("runtimeClasspath").resolve()

    @TaskAction
    fun action() {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        if (outputFilename.isPresent) {
            archiveFileName.set(outputFilename)
        }

        manifest {
            it.attributes["Implementation-Title"] = title.get()
            it.attributes["Implementation-Version"] = archiveVersion
            it.attributes["Implementation-Vendor"] = jarManifestVendor
            if (mainClass.isPresent) {
                it.attributes["Main-Class"] = mainClass.get()
            }
        }
        from(
            File("build/classes/kotlin/main"),
            File("build/resources/main"),
            compileClasspath.filter { it.name.endsWith(".jar") }.map { project.zipTree(it) },
            runtimeClasspath.filter { it.name.endsWith(".jar") }.map { project.zipTree(it) }
        )
        exclude("*.kotlin_metadata")

        super.copy() // important to call the original action
    }
}