package core

import dev.robocode.tankroyale.server.core.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.Tag
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class GameServerDebugModeTest : FunSpec({

    tags(Tag("Legacy"))

    val config = ServerConfig(
        port = 7654,
        gameTypes = setOf("classic"),
        controllerSecrets = emptySet(),
        botSecrets = emptySet(),
        initialPositionEnabled = false,
        tps = 30
    )

    fun createGameServer(lifecycleManager: GameLifecycleManager): GameServer {
        return GameServer(
            config = config,
            connectionHandler = mockk(),
            participantRegistry = mockk(),
            lifecycleManager = lifecycleManager,
            broadcaster = mockk(),
            resultsBuilder = mockk(),
            gson = mockk()
        )
    }

    test("handleEnableDebugMode sets debugMode to true") {
        val lifecycle = GameLifecycleManager()
        val gameServer = createGameServer(lifecycle)

        lifecycle.debugMode shouldBe false

        gameServer.handleEnableDebugMode()

        lifecycle.debugMode shouldBe true
    }

    test("handleDisableDebugMode sets debugMode to false") {
        val lifecycle = GameLifecycleManager()
        val gameServer = createGameServer(lifecycle)

        lifecycle.debugMode = true
        gameServer.handleDisableDebugMode()

        lifecycle.debugMode shouldBe false
    }

    test("handleEnableDebugMode is idempotent") {
        val lifecycle = GameLifecycleManager()
        val gameServer = createGameServer(lifecycle)

        gameServer.handleEnableDebugMode()
        gameServer.handleEnableDebugMode()

        lifecycle.debugMode shouldBe true
    }

    test("handleDisableDebugMode is idempotent") {
        val lifecycle = GameLifecycleManager()
        val gameServer = createGameServer(lifecycle)

        lifecycle.debugMode = false
        gameServer.handleDisableDebugMode()

        lifecycle.debugMode shouldBe false
    }
})
