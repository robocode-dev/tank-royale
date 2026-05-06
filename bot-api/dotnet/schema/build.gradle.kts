description = "Robocode Tank Royale schema for .NET"

plugins {
    base
}

val inputSchemaDir = "${layout.projectDirectory}/../../../schema/schemas"
val generatedOutputDir = "${layout.projectDirectory}/generated"

tasks {
    val dotnetRestore by registering(Exec::class) {
        commandLine("dotnet", "restore")
        isIgnoreExitValue = true
    }

    val dotnetClean by registering(Exec::class) {
        dependsOn(dotnetRestore)
        commandLine("dotnet", "clean")
        // Ignore clean failures since project may be in bad state initially
        isIgnoreExitValue = true
    }

    val dotnetBuild by registering(Exec::class) {
        dependsOn(dotnetRestore)
        commandLine("dotnet", "build", "--configuration", "Release")
    }

    val generateSchema by registering(Exec::class) {
        dependsOn(dotnetBuild)

        doFirst {
            mkdir(generatedOutputDir)

            val releaseDir = "${layout.projectDirectory}/bin/Release"
            val codeGeneratorDll = file("$releaseDir/net8.0/CodeGeneratorApp.dll").absolutePath

            println("codeGeneratorDll: $codeGeneratorDll")
            println(inputSchemaDir)
            println(generatedOutputDir)

            commandLine(
                "dotnet",
                codeGeneratorDll,
                inputSchemaDir,
                generatedOutputDir,
                "Robocode.TankRoyale.Schema"
            )
        }
    }

    named("clean") {
        dependsOn(dotnetClean)

        doLast {
            delete("bin")
            delete("obj")
            delete("generated")
        }
    }

    named("build") {
        dependsOn(generateSchema)
    }
}
