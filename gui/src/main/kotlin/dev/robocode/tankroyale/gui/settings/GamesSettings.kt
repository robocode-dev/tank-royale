package dev.robocode.tankroyale.gui.settings

import dev.robocode.tankroyale.client.model.GameSetup
import dev.robocode.tankroyale.client.model.IGameSetup
import dev.robocode.tankroyale.common.rules.GAME_TYPE_PRESETS
import dev.robocode.tankroyale.common.rules.GameTypePreset
import java.util.*

object GamesSettings : PropertiesStore("Robocode Games Setups", "game-setups.properties") {

    private fun GameTypePreset.toClientGameSetup() = GameSetup(
        gameType = gameType.displayName,
        arenaWidth = arenaWidth,
        isArenaWidthLocked = isArenaWidthLocked,
        arenaHeight = arenaHeight,
        isArenaHeightLocked = isArenaHeightLocked,
        minNumberOfParticipants = minNumberOfParticipants,
        isMinNumberOfParticipantsLocked = isMinNumberOfParticipantsLocked,
        maxNumberOfParticipants = maxNumberOfParticipants,
        isMaxNumberOfParticipantsLocked = isMaxNumberOfParticipantsLocked,
        numberOfRounds = numberOfRounds,
        isNumberOfRoundsLocked = isNumberOfRoundsLocked,
        gunCoolingRate = gunCoolingRate,
        isGunCoolingRateLocked = isGunCoolingRateLocked,
        maxInactivityTurns = maxInactivityTurns,
        isMaxInactivityTurnsLocked = isMaxInactivityTurnsLocked,
        turnTimeout = turnTimeoutMicros,
        isTurnTimeoutLocked = isTurnTimeoutLocked,
        readyTimeout = readyTimeoutMicros,
        isReadyTimeoutLocked = isReadyTimeoutLocked,
        defaultTurnsPerSecond = defaultTurnsPerSecond,
    )

    val defaultGameSetup: Map<String, GameSetup>
        get() = GAME_TYPE_PRESETS.values.associate { it.gameType.displayName to it.toClientGameSetup() }

    init {
        setProperties(defaultGameSetup)
        load()
    }

    private val gameSetups = HashMap<String, MutableGameSetup?>()

    init {
        for (propName in propertyNames()) {
            val strings = propName.split(".", limit = 2)
            val gameName = strings[0]
            val fieldName = strings[1]

            val value = load(propName) as String

            if (gameSetups[gameName] == null) {
                gameSetups[gameName] = defaultGameSetup[GameType.CUSTOM.displayName]?.toMutableGameSetup()
            }
            val gameType = games[gameName] as MutableGameSetup
            val theField = MutableGameSetup::class.java.getDeclaredField(fieldName)
            theField.isAccessible = true
            when (theField.type.name) {
                "boolean" -> theField.setBoolean(gameType, value.toBoolean())
                "int" -> theField.setInt(gameType, value.toInt())
                "double" -> theField.setDouble(gameType, value.toDouble())
                "java.lang.Integer" -> theField[gameType] = try {
                    Integer.parseInt(value)
                } catch (_: NumberFormatException) {
                    null
                }
                "java.lang.String" -> theField[gameType] = value
                else -> throw RuntimeException("Type is missing implementation: ${theField.type.name}")
            }
        }
    }

    val games: MutableMap<String, MutableGameSetup?>
        get() = Collections.unmodifiableMap(gameSetups)

    private fun setProperties(gameSetup: Map<String, IGameSetup?>) {
        for (key in gameSetup.keys) {
            val gameType = gameSetup[key]
            if (gameType != null) {
                putGameType(key, gameType)
            }
        }
    }

    private fun putGameType(name: String, gameSetup: IGameSetup) {
        val javaClass = (if (gameSetup is GameSetup) GameSetup::class else MutableGameSetup::class).java

        for (prop in javaClass.declaredFields) {
            if (prop.name == "Companion")
                continue

            val field = javaClass.getDeclaredField(prop.name)
            field.isAccessible = true
            var value = field[gameSetup]?.toString()
            if (value == null) {
                value = ""
            }
            set("$name.${prop.name}", value)
        }
    }

    override fun save() {
        setProperties(gameSetups)
        super.save()
    }
}