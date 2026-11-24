description = "Robocode Tank Royale Look"

group = "dev.robocode.tankroyale"

base {
    archivesName = "robocode-tankroyale-look" // renames _all_ archive names
}

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // The themes are FlatLaf look and feels; whoever sets them up uses FlatLaf too.
    api(libs.flatlaf)
}
