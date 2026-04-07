package core

import dev.robocode.tankroyale.server.core.GameServer
import dev.robocode.tankroyale.server.core.GameLifecycleManager
import dev.robocode.tankroyale.server.core.ServerConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GameServerDebugModeTest : FunSpec({

    val config = ServerConfig(
        port = 7654,
        gameTypes = setOf("classic"),
        controllerSecrets = emptySet(),
        botSecrets = emptySet(),
        initialPositionEnabled = false,
        tps = 30
    )

    fun GameServer.getLifecycleManager(): GameLifecycleManager {
        return GameServer::class.java.getDeclaredField("lifecycleManager").apply {
            isAccessible = true
        }.get(this) as GameLifecycleManager
    }

    fun GameServer.callHandleEnableDebugMode() {
        GameServer::class.java.declaredMethods
            .first { it.name.startsWith("handleEnableDebugMode") }
            .apply { isAccessible = true }
            .invoke(this)
    }

    fun GameServer.callHandleDisableDebugMode() {
        GameServer::class.java.declaredMethods
            .first { it.name.startsWith("handleDisableDebugMode") }
            .apply { isAccessible = true }
            .invoke(this)
    }

    test("handleEnableDebugMode sets debugMode to true") {
        val gameServer = GameServer(config)
        val lifecycle = gameServer.getLifecycleManager()

        lifecycle.debugMode shouldBe false

        gameServer.callHandleEnableDebugMode()

        lifecycle.debugMode shouldBe true
    }

    test("handleDisableDebugMode sets debugMode to false") {
        val gameServer = GameServer(config)
        val lifecycle = gameServer.getLifecycleManager()

        lifecycle.debugMode = true
        gameServer.callHandleDisableDebugMode()

        lifecycle.debugMode shouldBe false
    }

    test("handleEnableDebugMode is idempotent") {
        val gameServer = GameServer(config)
        val lifecycle = gameServer.getLifecycleManager()

        gameServer.callHandleEnableDebugMode()
        gameServer.callHandleEnableDebugMode()

        lifecycle.debugMode shouldBe true
    }

    test("handleDisableDebugMode is idempotent") {
        val gameServer = GameServer(config)
        val lifecycle = gameServer.getLifecycleManager()

        lifecycle.debugMode = false
        gameServer.callHandleDisableDebugMode()

        lifecycle.debugMode shouldBe false
    }
})
