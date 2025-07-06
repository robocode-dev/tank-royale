
import unittest
from robocode_tank_royale.schema import ScannedBotEvent, Message, BulletFiredEvent, BulletState, Color
from robocode_tank_royale.bot_api.internal.json_util import from_json, to_json


class JsonUtilSchemaTest(unittest.TestCase):
    def test_to_json_and_from_json(self):
        event = ScannedBotEvent(
            type=Message.Type.SCANNED_BOT_EVENT,
            turn_number=1,
            scanned_by_bot_id=2,
            scanned_bot_id=3,
            energy=100,
            x=10,
            y=20,
            direction=90,
            speed=5,
        )

        json_str = to_json(event)
        deserialized_event = from_json(json_str)
        assert isinstance(deserialized_event, ScannedBotEvent)
        self.assertEqual(event.type, deserialized_event.type)
        self.assertEqual(event.turn_number, deserialized_event.turn_number)
        self.assertEqual(event.scanned_by_bot_id, deserialized_event.scanned_by_bot_id)
        self.assertEqual(event.scanned_bot_id, deserialized_event.scanned_bot_id)
        self.assertEqual(event.energy, deserialized_event.energy)
        self.assertEqual(event.x, deserialized_event.x)
        self.assertEqual(event.y, deserialized_event.y)
        self.assertEqual(event.direction, deserialized_event.direction)
        self.assertEqual(event.speed, deserialized_event.speed)

    def test_nested_serialization(self):
        bullet = BulletState(
            bullet_id=1,
            owner_id=2,
            power=2.5,
            x=100,
            y=200,
            direction=45,
            color=Color(value="#FF0000"),
        )
        event = BulletFiredEvent(
            type=Message.Type.BULLET_FIRED_EVENT,
            turn_number=1,
            bullet=bullet,
        )

        json_str = to_json(event)
        deserialized_event = from_json(json_str)
        assert isinstance(deserialized_event, BulletFiredEvent)
        assert event.bullet.color is not None
        assert deserialized_event.bullet.color is not None
        self.assertEqual(event.bullet.color.value, deserialized_event.bullet.color.value)


if __name__ == "__main__":
    unittest.main()
