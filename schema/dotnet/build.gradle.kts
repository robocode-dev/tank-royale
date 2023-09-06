description = "Robocode Tank Royale schema for .Net"

tasks {
    register<Exec>("clean") {
        commandLine("dotnet", "clean")
    }

    val dotnetBuild by registering(Exec::class) {
        commandLine("dotnet", "build", "--configuration", "Release")
    }

    register<Exec>("build") {
        dependsOn(dotnetBuild)

        doFirst {
            mkdir("${project(":bot-api:dotnet").file("Robocode.TankRoyale.BotApi/src/generated")}")
        }

        var codeGeneratorPath = "$projectDir/bin/Release/net6.0/CodeGeneratorApp"
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            codeGeneratorPath += ".exe"
        }

        commandLine(
            codeGeneratorPath,
            "${project(":schema").file("schemas")}",
            "${project(":bot-api:dotnet").file("Robocode.TankRoyale.BotApi/src/generated")}"
        )
    }
}
