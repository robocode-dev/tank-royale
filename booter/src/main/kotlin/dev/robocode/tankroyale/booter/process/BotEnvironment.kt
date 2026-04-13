package dev.robocode.tankroyale.booter.process

import dev.robocode.tankroyale.booter.model.BootEntry
import dev.robocode.tankroyale.booter.util.Env

/**
 * Sets up environment variables for a bot process from its boot entry and optional team context.
 */
internal object BotEnvironment {

    /**
     * Populates [envMap] with server connection details, required bot metadata,
     * optional bot metadata, and team properties when [team] is provided.
     */
    fun setup(envMap: MutableMap<String, String?>, bootEntry: BootEntry, team: Team? = null) {
        setServerProperties(envMap)
        setBotProperties(envMap, bootEntry)
        setOptionalBotProperties(envMap, bootEntry)
        team?.let {
            envMap["TEAM_ID"] = it.id.toString()
            envMap["TEAM_NAME"] = it.name
            envMap["TEAM_VERSION"] = it.version
        }
    }

    private fun setServerProperties(envMap: MutableMap<String, String?>) {
        System.getProperty("server.url")?.let { envMap[Env.SERVER_URL.name] = it }
        System.getProperty("server.secret")?.let { envMap[Env.SERVER_SECRET.name] = it }
    }

    private fun setBotProperties(envMap: MutableMap<String, String?>, bootEntry: BootEntry) {
        envMap[Env.BOT_BOOTED.name] = "true"
        envMap[Env.BOT_NAME.name] = bootEntry.name
        envMap[Env.BOT_VERSION.name] = bootEntry.version
        envMap[Env.BOT_AUTHORS.name] = bootEntry.authors.joinToString()
    }

    private fun setOptionalBotProperties(envMap: MutableMap<String, String?>, bootEntry: BootEntry) {
        bootEntry.gameTypes?.let { envMap[Env.BOT_GAME_TYPES.name] = it.joinToString() }
        bootEntry.description?.let { envMap[Env.BOT_DESCRIPTION.name] = it }
        bootEntry.homepage?.let { envMap[Env.BOT_HOMEPAGE.name] = it }
        bootEntry.countryCodes?.let { envMap[Env.BOT_COUNTRY_CODES.name] = it.joinToString() }
        bootEntry.platform?.let { envMap[Env.BOT_PLATFORM.name] = it }
        bootEntry.programmingLang?.let { envMap[Env.BOT_PROG_LANG.name] = it }
        bootEntry.initialPosition?.let { envMap[Env.BOT_INITIAL_POS.name] = it }
    }
}
