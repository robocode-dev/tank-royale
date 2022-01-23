package dev.robocode.tankroyale.archive

import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

abstract class FatJar : Jar() {

    @get:Input
    abstract val title: Property<String>

    @get:Input
    abstract val mainClass: Property<String>

    @get:Input
    @get:Optional
    abstract val outputFilename: Property<String?>

    private val compileClasspath = project.configurations.getByName("compileClasspath").resolve()
    private val runtimeClasspath = project.configurations.getByName("runtimeClasspath").resolve()

    init {
        outputFilename.set(archiveFileName)
    }

    @TaskAction
    fun action() {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        manifest {
            it.attributes["Implementation-Title"] = title.get()
            it.attributes["Implementation-Version"] = archiveVersion
            it.attributes["Main-Class"] = mainClass.get()
        }
        from(
            compileClasspath.filter { it.name.endsWith(".jar") }.map { project.zipTree(it) },
            runtimeClasspath.filter { it.name.endsWith(".jar") }.map { project.zipTree(it) }
        )
        exclude("*.kotlin_metadata")

        super.copy() // important to call the original action
    }
}