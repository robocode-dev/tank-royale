import pytest
from unittest.mock import MagicMock
from robocode_tank_royale.bot_api.events import *
from robocode_tank_royale.bot_api.internal.event_queue import EventQueue
from robocode_tank_royale.bot_api.internal.bot_event_handlers import BotEventHandlers

@pytest.fixture
def mock_internals():
    internals = MagicMock()
    internals.conditions = []
    tick = MagicMock()
    tick.turn_number = 1
    internals.current_tick_or_throw = tick
    return internals

# TR-API-EVT-001: All event class constructors store correct fields

def test_TR_API_EVT_001_event_constructors():
    # TickEvent
    te = TickEvent(turn_number=1, round_number=2, bot_state=None, bullet_states=[], events=[])
    assert te.turn_number == 1
    assert te.round_number == 2
    
    # ScannedBotEvent
    sbe = ScannedBotEvent(turn_number=3, scanned_by_bot_id=1, scanned_bot_id=2, energy=80.0, x=100.0, y=200.0, direction=45.0, speed=5.0)
    assert sbe.turn_number == 3
    assert sbe.scanned_by_bot_id == 1
    assert sbe.scanned_bot_id == 2
    assert sbe.energy == 80.0
    assert sbe.x == 100.0
    assert sbe.y == 200.0
    assert sbe.direction == 45.0
    assert sbe.speed == 5.0

    # HitBotEvent
    hbe = HitBotEvent(turn_number=4, victim_id=5, energy=90.0, x=10.0, y=20.0, rammed=True)
    assert hbe.turn_number == 4
    assert hbe.victim_id == 5
    assert hbe.energy == 90.0
    assert hbe.x == 10.0
    assert hbe.y == 20.0
    assert hbe.rammed is True

    # HitByBulletEvent
    bullet = MagicMock()
    hbbe = HitByBulletEvent(turn_number=6, bullet=bullet, damage=5.0, energy=95.0)
    assert hbbe.turn_number == 6
    assert hbbe.bullet == bullet
    assert hbbe.damage == 5.0
    assert hbbe.energy == 95.0

    # HitWallEvent
    hwe = HitWallEvent(turn_number=7)
    assert hwe.turn_number == 7

    # BulletFiredEvent
    bfe = BulletFiredEvent(turn_number=8, bullet=bullet)
    assert bfe.turn_number == 8
    assert bfe.bullet == bullet

    # BulletHitBotEvent
    bhbe = BulletHitBotEvent(turn_number=9, victim_id=10, bullet=bullet, damage=5.0, energy=90.0)
    assert bhbe.turn_number == 9
    assert bhbe.bullet == bullet
    assert bhbe.victim_id == 10
    assert bhbe.damage == 5.0
    assert bhbe.energy == 90.0

    # BulletHitBulletEvent
    other_bullet = MagicMock()
    bhbue = BulletHitBulletEvent(turn_number=10, bullet=bullet, hit_bullet=other_bullet)
    assert bhbue.turn_number == 10
    assert bhbue.bullet == bullet
    assert bhbue.hit_bullet == other_bullet

    # BulletHitWallEvent
    bhwe = BulletHitWallEvent(turn_number=11, bullet=bullet)
    assert bhwe.turn_number == 11
    assert bhwe.bullet == bullet

    # BotDeathEvent
    bde = BotDeathEvent(turn_number=12, victim_id=13)
    assert bde.turn_number == 12
    assert bde.victim_id == 13

    # DeathEvent
    de = DeathEvent(14)
    assert de.turn_number == 14

    # SkippedTurnEvent
    ste = SkippedTurnEvent(15)
    assert ste.turn_number == 15

    # WonRoundEvent
    wre = WonRoundEvent(16)
    assert wre.turn_number == 16

    # TeamMessageEvent
    tme = TeamMessageEvent(17, "hello", 18)
    assert tme.turn_number == 17
    assert tme.message == "hello"
    assert tme.sender_id == 18

    # CustomEvent
    condition = MagicMock()
    ce = CustomEvent(19, condition)
    assert ce.turn_number == 19
    assert ce.condition == condition

# TR-API-EVT-005: EventQueue dispatches events in priority order

def test_TR_API_EVT_005_event_queue_priority(mock_internals):
    handlers = MagicMock(spec=BotEventHandlers)
    queue = EventQueue(mock_internals, handlers)
    
    # Priority order should be:
    # 1. Critical events (higher priority first)
    # 2. Non-critical events (higher priority first)
    
    wre = WonRoundEvent(turn_number=1) # Critical, Priority 150
    de = DeathEvent(turn_number=1)     # Critical, Priority 10
    sbe = ScannedBotEvent(turn_number=1, scanned_by_bot_id=1, scanned_bot_id=2, energy=100, x=100, y=100, direction=0, speed=0) # Non-critical, Priority 20
    
    queue.add_event(de)
    queue.add_event(wre)
    queue.add_event(sbe)
    
    queue.dispatch_events(1)
    
    calls = handlers.fire_event.call_args_list
    assert len(calls) == 3
    assert isinstance(calls[0][0][0], WonRoundEvent)
    assert isinstance(calls[1][0][0], DeathEvent)
    assert isinstance(calls[2][0][0], ScannedBotEvent)

# TR-API-EVT-006: EventQueue removes non-critical events older than MAX_EVENT_AGE turns

def test_TR_API_EVT_006_event_queue_age_culling(mock_internals):
    handlers = MagicMock(spec=BotEventHandlers)
    queue = EventQueue(mock_internals, handlers)
    
    # MAX_EVENT_AGE = 2
    # Current turn = 10
    # Event at turn 7 is too old (10 - 7 = 3 > 2)
    # Event at turn 8 is fine (10 - 8 = 2 <= 2)
    
    old_event = ScannedBotEvent(turn_number=7, scanned_by_bot_id=1, scanned_bot_id=2, energy=100, x=100, y=100, direction=0, speed=0)
    fine_event = ScannedBotEvent(turn_number=8, scanned_by_bot_id=1, scanned_bot_id=2, energy=100, x=100, y=100, direction=0, speed=0)
    old_critical = WonRoundEvent(turn_number=7)
    
    queue.add_event(old_event)
    queue.add_event(fine_event)
    queue.add_event(old_critical)
    
    queue.dispatch_events(10)
    
    calls = handlers.fire_event.call_args_list
    # old_event should be culled, fine_event and old_critical should remain
    assert len(calls) == 2
    assert isinstance(calls[0][0][0], WonRoundEvent)
    assert isinstance(calls[1][0][0], ScannedBotEvent)

# TR-API-EVT-007: EventQueue size does not exceed MAX_QUEUE_SIZE (256)

def test_TR_API_EVT_007_event_queue_size_cap(mock_internals):
    handlers = MagicMock(spec=BotEventHandlers)
    queue = EventQueue(mock_internals, handlers)
    
    for i in range(EventQueue.MAX_QUEUE_SIZE + 10):
        queue.add_event(SkippedTurnEvent(turn_number=i))
        
    assert len(queue.events) == EventQueue.MAX_QUEUE_SIZE

# TR-API-EVT-008: Condition.test() is callable and subclass can override return value

def test_TR_API_EVT_008_condition_test_callable():
    c1 = Condition(callable=lambda: True)
    assert c1.test() is True
    
    c2 = Condition(callable=lambda: False)
    assert c2.test() is False
    
    class MyCondition(Condition):
        def test(self):
            return True
    
    c3 = MyCondition()
    assert c3.test() is True

# TR-API-EVT-009: CustomEvent fires when its Condition.test() returns true

def test_TR_API_EVT_009_custom_event_firing(mock_internals):
    handlers = MagicMock(spec=BotEventHandlers)
    mock_internals.current_tick_or_throw.turn_number = 5
    queue = EventQueue(mock_internals, handlers)
    
    cond_true = Condition(name="true", callable=lambda: True)
    cond_false = Condition(name="false", callable=lambda: False)
    
    mock_internals.conditions = [cond_true, cond_false]
    
    queue.dispatch_events(5)
    
    calls = handlers.fire_event.call_args_list
    assert len(calls) == 1
    assert isinstance(calls[0][0][0], CustomEvent)
    assert calls[0][0][0].condition == cond_true
