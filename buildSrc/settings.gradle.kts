rootProject.name = "buildSrc"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("json", "org.json:json:20240303")

            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").version("2.0.0")
        }
    }
}