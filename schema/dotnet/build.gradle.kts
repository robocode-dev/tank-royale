description = "Robocode Tank Royale schema for .Net"

plugins {
    base
}

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
            mkdir("${project(":bot-api:dotnet").file("api/src/generated")}")
        }

        var codeGeneratorPath = "$projectDir/bin/Release/net6.0/CodeGeneratorApp"
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            codeGeneratorPath += ".exe"
        }

        println("codeGeneratorPath: $codeGeneratorPath")
        println("${project(":schema").file("schemas")}")
        println("${project(":bot-api:dotnet").file("api/src/generated")}")

        commandLine(
            codeGeneratorPath,
            "${project(":schema").file("schemas")}",
            "${project(":bot-api:dotnet").file("api/src/generated")}",
            "Robocode.TankRoyale.Schema"
        )
    }

    named("clean") {
        dependsOn(dotnetClean)
    }

    named("build") {
        dependsOn(generateSchema)
    }
}
