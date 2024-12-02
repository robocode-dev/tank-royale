description = "Robocode Tank Royale schema for .Net"

tasks {
    val clean by registering(Exec::class) {
        commandLine("dotnet", "clean")
    }

    val dotnetBuild by registering(Exec::class) {
        commandLine("dotnet", "build", "--configuration", "Release")
    }

    val build by registering(Exec::class) {
        dependsOn(dotnetBuild)

        doFirst {
            mkdir("${project(":bot-api:dotnet").file("Robocode.TankRoyale.BotApi/src/generated/Game")}")
        }

        var codeGeneratorPath = "$projectDir/bin/Release/net6.0/CodeGeneratorApp"
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            codeGeneratorPath += ".exe"
        }

        println("codeGeneratorPath: $codeGeneratorPath")
        println("${project(":schema").file("schemas")}")
        println("${project(":bot-api:dotnet").file("Robocode.TankRoyale.BotApi/src/generated/Game")}")

        commandLine(
            codeGeneratorPath,
            "${project(":schema").file("schemas/game")}",
            "${project(":bot-api:dotnet").file("Robocode.TankRoyale.BotApi/src/generated/Game")}",
            "Robocode.TankRoyale.Schema.Game"
        )
    }
}
