package dev.robocode.tankroyale.rumble

/** Which Rumble environment the desktop program uses, worked out from the variable's value. */
sealed interface EnvironmentChoice {
    data class Chosen(val environment: RumbleEnvironment) : EnvironmentChoice

    /** The variable names no known environment. It is reported, never replaced by another one. */
    data class Unknown(val name: String) : EnvironmentChoice
}

/**
 * The developer switch that shows the Rumble menu while it is not whole, and the choice of environment. Both read
 * only what they are given: the menu is shown for a true word in the property or in the variable, and the
 * environment is the one the variable names, the public Rumble when it names none, the same rule as the command line.
 */
object RumbleMenuSwitch {
    const val MENU_PROPERTY = "rumble.menu"
    const val MENU_VARIABLE = "RUMBLE_MENU"
    const val ENVIRONMENT_VARIABLE = "RUMBLE_ENVIRONMENT"

    private val TRUE_WORDS = setOf("1", "true", "yes", "on")

    /** True when [property] or [variable] holds a true word. */
    fun menuShown(property: String?, variable: String?): Boolean =
        listOf(property, variable).any { it?.trim()?.lowercase() in TRUE_WORDS }

    /** The environment named by [variable], or the public Rumble when it is not set or blank. */
    fun environment(variable: String?): EnvironmentChoice {
        val name = variable?.trim()?.takeIf { it.isNotEmpty() } ?: return EnvironmentChoice.Chosen(RumbleEnvironments.DEFAULT)
        return RumbleEnvironments.all.singleOrNull { it.name == name }?.let { EnvironmentChoice.Chosen(it) } ?: EnvironmentChoice.Unknown(name)
    }
}
