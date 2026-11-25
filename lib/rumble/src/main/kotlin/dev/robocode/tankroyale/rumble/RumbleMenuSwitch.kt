package dev.robocode.tankroyale.rumble

/** Which Rumble environment the desktop program uses, worked out from the variable's value. */
sealed interface EnvironmentChoice {
    data class Chosen(val environment: RumbleEnvironment) : EnvironmentChoice

    /** The variable names no known environment. It is reported, never replaced by another one. */
    data class Unknown(val name: String) : EnvironmentChoice
}

/**
 * The desktop program's choice of Rumble environment. It reads only what it is given: the environment is the one the
 * variable names, and the public Rumble when it names none, the same rule as the command line. The Rumble menu itself
 * is always shown; the developer switch that hid it while the menu was not whole is gone.
 */
object RumbleMenuSwitch {
    const val ENVIRONMENT_VARIABLE = "RUMBLE_ENVIRONMENT"

    /** The environment named by [variable], or the public Rumble when it is not set or blank. */
    fun environment(variable: String?): EnvironmentChoice {
        val name = variable?.trim()?.takeIf { it.isNotEmpty() } ?: return EnvironmentChoice.Chosen(RumbleEnvironments.DEFAULT)
        return RumbleEnvironments.all.singleOrNull { it.name == name }?.let { EnvironmentChoice.Chosen(it) } ?: EnvironmentChoice.Unknown(name)
    }
}
