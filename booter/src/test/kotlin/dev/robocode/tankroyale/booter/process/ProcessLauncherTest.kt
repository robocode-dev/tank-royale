package dev.robocode.tankroyale.booter.process

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProcessLauncherTest {

    @Test
    fun `a shell script path containing a space is passed to bash as a single argument`() {
        val scriptPath = "/work/bots/java/Corners 1.0.0/Corners 1.0.0.sh"

        val builder = ProcessLauncher.createProcessBuilder(scriptPath)

        assertThat(builder.command()).containsExactly("bash", scriptPath)
    }

    @Test
    fun `a python script is launched via the python interpreter`() {
        val scriptPath = "/work/bots/python/Orbit 1.0.0/Orbit 1.0.0.py"

        val builder = ProcessLauncher.createProcessBuilder(scriptPath)

        assertThat(builder.command()).containsExactly("python", scriptPath)
    }
}
