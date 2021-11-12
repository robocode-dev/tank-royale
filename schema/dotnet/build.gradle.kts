val dotnetClean = task<Exec>("dotnetClean") {
    commandLine("dotnet", "clean")
}

val dotnetBuild = task<Exec>("dotnetBuild") {
    commandLine("dotnet", "build", "--configuration", "Release")
}

val generateCode = task<Exec>("generateCode") {
    commandLine("$projectDir/bin/Release/net5.0/CodeGeneratorApp",
        "${project(":schema").file("schemas")}",
        "${project(":bot-api:dotnet").file("src/generated")}"
    )
}

tasks.register("build") {
    dependsOn(dotnetClean)
    dependsOn(dotnetBuild)
    dependsOn(generateCode)
}
