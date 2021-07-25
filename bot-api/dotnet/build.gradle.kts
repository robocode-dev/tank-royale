plugins {
    id("com.itiviti.dotnet") version "1.7.2"
}

dotnet {
//    verbosity = "detailed"

    build {
        version = "0.9.9"
        packageVersion = version
    }

    nugetPush {
        source = "local"
    }
}
