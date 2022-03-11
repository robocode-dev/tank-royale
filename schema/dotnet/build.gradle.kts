
tasks {
    register<Exec>("clean") {
        commandLine("dotnet", "clean")
    }

    val dotnetBuild by registering(Exec::class) {
        commandLine("dotnet", "build", "--configuration", "Release")
    }

    register<Exec>("build") {
        dependsOn(dotnetBuild)

        commandLine(
            "$projectDir/bin/Release/net5.0/CodeGeneratorApp",
            "${project(":schema").file("schemas")}",
            "${project(":bot-api:dotnet").file("bot-api/src/generated")}"
        )
    }
}
