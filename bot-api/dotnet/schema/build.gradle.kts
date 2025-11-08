description = "Robocode Tank Royale schema for .NET"

plugins {
    base
}

val inputSchemaDir = "${layout.projectDirectory}/../../../schema/schemas"
val generatedOutputDir = "${layout.projectDirectory}/generated"

tasks {
    val dotnetClean by registering(Exec::class) {
        commandLine("dotnet", "clean")
    }

    val dotnetBuild by registering(Exec::class) {
        commandLine("dotnet", "build", "--configuration", "Release")
    }

    val generateSchema by registering(Exec::class) {
        dependsOn(dotnetBuild)

        doFirst {
            mkdir(generatedOutputDir)

            val releaseDir = "${layout.projectDirectory}/bin/Release"
            var codeGeneratorPath = file("$releaseDir/net8.0/CodeGeneratorApp").absolutePath
            if (System.getProperty("os.name").lowercase().contains("windows")) {
                codeGeneratorPath += ".exe"
            }

            println("codeGeneratorPath: $codeGeneratorPath")
            println(inputSchemaDir)
            println(generatedOutputDir)

            commandLine(
                codeGeneratorPath,
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
