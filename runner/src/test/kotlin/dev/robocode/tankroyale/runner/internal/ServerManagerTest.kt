package dev.robocode.tankroyale.runner.internal

import dev.robocode.tankroyale.runner.BattleRunner.ServerMode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * Unit tests for [ServerManager] configuration and secret generation.
 * These tests do NOT start real server processes.
 */
class ServerManagerTest {

    @Test
    fun `embedded mode generates unique controller and bot secrets`() {
        val manager = ServerManager(ServerMode.Embedded())
        assertThat(manager.controllerSecret).isNotBlank()
        assertThat(manager.botSecret).isNotBlank()
        assertThat(manager.controllerSecret).isNotEqualTo(manager.botSecret)
    }

    @Test
    fun `each instance gets distinct secrets`() {
        val manager1 = ServerManager(ServerMode.Embedded())
        val manager2 = ServerManager(ServerMode.Embedded())
        assertThat(manager1.controllerSecret).isNotEqualTo(manager2.controllerSecret)
        assertThat(manager1.botSecret).isNotEqualTo(manager2.botSecret)
    }

    @Test
    fun `embedded mode server url uses localhost`() {
        val manager = ServerManager(ServerMode.Embedded(9876))
        // Port is 0 until ensureStarted() is called, but the mode stores the requested port
        assertThat(manager.serverUrl).startsWith("ws://localhost:")
    }

    @Test
    fun `external mode server url returns configured url`() {
        val manager = ServerManager(ServerMode.External("ws://remote:7654"))
        assertThat(manager.serverUrl).isEqualTo("ws://remote:7654")
    }

    @Test
    fun `external mode isRunning returns true`() {
        val manager = ServerManager(ServerMode.External("ws://remote:7654"))
        assertThat(manager.isRunning).isTrue()
    }

    @Test
    fun `embedded mode isRunning returns false before start`() {
        val manager = ServerManager(ServerMode.Embedded())
        assertThat(manager.isRunning).isFalse()
    }

    @Test
    fun `close on external server is a no-op`() {
        val manager = ServerManager(ServerMode.External("ws://remote:7654"))
        // Should not throw
        manager.close()
    }

    @Test
    fun `close on unstarted embedded server is a no-op`() {
        val manager = ServerManager(ServerMode.Embedded())
        // Should not throw
        manager.close()
    }

    @Test
    fun `captureOutput false is accepted without throwing`() {
        val manager = ServerManager(ServerMode.Embedded(), captureOutput = false)
        assertThat(manager.isRunning).isFalse()
    }

    @Test
    fun `captureOutput true is the default`() {
        // Constructed without explicit captureOutput — must not throw and behave normally
        val manager = ServerManager(ServerMode.External("ws://localhost:9999"))
        assertThat(manager.isRunning).isTrue()
    }

    @Test
    fun `ensureStarted with unreachable external server throws BattleException`() {
        val manager = ServerManager(ServerMode.External("ws://localhost:1"))
        assertThatThrownBy { manager.ensureStarted() }
            .isInstanceOf(dev.robocode.tankroyale.runner.BattleException::class.java)
            .hasMessageContaining("Could not connect to external server")
    }
}
