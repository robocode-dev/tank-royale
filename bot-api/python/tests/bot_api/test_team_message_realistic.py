"""
Simulates RobotColors and Point message handling as they would appear in actual bot source files.
Tests the real-world scenario of team message passing between MyFirstLeader and MyFirstDroid.
"""
import unittest

from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.team_message import (
    serialize_team_message,
    deserialize_team_message,
)

# Import shared message types
from test_utils.team_message_types import Point, RobotColors


class TestTeamMessageRealistic(unittest.TestCase):
    """
    Tests the realistic team message scenario between MyFirstLeader and MyFirstDroid.

    In Python, team messages can now be typed objects (like in Java/C#).
    The bot uses isinstance() checks to determine how to handle messages.
    """

    def test_real_world_scenario(self):
        """Test real-world team message scenario with typed objects."""
        print("=== Testing Real-World Team Message Scenario ===\n")

        # SENDER SIDE (MyFirstLeader)
        print("--- SENDER SIDE (MyFirstLeader) ---")

        # This is how MyFirstLeader creates the message
        colors = RobotColors()
        colors.body_color = Color.RED
        colors.tracks_color = Color.CYAN
        colors.turret_color = Color.RED
        colors.gun_color = Color.YELLOW
        colors.radar_color = Color.RED
        colors.scan_color = Color.YELLOW
        colors.bullet_color = Color.YELLOW

        # This is what BaseBotInternals.send_team_message does
        json_str = serialize_team_message(colors)
        message_type = type(colors).__name__

        print(f"Message Type: {message_type}")
        print(f"JSON Length: {len(json_str)} bytes")
        print(f"JSON: {json_str}\n")

        # RECEIVER SIDE (MyFirstDroid)
        print("--- RECEIVER SIDE (MyFirstDroid) ---")
        print("Simulating EventMapper._map_team_message_event...\n")

        # EventMapper deserializes into typed object
        received = deserialize_team_message(json_str, message_type)

        print(f"Received message type: {type(received).__name__}")
        self.assertIsInstance(received, RobotColors)

        # This is how MyFirstDroid.on_team_message handles it
        if isinstance(received, RobotColors):
            print("\n✓ Successfully identified as RobotColors message (isinstance)")

            print(f"  BodyColor: {received.body_color}")
            print(f"  GunColor: {received.gun_color}")
            print(f"  TracksColor: {received.tracks_color}")

            # Verify the colors are correct Color objects
            self.assertEqual(received.body_color, Color.RED)
            self.assertEqual(received.gun_color, Color.YELLOW)
            self.assertEqual(received.tracks_color, Color.CYAN)

        elif isinstance(received, Point):
            self.fail("Should not be a Point message")
        else:
            self.fail(f"Unknown message type: {type(received)}")

        print("\n✓✓✓ TEST PASSED - Team messages work correctly! ✓✓✓")

    def test_point_message(self):
        """Test Point message with typed objects."""
        print("=== Testing Point Message ===\n")

        # SENDER SIDE
        point = Point(x=100.5, y=200.7)

        json_str = serialize_team_message(point)
        message_type = type(point).__name__
        print(f"JSON: {json_str}\n")

        # RECEIVER SIDE
        received = deserialize_team_message(json_str, message_type)

        self.assertIsInstance(received, Point)

        if isinstance(received, Point):
            print(f"Point X: {received.x}, Y: {received.y}")

            self.assertAlmostEqual(received.x, 100.5)
            self.assertAlmostEqual(received.y, 200.7)

        print("\n✓ TEST PASSED")

    def test_message_type_field_required(self):
        """Test that message type is required for deserialization."""
        print("=== Testing Type Field Requirement ===\n")

        # In the new system, the protocol's message_type field is used
        # to look up the registered class for deserialization

        point = Point(x=1.0, y=2.0)
        json_str = serialize_team_message(point)

        # Deserialize with correct type
        received = deserialize_team_message(json_str, "Point")
        self.assertIsInstance(received, Point)
        print("✓ Point message deserialized correctly with correct type")

        # Deserialize with unknown type falls back to dict
        received_unknown = deserialize_team_message(json_str, "UnknownType")
        self.assertIsInstance(received_unknown, dict)
        print("✓ Unknown type falls back to dict correctly")

        print("\n✓ TEST PASSED")



if __name__ == "__main__":
    unittest.main()
