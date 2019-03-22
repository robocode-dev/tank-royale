package net.robocode2.gui.settings

import net.robocode2.gui.model.GameSetup

object GamesSettings : PropertiesStore("Robocode Game Setup", "games.properties") {

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
                        inactivityTurns = 450,
                        isInactivityTurnsLocked = false,
                        turnTimeout = 30,
                        isTurnTimeoutLocked = false,
                        readyTimeout = 1000,
                        isReadyTimeoutLocked = false
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
                        inactivityTurns = 450,
                        isInactivityTurnsLocked = true,
                        turnTimeout = 30,
                        isTurnTimeoutLocked = false,
                        readyTimeout = 1000,
                        isReadyTimeoutLocked = false
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
                        inactivityTurns = 450,
                        isInactivityTurnsLocked = false,
                        turnTimeout = 30,
                        isTurnTimeoutLocked = false,
                        readyTimeout = 1000,
                        isReadyTimeoutLocked = false
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
                        inactivityTurns = 450,
                        isInactivityTurnsLocked = false,
                        turnTimeout = 30,
                        isTurnTimeoutLocked = false,
                        readyTimeout = 1000,
                        isReadyTimeoutLocked = false
                )
        )

    init {
        setProperties(defaultGameSetup)
        load()
    }

    private val internalGameSetup = HashMap<String, GameSetup?>()

    init {
        for (propName in properties.stringPropertyNames()) {
            val strings = propName.split(".", limit = 2)
            val gameName = strings[0]
            val fieldName = strings[1]
            val value = properties.getValue(propName) as String

            if (internalGameSetup[gameName] == null) {
                internalGameSetup[gameName] = defaultGameSetup[GameType.CUSTOM.type]
            }
            val gameType = games[gameName] as GameSetup
            val theField = GameSetup::class.java.getDeclaredField(fieldName)
            theField.isAccessible = true
            when (theField.type.name) {
                "boolean" -> theField.setBoolean(gameType, value.toBoolean())
                "int" -> theField.setInt(gameType, value.toInt())
                "double" -> theField.setDouble(gameType, value.toDouble())
                "java.lang.Integer" -> theField.set(gameType, try {
                    Integer.parseInt(value)
                } catch (e: NumberFormatException) {
                    null
                })
                "java.lang.String" -> theField.set(gameType, value)
                else -> throw RuntimeException("Type is missing implementation: ${theField.type.name}")
            }
        }
    }

    val games: MutableMap<String, GameSetup?>
            get() {
            return internalGameSetup
        }

    private fun setProperties(gameSetup: Map<String, GameSetup?>) {
        for (key in gameSetup.keys) {
            val gameType = gameSetup[key]
            if (gameType != null) {
                putGameType(key, gameType)
            }
        }
    }

    private fun putGameType(name: String, gameSetup: GameSetup) {
        for (prop in GameSetup::class.java.declaredFields) {
            val field = GameSetup::class.java.getDeclaredField(prop.name)
            field.isAccessible = true
            var value = field.get(gameSetup)?.toString()
            if (value == null) {
                value = ""
            }
            properties.setProperty("$name.${prop.name}", value)
        }
    }

    override fun save() {
        setProperties(internalGameSetup)
        super.save()
    }
}