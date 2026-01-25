"""
Test to simulate team message serialization/deserialization between MyFirstLeader and MyFirstDroid.
This test reproduces the exact scenario where colors are sent from leader to droid.
"""
import unittest
from dataclasses import dataclass

from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.team_message import (
    team_message_type,
    register_team_message_type,
    serialize_team_message,
    deserialize_team_message,
    get_team_message_type,
)

# Import shared message types
from test_utils.team_message_types import Point, RobotColors


class TestTeamMessageSerialization(unittest.TestCase):
    """Tests for team message serialization and deserialization."""

    def test_colors_serialization_and_deserialization(self):
        """Test RobotColors message serialization/deserialization with typed objects."""
        print("=== Testing RobotColors Serialization (Typed) ===")

        # Create colors as MyFirstLeader would
        colors = RobotColors()
        colors.body_color = Color.RED
        colors.tracks_color = Color.CYAN
        colors.turret_color = Color.RED
        colors.gun_color = Color.YELLOW
        colors.radar_color = Color.RED
        colors.scan_color = Color.YELLOW
        colors.bullet_color = Color.YELLOW

        # Serialize as BaseBotInternals.send_team_message does
        json_str = serialize_team_message(colors)
        message_type = type(colors).__name__

        print(f"JSON: {json_str}")
        print(f"Message Type: {message_type}")

        # Deserialize as EventMapper._map_team_message_event does
        received = deserialize_team_message(json_str, message_type)

        # Verify it's a typed RobotColors object
        self.assertIsInstance(received, RobotColors)
        self.assertEqual(received.body_color, Color.RED)
        self.assertEqual(received.tracks_color, Color.CYAN)
        self.assertEqual(received.gun_color, Color.YELLOW)

        print(f"Received type: {type(received).__name__}")
        print(f"BodyColor: {received.body_color}")
        print(f"GunColor: {received.gun_color}")

        print("\n✓ TEST PASSED")

    def test_point_serialization_and_deserialization(self):
        """Test Point message serialization/deserialization with typed objects."""
        print("=== Testing Point Serialization (Typed) ===")

        # Create point as MyFirstLeader would
        point = Point(x=100.5, y=200.7)

        json_str = serialize_team_message(point)
        message_type = type(point).__name__

        print(f"JSON: {json_str}")
        print(f"Message Type: {message_type}")

        # Deserialize
        received = deserialize_team_message(json_str, message_type)

        # Verify it's a typed Point object
        self.assertIsInstance(received, Point)
        self.assertAlmostEqual(received.x, 100.5)
        self.assertAlmostEqual(received.y, 200.7)

        print(f"Point X: {received.x}, Y: {received.y}")

        print("\n✓ TEST PASSED")

    def test_isinstance_message_handling(self):
        """Test that messages can be checked with isinstance() like Java/C#."""
        print("=== Testing isinstance() Message Handling ===")

        # Create and serialize messages
        colors = RobotColors(body_color=Color.RED)
        point = Point(x=150.0, y=250.0)

        colors_json = serialize_team_message(colors)
        point_json = serialize_team_message(point)

        # Deserialize
        received_colors = deserialize_team_message(colors_json, "RobotColors")
        received_point = deserialize_team_message(point_json, "Point")

        # Test isinstance handling as MyFirstDroid would do
        if isinstance(received_colors, RobotColors):
            print(f"Received RobotColors with bodyColor: {received_colors.body_color}")
            self.assertEqual(received_colors.body_color, Color.RED)
        else:
            self.fail("Should have matched RobotColors")

        if isinstance(received_point, Point):
            print(f"Received Point at ({received_point.x}, {received_point.y})")
            self.assertAlmostEqual(received_point.x, 150.0)
            self.assertAlmostEqual(received_point.y, 250.0)
        else:
            self.fail("Should have matched Point")

        print("\n✓ TEST PASSED")

    def test_register_team_message_type_function(self):
        """Test that register_team_message_type() works as alternative to decorator."""
        print("=== Testing register_team_message_type() function ===")

        @dataclass
        class CustomMessage:
            value: int

        # Register using function instead of decorator
        register_team_message_type(CustomMessage)

        # Verify it's registered
        self.assertEqual(get_team_message_type("CustomMessage"), CustomMessage)

        # Test serialization/deserialization
        msg = CustomMessage(value=42)
        json_str = serialize_team_message(msg)
        received = deserialize_team_message(json_str, "CustomMessage")

        self.assertIsInstance(received, CustomMessage)
        self.assertEqual(received.value, 42)

        print("\n✓ TEST PASSED")

    def test_unregistered_type_returns_dict(self):
        """Test that unregistered types fall back to dictionary."""
        print("=== Testing unregistered type fallback ===")

        json_str = '{"someField": "someValue"}'
        received = deserialize_team_message(json_str, "UnknownType")

        self.assertIsInstance(received, dict)
        self.assertEqual(received["someField"], "someValue")

        print("\n✓ TEST PASSED")

    def test_color_serialization_format(self):
        """Test that colors are serialized to hex strings."""
        print("=== Testing Color serialization format ===")

        colors = RobotColors(body_color=Color.RED)
        json_str = serialize_team_message(colors)

        # Should contain hex color string
        self.assertIn("#FF0000", json_str)

        print(f"Serialized: {json_str}")
        print("\n✓ TEST PASSED")


if __name__ == "__main__":
    unittest.main()
