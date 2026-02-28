package dev.robocode.tankroyale.intent

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class IntentDiagnosticsProxyTest {

    private var proxy: IntentDiagnosticsProxy? = null

    @AfterEach
    fun tearDown() {
        proxy?.close()
    }

    @Test
    fun `proxy starts and assigns a port`() {
        proxy = IntentDiagnosticsProxy("ws://localhost:9999")
        proxy!!.start()

        assertThat(proxy!!.isRunning).isTrue()
        assertThat(proxy!!.port).isGreaterThan(0)
        assertThat(proxy!!.proxyUrl).startsWith("ws://localhost:")
    }

    @Test
    fun `proxy close stops the server`() {
        proxy = IntentDiagnosticsProxy("ws://localhost:9999")
        proxy!!.start()
        assertThat(proxy!!.isRunning).isTrue()

        proxy!!.close()

        assertThat(proxy!!.isRunning).isFalse()
    }

    @Test
    fun `start is idempotent`() {
        proxy = IntentDiagnosticsProxy("ws://localhost:9999")
        proxy!!.start()
        val port1 = proxy!!.port

        proxy!!.start() // second call should be a no-op

        assertThat(proxy!!.port).isEqualTo(port1)
        assertThat(proxy!!.isRunning).isTrue()
    }

    @Test
    fun `store is empty before any connections`() {
        proxy = IntentDiagnosticsProxy("ws://localhost:9999")
        proxy!!.start()

        assertThat(proxy!!.store.size).isEqualTo(0)
        assertThat(proxy!!.store.botNames()).isEmpty()
    }

    @Test
    fun `close is safe to call multiple times`() {
        proxy = IntentDiagnosticsProxy("ws://localhost:9999")
        proxy!!.start()

        proxy!!.close()
        proxy!!.close() // should not throw
    }
}
