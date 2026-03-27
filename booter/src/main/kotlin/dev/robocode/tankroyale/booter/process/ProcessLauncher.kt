package dev.robocode.tankroyale.booter.process

/**
 * Creates OS-appropriate [ProcessBuilder] instances for bot launch scripts.
 * Centralises script-type detection so [ProcessManager] stays focused on lifecycle management.
 */
internal object ProcessLauncher {

    fun createProcessBuilder(command: String): ProcessBuilder =
        when (getScriptType(command)) {
            ScriptType.WINDOWS_BATCH -> ProcessBuilder("cmd.exe", "/c", command)
            ScriptType.SHELL_SCRIPT -> ProcessBuilder("bash", "-c", command)
            ScriptType.PYTHON_SCRIPT -> ProcessBuilder("python", command)
            ScriptType.OTHER -> ProcessBuilder(command)
        }

    private fun getScriptType(command: String): ScriptType {
        val cmd = command.lowercase()
        return when {
            cmd.endsWith(".bat") || cmd.endsWith(".cmd") -> ScriptType.WINDOWS_BATCH
            cmd.endsWith(".sh") -> ScriptType.SHELL_SCRIPT
            cmd.endsWith(".py") -> ScriptType.PYTHON_SCRIPT
            else -> ScriptType.OTHER
        }
    }

    private enum class ScriptType {
        WINDOWS_BATCH,
        SHELL_SCRIPT,
        PYTHON_SCRIPT,
        OTHER
    }
}
