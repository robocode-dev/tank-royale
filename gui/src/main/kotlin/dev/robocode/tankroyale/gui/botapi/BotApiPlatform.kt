package dev.robocode.tankroyale.gui.botapi

import dev.robocode.tankroyale.common.util.Version

enum class BotApiPlatform(
    val displayName: String,
    val subDir: String,
    private val filenameRegex: Regex,
    val resourceName: String,
    private val newFileNameTemplate: String,
) {
    JAVA(
        displayName = "Java",
        subDir = "lib",
        filenameRegex = Regex("""robocode-tankroyale-bot-api-(.+)\.jar"""),
        resourceName = "robocode-tankroyale-bot-api-java.jar",
        newFileNameTemplate = "robocode-tankroyale-bot-api-{version}.jar",
    ),
    DOTNET(
        displayName = ".NET",
        subDir = "lib",
        filenameRegex = Regex("""Robocode\.TankRoyale\.BotApi\.(.+)\.nupkg"""),
        resourceName = "Robocode.TankRoyale.BotApi.nupkg",
        newFileNameTemplate = "Robocode.TankRoyale.BotApi.{version}.nupkg",
    ),
    PYTHON(
        displayName = "Python",
        subDir = "deps",
        filenameRegex = Regex("""robocode_tank_royale-(.+)-py3-none-any\.whl"""),
        resourceName = "robocode-tank-royale-bot-api-python.whl",
        newFileNameTemplate = "robocode_tank_royale-{version}-py3-none-any.whl",
    ),
    TYPESCRIPT(
        displayName = "TypeScript",
        subDir = "deps",
        filenameRegex = Regex("""robocode\.dev-tank-royale-bot-api-(.+)\.tgz"""),
        resourceName = "robocode-tank-royale-bot-api-typescript.tgz",
        newFileNameTemplate = "robocode.dev-tank-royale-bot-api-{version}.tgz",
    );

    fun newFileName(): String = newFileNameTemplate.replace("{version}", Version.version)

    fun extractVersion(filename: String): String? =
        filenameRegex.matchEntire(filename)?.groupValues?.get(1)
}
