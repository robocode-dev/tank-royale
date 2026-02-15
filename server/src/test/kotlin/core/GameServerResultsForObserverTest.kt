package core

import dev.robocode.tankroyale.schema.Participant
import dev.robocode.tankroyale.schema.ResultsForObserver
import dev.robocode.tankroyale.server.core.GameServer
import dev.robocode.tankroyale.server.core.ModelUpdater
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.model.Score
import dev.robocode.tankroyale.server.model.TeamId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.ConcurrentHashMap


class GameServerResultsForObserverTest : FunSpec({

    test("should include isTeam flag and competition ranks") {
        val gameServer = GameServer(setOf("classic"), emptySet(), emptySet())

        val participantMapField = GameServer::class.java.getDeclaredField("participantMap").apply {
            isAccessible = true
        }
        val participantMap =
            participantMapField.get(gameServer) as ConcurrentHashMap<BotId, Participant>

        participantMap[BotId(1)] = participant(botId = 1, name = "TeamBotA", teamId = 10, teamName = "Alpha", teamVersion = "1.0")
        participantMap[BotId(2)] = participant(botId = 2, name = "TeamBotB", teamId = 10, teamName = "Alpha", teamVersion = "1.0")
        participantMap[BotId(3)] = participant(botId = 3, name = "SoloBot")

        val teamScore1 = Score(ParticipantId(BotId(1), TeamId(10))).apply {
            bulletDamageScore = 50.0
            rank = 0
        }
        val teamScore2 = Score(ParticipantId(BotId(2), TeamId(10))).apply {
            bulletDamageScore = 30.0
            rank = 8
        }
        val soloScore = Score(ParticipantId(BotId(3))).apply {
            bulletDamageScore = 60.0
            rank = 3
        }

        val modelUpdater = mockk<ModelUpdater>()
        every { modelUpdater.getResults() } returns listOf(teamScore1, teamScore2, soloScore)

        val modelUpdaterField = GameServer::class.java.getDeclaredField("modelUpdater").apply {
            isAccessible = true
        }
        modelUpdaterField.set(gameServer, modelUpdater)

        val getResultsMethod = GameServer::class.java.getDeclaredMethod("getResultsForObservers").apply {
            isAccessible = true
        }
        val results = getResultsMethod.invoke(gameServer) as List<ResultsForObserver>

        // Team score = 80 (50+30), Solo = 60 → ranks 1, 2
        results.map { it.rank } shouldContainExactly listOf(1, 2)

        results[0].apply {
            id shouldBe 10
            name shouldBe "Alpha"
            version shouldBe "1.0"
            isTeam shouldBe true
        }

        results[1].apply {
            id shouldBe 3
            name shouldBe "SoloBot"
            isTeam shouldBe false
        }
    }

    test("should assign same rank for tied scores (two 1st places)") {
        val gameServer = GameServer(setOf("classic"), emptySet(), emptySet())

        val participantMapField = GameServer::class.java.getDeclaredField("participantMap").apply {
            isAccessible = true
        }
        val participantMap =
            participantMapField.get(gameServer) as ConcurrentHashMap<BotId, Participant>

        participantMap[BotId(1)] = participant(botId = 1, name = "Bot1")
        participantMap[BotId(2)] = participant(botId = 2, name = "TeamBotA", teamId = 10, teamName = "Alpha", teamVersion = "1.0")
        participantMap[BotId(3)] = participant(botId = 3, name = "Bot3")
        participantMap[BotId(4)] = participant(botId = 4, name = "Bot4")

        val bot1Score = Score(ParticipantId(BotId(1))).apply { bulletDamageScore = 370.0 }
        val teamScore = Score(ParticipantId(BotId(2), TeamId(10))).apply { bulletDamageScore = 370.0 }
        val bot3Score = Score(ParticipantId(BotId(3))).apply { bulletDamageScore = 250.0 }
        val bot4Score = Score(ParticipantId(BotId(4))).apply { bulletDamageScore = 150.0 }

        val modelUpdater = mockk<ModelUpdater>()
        every { modelUpdater.getResults() } returns listOf(bot1Score, teamScore, bot3Score, bot4Score)

        val modelUpdaterField = GameServer::class.java.getDeclaredField("modelUpdater").apply {
            isAccessible = true
        }
        modelUpdaterField.set(gameServer, modelUpdater)

        val getResultsMethod = GameServer::class.java.getDeclaredMethod("getResultsForObservers").apply {
            isAccessible = true
        }
        val results = getResultsMethod.invoke(gameServer) as List<ResultsForObserver>

        // Two 1st places (tied at 370), skip 2nd, then 3rd and 4th
        results.map { it.rank } shouldContainExactly listOf(1, 1, 3, 4)
    }

    test("should handle multiple tie groups") {
        val gameServer = GameServer(setOf("classic"), emptySet(), emptySet())

        val participantMapField = GameServer::class.java.getDeclaredField("participantMap").apply {
            isAccessible = true
        }
        val participantMap =
            participantMapField.get(gameServer) as ConcurrentHashMap<BotId, Participant>

        participantMap[BotId(1)] = participant(botId = 1, name = "Bot1")
        participantMap[BotId(2)] = participant(botId = 2, name = "TeamBotA", teamId = 10, teamName = "Alpha", teamVersion = "1.0")
        participantMap[BotId(3)] = participant(botId = 3, name = "Bot3")
        participantMap[BotId(4)] = participant(botId = 4, name = "Bot4")
        participantMap[BotId(5)] = participant(botId = 5, name = "Bot5")

        val bot1Score = Score(ParticipantId(BotId(1))).apply { bulletDamageScore = 370.0 }
        val teamScore = Score(ParticipantId(BotId(2), TeamId(10))).apply { bulletDamageScore = 370.0 }
        val bot3Score = Score(ParticipantId(BotId(3))).apply { bulletDamageScore = 230.0 }
        val bot4Score = Score(ParticipantId(BotId(4))).apply { bulletDamageScore = 230.0 }
        val bot5Score = Score(ParticipantId(BotId(5))).apply { bulletDamageScore = 220.0 }

        val modelUpdater = mockk<ModelUpdater>()
        every { modelUpdater.getResults() } returns listOf(bot1Score, teamScore, bot3Score, bot4Score, bot5Score)

        val modelUpdaterField = GameServer::class.java.getDeclaredField("modelUpdater").apply {
            isAccessible = true
        }
        modelUpdaterField.set(gameServer, modelUpdater)

        val getResultsMethod = GameServer::class.java.getDeclaredMethod("getResultsForObservers").apply {
            isAccessible = true
        }
        val results = getResultsMethod.invoke(gameServer) as List<ResultsForObserver>

        // Two 1st (370), two 3rd (230), one 5th (220)
        results.map { it.rank } shouldContainExactly listOf(1, 1, 3, 3, 5)
    }

    test("should assign all rank 1 when all scores are equal") {
        val gameServer = GameServer(setOf("classic"), emptySet(), emptySet())

        val participantMapField = GameServer::class.java.getDeclaredField("participantMap").apply {
            isAccessible = true
        }
        val participantMap =
            participantMapField.get(gameServer) as ConcurrentHashMap<BotId, Participant>

        participantMap[BotId(1)] = participant(botId = 1, name = "Bot1")
        participantMap[BotId(2)] = participant(botId = 2, name = "Bot2")
        participantMap[BotId(3)] = participant(botId = 3, name = "Bot3")
        participantMap[BotId(4)] = participant(botId = 4, name = "Bot4")

        val bot1Score = Score(ParticipantId(BotId(1))).apply { bulletDamageScore = 100.0 }
        val bot2Score = Score(ParticipantId(BotId(2))).apply { bulletDamageScore = 100.0 }
        val bot3Score = Score(ParticipantId(BotId(3))).apply { bulletDamageScore = 100.0 }
        val bot4Score = Score(ParticipantId(BotId(4))).apply { bulletDamageScore = 100.0 }

        val modelUpdater = mockk<ModelUpdater>()
        every { modelUpdater.getResults() } returns listOf(bot1Score, bot2Score, bot3Score, bot4Score)

        val modelUpdaterField = GameServer::class.java.getDeclaredField("modelUpdater").apply {
            isAccessible = true
        }
        modelUpdaterField.set(gameServer, modelUpdater)

        val getResultsMethod = GameServer::class.java.getDeclaredMethod("getResultsForObservers").apply {
            isAccessible = true
        }
        val results = getResultsMethod.invoke(gameServer) as List<ResultsForObserver>

        // All equal scores → all rank 1
        results.map { it.rank } shouldContainExactly listOf(1, 1, 1, 1)
    }

    test("should assign sequential ranks when all scores are different") {
        val gameServer = GameServer(setOf("classic"), emptySet(), emptySet())

        val participantMapField = GameServer::class.java.getDeclaredField("participantMap").apply {
            isAccessible = true
        }
        val participantMap =
            participantMapField.get(gameServer) as ConcurrentHashMap<BotId, Participant>

        participantMap[BotId(1)] = participant(botId = 1, name = "Bot1")
        participantMap[BotId(2)] = participant(botId = 2, name = "Bot2")
        participantMap[BotId(3)] = participant(botId = 3, name = "Bot3")
        participantMap[BotId(4)] = participant(botId = 4, name = "Bot4")

        val bot1Score = Score(ParticipantId(BotId(1))).apply { bulletDamageScore = 400.0 }
        val bot2Score = Score(ParticipantId(BotId(2))).apply { bulletDamageScore = 100.0 }
        val bot3Score = Score(ParticipantId(BotId(3))).apply { bulletDamageScore = 300.0 }
        val bot4Score = Score(ParticipantId(BotId(4))).apply { bulletDamageScore = 200.0 }

        val modelUpdater = mockk<ModelUpdater>()
        every { modelUpdater.getResults() } returns listOf(bot1Score, bot2Score, bot3Score, bot4Score)

        val modelUpdaterField = GameServer::class.java.getDeclaredField("modelUpdater").apply {
            isAccessible = true
        }
        modelUpdaterField.set(gameServer, modelUpdater)

        val getResultsMethod = GameServer::class.java.getDeclaredMethod("getResultsForObservers").apply {
            isAccessible = true
        }
        val results = getResultsMethod.invoke(gameServer) as List<ResultsForObserver>

        // All different scores → sequential ranks 1, 2, 3, 4
        results.map { it.rank } shouldContainExactly listOf(1, 2, 3, 4)
    }

    test("should handle three-way tie for 1st place") {
        val gameServer = GameServer(setOf("classic"), emptySet(), emptySet())

        val participantMapField = GameServer::class.java.getDeclaredField("participantMap").apply {
            isAccessible = true
        }
        val participantMap =
            participantMapField.get(gameServer) as ConcurrentHashMap<BotId, Participant>

        participantMap[BotId(1)] = participant(botId = 1, name = "Bot1")
        participantMap[BotId(2)] = participant(botId = 2, name = "TeamBotA", teamId = 10, teamName = "Alpha", teamVersion = "1.0")
        participantMap[BotId(3)] = participant(botId = 3, name = "Bot3")
        participantMap[BotId(4)] = participant(botId = 4, name = "Bot4")
        participantMap[BotId(5)] = participant(botId = 5, name = "Bot5")

        val bot1Score = Score(ParticipantId(BotId(1))).apply { bulletDamageScore = 370.0 }
        val teamScore = Score(ParticipantId(BotId(2), TeamId(10))).apply { bulletDamageScore = 370.0 }
        val bot3Score = Score(ParticipantId(BotId(3))).apply { bulletDamageScore = 370.0 }
        val bot4Score = Score(ParticipantId(BotId(4))).apply { bulletDamageScore = 230.0 }
        val bot5Score = Score(ParticipantId(BotId(5))).apply { bulletDamageScore = 220.0 }

        val modelUpdater = mockk<ModelUpdater>()
        every { modelUpdater.getResults() } returns listOf(bot1Score, teamScore, bot3Score, bot4Score, bot5Score)

        val modelUpdaterField = GameServer::class.java.getDeclaredField("modelUpdater").apply {
            isAccessible = true
        }
        modelUpdaterField.set(gameServer, modelUpdater)

        val getResultsMethod = GameServer::class.java.getDeclaredMethod("getResultsForObservers").apply {
            isAccessible = true
        }
        val results = getResultsMethod.invoke(gameServer) as List<ResultsForObserver>

        // Three 1st places (370), skip 2nd and 3rd, then 4th and 5th
        results.map { it.rank } shouldContainExactly listOf(1, 1, 1, 4, 5)
    }

    test("should handle tie for 2nd place") {
        val gameServer = GameServer(setOf("classic"), emptySet(), emptySet())

        val participantMapField = GameServer::class.java.getDeclaredField("participantMap").apply {
            isAccessible = true
        }
        val participantMap =
            participantMapField.get(gameServer) as ConcurrentHashMap<BotId, Participant>

        participantMap[BotId(1)] = participant(botId = 1, name = "Bot1")
        participantMap[BotId(2)] = participant(botId = 2, name = "TeamBotA", teamId = 10, teamName = "Alpha", teamVersion = "1.0")
        participantMap[BotId(3)] = participant(botId = 3, name = "Bot3")
        participantMap[BotId(4)] = participant(botId = 4, name = "Bot4")
        participantMap[BotId(5)] = participant(botId = 5, name = "Bot5")

        val bot1Score = Score(ParticipantId(BotId(1))).apply { bulletDamageScore = 410.0 }
        val teamScore = Score(ParticipantId(BotId(2), TeamId(10))).apply { bulletDamageScore = 370.0 }
        val bot3Score = Score(ParticipantId(BotId(3))).apply { bulletDamageScore = 370.0 }
        val bot4Score = Score(ParticipantId(BotId(4))).apply { bulletDamageScore = 210.0 }
        val bot5Score = Score(ParticipantId(BotId(5))).apply { bulletDamageScore = 210.0 }

        val modelUpdater = mockk<ModelUpdater>()
        every { modelUpdater.getResults() } returns listOf(bot1Score, teamScore, bot3Score, bot4Score, bot5Score)

        val modelUpdaterField = GameServer::class.java.getDeclaredField("modelUpdater").apply {
            isAccessible = true
        }
        modelUpdaterField.set(gameServer, modelUpdater)

        val getResultsMethod = GameServer::class.java.getDeclaredMethod("getResultsForObservers").apply {
            isAccessible = true
        }
        val results = getResultsMethod.invoke(gameServer) as List<ResultsForObserver>

        // 1st (410), two 2nd (370), two 4th (210)
        results.map { it.rank } shouldContainExactly listOf(1, 2, 2, 4, 4)
    }
})

private fun participant(
    botId: Int,
    name: String,
    version: String = "1.0",
    teamId: Int? = null,
    teamName: String? = null,
    teamVersion: String? = null,
): Participant = Participant().apply {
    id = botId
    sessionId = "session-$botId"
    this.name = name
    this.version = version
    authors = listOf("Author")
    this.teamId = teamId
    this.teamName = teamName
    this.teamVersion = teamVersion
}
