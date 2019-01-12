package net.robocode2.gui.settings

object GameSetupSettings : PropertiesStore("Robocode Game Setup", "game-setup.properties"){

    private val gameSetup = mapOf(
            "classic" to GameType(),
            "1-vs-1" to GameType(width = 1000, height = 1000, maxNumParticipants = 2),
            "melee" to GameType(width = 1000, height = 1000, minNumParticipants = 10)
    )

    init {
        if (!load()) {
            setPropertiesToDefaultGameSetup()
        }
    }

    val setup: GameSetup
        get() {
            val gameSetup = GameSetup()

            for (propName in properties.stringPropertyNames()) {
                val strings = propName.split(".", limit = 2)
                val gameName = strings[0]
                val fieldName = strings[1]
                val value = properties.getValue(propName) as String

                if (gameSetup[gameName] == null) {
                    gameSetup[gameName] = GameType()
                }
                val gameType = gameSetup[gameName] as GameType
                val field = GameType::class.java.getDeclaredField(fieldName)
                field.isAccessible = true
                when (field.type.name) {
                    "int"-> field.setInt(gameType, value.toInt())
                    "double" -> field.setDouble(gameType, value.toDouble())
                    "String" -> field.set(gameType, value)
                    "java.lang.Integer" -> field.set(gameType, Integer.parseInt(value))
                    else -> throw RuntimeException("Type is missing implementation: ${field.type.name}")
                }
            }

            return gameSetup
        }

    private fun setPropertiesToDefaultGameSetup() {
        for (key in gameSetup.keys) {
            val gameType = gameSetup[key]
            if (gameType != null) {
                putGameType(key, gameType)
            }
        }
    }

    private fun putGameType(name: String, gameType: GameType) {
        for (prop in GameType::class.java.declaredFields) {
            val field = GameType::class.java.getDeclaredField(prop.name)
            field.isAccessible = true
            val value = field.get(gameType)?.toString()
            if (value != null) {
                properties.setProperty("$name.${prop.name}", value)
            }
        }
    }
}