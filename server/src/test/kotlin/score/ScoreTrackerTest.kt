package score

import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.rules.SCORE_PER_BULLET_DAMAGE
import dev.robocode.tankroyale.server.score.ScoreTracker
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.Tag
import io.kotest.matchers.shouldBe

class ScoreTrackerTest : FunSpec({

    val botId1 = BotId(1)
    val botId2 = BotId(2)
    val participantId1 = ParticipantId(botId1)
    val participantId2 = ParticipantId(botId2)
    val participantIds = setOf(participantId1, participantId2)
    
    lateinit var tracker: ScoreTracker

    beforeTest {
        tracker = ScoreTracker(participantIds)
    }

    context("TR-SRV-SCR-001: Damage tracking").config(tags = setOf(Tag("TR-SRV-SCR-001"))) {

        test("Positive: Damage applied correctly") {
            tracker.registerBulletHit(participantId1, participantId2, 10.0, false)
            val score = tracker.calculateScore(participantId1)
            score.bulletDamageScore shouldBe 10.0 * SCORE_PER_BULLET_DAMAGE
        }

        test("Negative: Zero-damage hit") {
            tracker.registerBulletHit(participantId1, participantId2, 0.0, false)
            val score = tracker.calculateScore(participantId1)
            score.bulletDamageScore shouldBe 0.0
        }

        test("Edge: Overkill capping (received from detector)") {
            // We assume the detector already capped the damage to 5.0
            tracker.registerBulletHit(participantId1, participantId2, 5.0, true)
            val score = tracker.calculateScore(participantId1)
            score.bulletDamageScore shouldBe 5.0 * SCORE_PER_BULLET_DAMAGE
        }
    }
})
