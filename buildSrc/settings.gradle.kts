plugins {
  // Apply the foojay-resolver plugin to allow automatic download of JDKs
  id("org.gradle.toolchains.foojay-resolver-convention")
}

rootProject.name = "buildSrc"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
        create("testLibs") {
            from(files("../gradle/test-libs.versions.toml"))
        }
    }
}