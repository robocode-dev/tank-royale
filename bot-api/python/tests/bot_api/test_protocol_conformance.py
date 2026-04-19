"""
Cross-platform protocol conformance tests (TR-API-TCK-007 through TR-API-TCK-017).

These tests verify that the Python Bot API correctly handles protocol messages
as defined in the Tank Royale cross-platform TCK.

See also: bot-api/tests/TEST-REGISTRY.md
"""
import json
import threading
import time
import pytest

from robocode_tank_royale.bot_api import Bot, BotInfo
from robocode_tank_royale.bot_api.events import (
    RoundStartedEvent,
    RoundEndedEvent,
    GameEndedEvent,
    SkippedTurnEvent,
    ConnectionErrorEvent,
    DeathEvent,
    BotDeathEvent,
    HitByBulletEvent,
    BulletHitBotEvent,
)
from robocode_tank_royale.schema import (
    BotDeathEvent as SchemaBotDeathEvent,
    BulletHitBotEvent as SchemaBulletHitBotEvent,
    BulletState as SchemaBulletState,
    Message,
)
from tests.bot_api.abstract_bot_test import AbstractBotTest


@pytest.mark.TCK
class TestProtocolConformance(AbstractBotTest):

    # -----------------------------------------------------------------------
    # TCK-007: BotHandshake contains correct sessionId, name, version, authors, isDroid
    # -----------------------------------------------------------------------

    @pytest.mark.TR_API_TCK_007
    def test_tck007_bot_handshake_contains_correct_fields(self):
        bot = Bot(self.bot_info, self.server.server_url)
        self.start_async(bot)
        self.assertTrue(self.server.await_bot_handshake(3000))

        handshake = self.server.get_handshake()
        self.assertIsNotNone(handshake)
        self.assertEqual(handshake.session_id, self.server.session_id)
        self.assertEqual(handshake.name, "TestBot")
        self.assertEqual(handshake.version, "1.0")
        self.assertCountEqual(handshake.authors, ["Author 1", "Author 2"])
        self.assertFalse(handshake.is_droid or False)

    # -----------------------------------------------------------------------
    # TCK-008: Bot sends BotReady after GameStarted
    # -----------------------------------------------------------------------

    @pytest.mark.TR_API_TCK_008
    def test_tck008_bot_sends_bot_ready_after_game_started(self):
        bot = Bot(self.bot_info, self.server.server_url)
        self.start_async(bot)
        self.assertTrue(self.server.await_bot_ready_message(3000))

    # -----------------------------------------------------------------------
    # TCK-009: onRoundStarted fires with roundNumber==1
    # -----------------------------------------------------------------------

    @pytest.mark.TR_API_TCK_009
    def test_tck009_on_round_started_fires_with_round_number_1(self):
        latch = threading.Event()
        captured = {}

        class TckBot(Bot):
            def on_round_started(self, event: RoundStartedEvent):
                captured["round_number"] = event.round_number
                latch.set()

        bot = TckBot(self.bot_info, self.server.server_url)
        self.start_async(bot)

        self.assertTrue(latch.wait(3.0), "on_round_started should be called")
        self.assertEqual(captured["round_number"], 1)

    # -----------------------------------------------------------------------
    # TCK-010: onRoundEnded fires with roundNumber==1, turnNumber==5
    # -----------------------------------------------------------------------

    @pytest.mark.TR_API_TCK_010
    def test_tck010_on_round_ended_fires_with_correct_numbers(self):
        latch = threading.Event()
        captured = {}

        class TckBot(Bot):
            def on_round_ended(self, event: RoundEndedEvent):
                captured["round_number"] = event.round_number
                captured["turn_number"] = event.turn_number
                latch.set()

        bot = TckBot(self.bot_info, self.server.server_url)
        self.start_async(bot)

        self.assertTrue(self.server.await_bot_ready_message(3000))
        self.server.send_raw(_build_round_ended_json(1, 5))

        self.assertTrue(latch.wait(3.0), "on_round_ended should be called")
        self.assertEqual(captured["round_number"], 1)
        self.assertEqual(captured["turn_number"], 5)

    # -----------------------------------------------------------------------
    # TCK-011: onGameEnded fires with numberOfRounds==10
    # -----------------------------------------------------------------------

    @pytest.mark.TR_API_TCK_011
    def test_tck011_on_game_ended_fires_with_number_of_rounds_10(self):
        latch = threading.Event()
        captured = {}

        class TckBot(Bot):
            def on_game_ended(self, event: GameEndedEvent):
                captured["number_of_rounds"] = event.number_of_rounds
                latch.set()

        bot = TckBot(self.bot_info, self.server.server_url)
        self.start_async(bot)

        self.assertTrue(self.server.await_bot_ready_message(3000))
        self.server.send_raw(_build_game_ended_json(10))

        self.assertTrue(latch.wait(3.0), "on_game_ended should be called")
        self.assertEqual(captured["number_of_rounds"], 10)

    # -----------------------------------------------------------------------
    # TCK-012: onSkippedTurn fires with turnNumber==1
    # -----------------------------------------------------------------------

    @pytest.mark.TR_API_TCK_012
    def test_tck012_on_skipped_turn_fires_with_turn_number_1(self):
        latch = threading.Event()
        captured = {}

        class TckBot(Bot):
            def on_skipped_turn(self, event: SkippedTurnEvent):
                captured["turn_number"] = event.turn_number
                latch.set()

        bot = TckBot(self.bot_info, self.server.server_url)
        self.start_async(bot)

        self.assertTrue(self.server.await_bot_ready(3000))
        self.await_tick(bot)
        self.server.send_raw(json.dumps({"type": "SkippedTurnEvent", "turnNumber": 1}))
        time.sleep(0.1)  # Allow WebSocket receive thread to process the event before next tick
        self.server.continue_bot_intent()

        self.assertTrue(latch.wait(3.0), "on_skipped_turn should be called")
        self.assertEqual(captured["turn_number"], 1)

    # -----------------------------------------------------------------------
    # TCK-013: Unknown server message type triggers onConnectionError
    # -----------------------------------------------------------------------

    @pytest.mark.TR_API_TCK_013
    def test_tck013_unknown_message_type_triggers_on_connection_error(self):
        latch = threading.Event()
        captured = {}

        class TckBot(Bot):
            def on_connection_error(self, event: ConnectionErrorEvent):
                captured["message"] = str(event.error)
                latch.set()

        bot = TckBot(self.bot_info, self.server.server_url)
        self.start_async(bot)

        self.assertTrue(self.server.await_bot_ready_message(3000))
        self.server.send_raw(json.dumps({"type": "UnknownMessageType", "data": "test"}))

        self.assertTrue(latch.wait(3.0), "on_connection_error should be called")
        self.assertIn("Unsupported WebSocket message type", captured["message"])

    # -----------------------------------------------------------------------
    # TCK-014: BotDeathEvent(victimId==myId) triggers onDeath
    # -----------------------------------------------------------------------

    @pytest.mark.TR_API_TCK_014
    def test_tck014_bot_death_event_self_triggers_on_death(self):
        latch = threading.Event()

        class TckBot(Bot):
            def on_death(self, event: DeathEvent):
                latch.set()

        bot = TckBot(self.bot_info, self.server.server_url)
        self.start_bot(bot)

        self.server.add_event(SchemaBotDeathEvent(
            victim_id=self.server.my_id,
            turn_number=2,
            type=Message.Type.BOT_DEATH_EVENT,
        ))
        self.server.set_bot_state_and_await_tick()

        self.assertTrue(latch.wait(3.0), "on_death should be called")

    # -----------------------------------------------------------------------
    # TCK-015: BotDeathEvent(victimId!=myId) triggers onBotDeath
    # -----------------------------------------------------------------------

    @pytest.mark.TR_API_TCK_015
    def test_tck015_bot_death_event_other_triggers_on_bot_death(self):
        latch = threading.Event()
        captured = {}

        class TckBot(Bot):
            def on_bot_death(self, event: BotDeathEvent):
                captured["victim_id"] = event.victim_id
                latch.set()

        bot = TckBot(self.bot_info, self.server.server_url)
        self.start_bot(bot)

        self.server.add_event(SchemaBotDeathEvent(
            victim_id=99,
            turn_number=2,
            type=Message.Type.BOT_DEATH_EVENT,
        ))
        self.server.reset_bot_intent_latch()
        self.server.set_bot_state_and_await_tick()
        self.server.continue_bot_intent()
        self.await_bot_intent(1000)

        self.assertTrue(latch.wait(3.0), "on_bot_death should be called")
        self.assertEqual(captured["victim_id"], 99)

    # -----------------------------------------------------------------------
    # TCK-016: BulletHitBotEvent(victimId==myId) triggers onHitByBullet
    # -----------------------------------------------------------------------

    @pytest.mark.TR_API_TCK_016
    def test_tck016_bullet_hit_bot_event_self_triggers_on_hit_by_bullet(self):
        latch = threading.Event()

        class TckBot(Bot):
            def on_hit_by_bullet(self, event: HitByBulletEvent):
                latch.set()

        bot = TckBot(self.bot_info, self.server.server_url)
        self.start_bot(bot)

        self.server.add_event(_build_bullet_hit_bot_schema_event(self.server.my_id))
        self.server.reset_bot_intent_latch()
        self.server.set_bot_state_and_await_tick()
        self.server.continue_bot_intent()
        self.await_bot_intent(1000)

        self.assertTrue(latch.wait(3.0), "on_hit_by_bullet should be called")

    # -----------------------------------------------------------------------
    # TCK-017: BulletHitBotEvent(victimId!=myId) triggers onBulletHit
    # -----------------------------------------------------------------------

    @pytest.mark.TR_API_TCK_017
    def test_tck017_bullet_hit_bot_event_other_triggers_on_bullet_hit(self):
        latch = threading.Event()

        class TckBot(Bot):
            def on_bullet_hit(self, event: BulletHitBotEvent):
                latch.set()

        bot = TckBot(self.bot_info, self.server.server_url)
        self.start_bot(bot)

        self.server.add_event(_build_bullet_hit_bot_schema_event(99))
        self.server.reset_bot_intent_latch()
        self.server.set_bot_state_and_await_tick()
        self.server.continue_bot_intent()
        self.await_bot_intent(1000)

        self.assertTrue(latch.wait(3.0), "on_bullet_hit should be called")


# -----------------------------------------------------------------------
# JSON / schema builders (module-level helpers)
# -----------------------------------------------------------------------

def _build_round_ended_json(round_number: int, turn_number: int) -> str:
    return json.dumps({
        "type": "RoundEndedEventForBot",
        "roundNumber": round_number,
        "turnNumber": turn_number,
        "results": {
            "rank": 1, "survival": 0, "lastSurvivorBonus": 0,
            "bulletDamage": 0, "bulletKillBonus": 0, "ramDamage": 0,
            "ramKillBonus": 0, "totalScore": 0,
            "firstPlaces": 0, "secondPlaces": 0, "thirdPlaces": 0,
        },
    })


def _build_game_ended_json(number_of_rounds: int) -> str:
    return json.dumps({
        "type": "GameEndedEventForBot",
        "numberOfRounds": number_of_rounds,
        "results": {
            "rank": 1, "survival": 0, "lastSurvivorBonus": 0,
            "bulletDamage": 0, "bulletKillBonus": 0, "ramDamage": 0,
            "ramKillBonus": 0, "totalScore": 0,
            "firstPlaces": 0, "secondPlaces": 0, "thirdPlaces": 0,
        },
    })


def _build_bullet_hit_bot_schema_event(victim_id: int) -> SchemaBulletHitBotEvent:
    bullet = SchemaBulletState(
        bullet_id=1,
        owner_id=2,
        power=1.0,
        x=50.0,
        y=50.0,
        direction=90.0,
    )
    return SchemaBulletHitBotEvent(
        victim_id=victim_id,
        bullet=bullet,
        damage=4.0,
        energy=96.0,
        turn_number=2,
        type=Message.Type.BULLET_HIT_BOT_EVENT,
    )
