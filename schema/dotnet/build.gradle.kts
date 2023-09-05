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

        commandLine(
            "$projectDir/bin/Release/net6.0/CodeGeneratorApp.exe",
            "${project(":schema").file("schemas")}",
            "${project(":bot-api:dotnet").file("Robocode.TankRoyale.BotApi/src/generated")}"
        )
    }
}
