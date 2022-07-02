
tasks {
    register<Exec>("clean") {
        commandLine("dotnet", "clean")
    }

    val dotnetBuild by registering(Exec::class) {
        commandLine("dotnet", "build", "--configuration", "Release")
    }

    register<Exec>("build") {
        dependsOn(dotnetBuild)

        mkdir("$projectDir/generated")

        commandLine(
            "$projectDir/bin/Release/net6.0/CodeGeneratorApp",
            "${project(":schema").file("schemas")}",
            "$projectDir/generated"
        )
    }
}
