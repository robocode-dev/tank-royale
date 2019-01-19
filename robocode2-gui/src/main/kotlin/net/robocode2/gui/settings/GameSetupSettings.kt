package net.robocode2.gui.settings

object GameSetupSettings : PropertiesStore("Robocode Game Setup", "game-gameSetup.properties"){

    private val defaultGameSetup: Map<String, GameType>
        get() = mapOf(
            "classic" to GameType(),
            "1-vs-1" to GameType(width = 1000, height = 1000, maxNumParticipants = 2),
            "melee" to GameType(width = 1000, height = 1000, minNumParticipants = 10))

    init {
        setProperties(defaultGameSetup)
        load()
    }

    private val internalGameSetup = GameSetup()

    init {
        for (propName in properties.stringPropertyNames()) {
            val strings = propName.split(".", limit = 2)
            val gameName = strings[0]
            val fieldName = strings[1]
            val value = properties.getValue(propName) as String

            if (internalGameSetup[gameName] == null) {
                internalGameSetup[gameName] = GameType()
            }
            val gameType = gameSetup[gameName] as GameType
            val theField = GameType::class.java.getDeclaredField(fieldName)
            theField.isAccessible = true
            when (theField.type.name) {
                "int" -> theField.setInt(gameType, value.toInt())
                "double" -> theField.setDouble(gameType, value.toDouble())
                "String" -> theField.set(gameType, value)
                "java.lang.Integer" -> theField.set(gameType, try { Integer.parseInt(value) } catch (e: NumberFormatException) { null })
                else -> throw RuntimeException("Type is missing implementation: ${theField.type.name}")
            }
        }
    }

    val gameSetup: GameSetup
        get() {
            return internalGameSetup
        }

    private fun setProperties(gameSetup: Map<String, GameType>) {
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
            var value = field.get(gameType)?.toString()
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