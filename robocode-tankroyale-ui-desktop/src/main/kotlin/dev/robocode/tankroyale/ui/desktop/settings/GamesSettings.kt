package dev.robocode.tankroyale.ui.desktop.settings

import dev.robocode.tankroyale.ui.desktop.model.GameSetup
import dev.robocode.tankroyale.ui.desktop.model.IGameSetup
import java.util.*
import kotlin.collections.HashMap

object GamesSettings : PropertiesStore("Robocode Games Config", "games.properties") {

    val defaultGameSetup: Map<String, GameSetup>
        get() = mapOf(
            GameType.CUSTOM.type to GameSetup(
                gameType = GameType.CUSTOM.type,
                arenaWidth = 800,
                isArenaWidthLocked = false,
                arenaHeight = 600,
                isArenaHeightLocked = false,
                minNumberOfParticipants = 2,
                isMinNumberOfParticipantsLocked = false,
                maxNumberOfParticipants = null,
                isMaxNumberOfParticipantsLocked = false,
                numberOfRounds = 10,
                isNumberOfRoundsLocked = false,
                gunCoolingRate = 0.1,
                isGunCoolingRateLocked = false,
                maxInactivityTurns = 450,
                isMaxInactivityTurnsLocked = false,
                turnTimeout = 30_000, // 30 milliseconds
                isTurnTimeoutLocked = false,
                readyTimeout = 1_000_000, // 1 second
                isReadyTimeoutLocked = false,
                defaultTurnsPerSecond = 30
            ),
            GameType.CLASSIC.type to GameSetup(
                gameType = GameType.CLASSIC.type,
                arenaWidth = 800,
                isArenaWidthLocked = true,
                arenaHeight = 600,
                isArenaHeightLocked = true,
                minNumberOfParticipants = 2,
                isMinNumberOfParticipantsLocked = true,
                maxNumberOfParticipants = null,
                isMaxNumberOfParticipantsLocked = true,
                numberOfRounds = 10,
                isNumberOfRoundsLocked = true,
                gunCoolingRate = 0.1,
                isGunCoolingRateLocked = true,
                maxInactivityTurns = 450,
                isMaxInactivityTurnsLocked = true,
                turnTimeout = 30_000, // 30 milliseconds
                isTurnTimeoutLocked = false,
                readyTimeout = 1_000_000, // 1 second
                isReadyTimeoutLocked = false,
                defaultTurnsPerSecond = 30
            ),
            GameType.MELEE.type to GameSetup(
                gameType = GameType.MELEE.type,
                arenaWidth = 1000,
                isArenaWidthLocked = true,
                arenaHeight = 1000,
                isArenaHeightLocked = true,
                minNumberOfParticipants = 10,
                isMinNumberOfParticipantsLocked = true,
                maxNumberOfParticipants = null,
                isMaxNumberOfParticipantsLocked = false,
                numberOfRounds = 10,
                isNumberOfRoundsLocked = false,
                gunCoolingRate = 0.1,
                isGunCoolingRateLocked = false,
                maxInactivityTurns = 450,
                isMaxInactivityTurnsLocked = false,
                turnTimeout = 30_000, // 30 milliseconds
                isTurnTimeoutLocked = false,
                readyTimeout = 1_000_000, // 1 second
                isReadyTimeoutLocked = false,
                defaultTurnsPerSecond = 30
            ),
            GameType.ONE_VS_ONE.type to GameSetup(
                gameType = GameType.ONE_VS_ONE.type,
                arenaWidth = 1000,
                isArenaWidthLocked = true,
                arenaHeight = 1000,
                isArenaHeightLocked = true,
                minNumberOfParticipants = 2,
                isMinNumberOfParticipantsLocked = true,
                maxNumberOfParticipants = 2,
                isMaxNumberOfParticipantsLocked = true,
                numberOfRounds = 10,
                isNumberOfRoundsLocked = false,
                gunCoolingRate = 0.1,
                isGunCoolingRateLocked = false,
                maxInactivityTurns = 450,
                isMaxInactivityTurnsLocked = false,
                turnTimeout = 30_000, // 30 milliseconds
                isTurnTimeoutLocked = false,
                readyTimeout = 1_000_000, // 1 second
                isReadyTimeoutLocked = false,
                defaultTurnsPerSecond = 30
            )
        )

    init {
        setProperties(defaultGameSetup)
        load()
    }

    private val gameSetups = HashMap<String, MutableGameSetup?>()

    init {
        for (propName in properties.stringPropertyNames()) {
            val strings = propName.split(".", limit = 2)
            val gameName = strings[0]
            val fieldName = strings[1]

            val value = properties.getValue(propName) as String

            if (gameSetups[gameName] == null) {
                gameSetups[gameName] = defaultGameSetup[GameType.CUSTOM.type]?.toMutableGameSetup()
            }
            val gameType = games[gameName] as MutableGameSetup
            val theField = MutableGameSetup::class.java.getDeclaredField(fieldName)
            theField.isAccessible = true
            when (theField.type.name) {
                "boolean" -> theField.setBoolean(gameType, value.toBoolean())
                "int" -> theField.setInt(gameType, value.toInt())
                "double" -> theField.setDouble(gameType, value.toDouble())
                "java.lang.Integer" -> theField.set(
                    gameType, try {
                        Integer.parseInt(value)
                    } catch (e: NumberFormatException) {
                        null
                    }
                )
                "java.lang.String" -> theField.set(gameType, value)
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
            var value = field.get(gameSetup)?.toString()
            if (value == null) {
                value = ""
            }
            properties.setProperty("$name.${prop.name}", value)
        }
    }

    override fun save() {
        setProperties(gameSetups)
        super.save()
    }
}