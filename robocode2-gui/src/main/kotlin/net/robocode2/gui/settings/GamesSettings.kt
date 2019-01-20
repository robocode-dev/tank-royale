package net.robocode2.gui.settings

object GamesSettings : PropertiesStore("Robocode Game Setup", "game-games.properties") {

    val defaultGameSetup: Map<String, GameSetup>
        get() = mapOf(
                GameType.CLASSIC.type to GameSetup(),
                GameType.MELEE.type to GameSetup(width = 1000, height = 1000, minNumParticipants = 10),
                GameType.ONE_VS_ONE.type to GameSetup(width = 1000, height = 1000, maxNumParticipants = 2))

    init {
        setProperties(defaultGameSetup)
        load()
    }

    private val internalGameSetup = Games()

    init {
        for (propName in properties.stringPropertyNames()) {
            val strings = propName.split(".", limit = 2)
            val gameName = strings[0]
            val fieldName = strings[1]
            val value = properties.getValue(propName) as String

            if (internalGameSetup[gameName] == null) {
                internalGameSetup[gameName] = GameSetup()
            }
            val gameType = games[gameName] as GameSetup
            val theField = GameSetup::class.java.getDeclaredField(fieldName)
            theField.isAccessible = true
            when (theField.type.name) {
                "int" -> theField.setInt(gameType, value.toInt())
                "double" -> theField.setDouble(gameType, value.toDouble())
                "String" -> theField.set(gameType, value)
                "java.lang.Integer" -> theField.set(gameType, try {
                    Integer.parseInt(value)
                } catch (e: NumberFormatException) {
                    null
                })
                else -> throw RuntimeException("Type is missing implementation: ${theField.type.name}")
            }
        }
    }

    val games: Games
        get() {
            return internalGameSetup
        }

    private fun setProperties(gameSetup: Map<String, GameSetup>) {
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