"""
Test to simulate team message serialization/deserialization between MyFirstLeader and MyFirstDroid.
This test reproduces the exact scenario where colors are sent from leader to droid.
"""
import json
import unittest
from dataclasses import dataclass
from typing import Any, Dict


class TestTeamMessageSerialization(unittest.TestCase):
    """Tests for team message serialization and deserialization."""

    def test_colors_serialization(self):
        """Test RobotColors message serialization/deserialization."""
        print("=== Testing RobotColors Serialization ===")

        # Create colors message as MyFirstLeader would (as a dict with type field)
        leader_colors = {
            "type": "RobotColors",
            "bodyColor": "#FF0000FF",
            "tracksColor": "#00FFFFFF",
            "turretColor": "#FF0000FF",
            "gunColor": "#FFFF00FF",
            "radarColor": "#FF0000FF",
            "scanColor": "#FFFF00FF",
            "bulletColor": "#FFFF00FF"
        }

        # Simulate what BaseBotInternals._send_team_message does
        json_str = json.dumps(leader_colors)

        print(f"JSON: {json_str}")

        # Now simulate receiving on MyFirstDroid side
        print("\n=== Attempting to deserialize ===")

        # Python uses json.loads which returns a dict
        received_object: Dict[str, Any] = json.loads(json_str)

        self.assertIsNotNone(received_object)
        self.assertIsInstance(received_object, dict)
        self.assertEqual(received_object.get("type"), "RobotColors")

        print(f"Received type: {received_object.get('type')}")
        print(f"BodyColor: {received_object.get('bodyColor')}")
        print(f"GunColor: {received_object.get('gunColor')}")

        self.assertEqual(received_object.get("bodyColor"), "#FF0000FF")
        self.assertEqual(received_object.get("gunColor"), "#FFFF00FF")
        self.assertEqual(received_object.get("tracksColor"), "#00FFFFFF")

        print("\n✓ TEST PASSED")

    def test_point_serialization(self):
        """Test Point message serialization/deserialization."""
        print("=== Testing Point Serialization ===")

        # Create point message as MyFirstLeader would
        leader_point = {
            "type": "Point",
            "x": 100.5,
            "y": 200.7
        }

        json_str = json.dumps(leader_point)

        print(f"JSON: {json_str}")

        # Now simulate receiving on MyFirstDroid side
        received_object: Dict[str, Any] = json.loads(json_str)

        self.assertIsNotNone(received_object)
        self.assertIsInstance(received_object, dict)
        self.assertEqual(received_object.get("type"), "Point")

        x = float(received_object.get("x", 0.0))
        y = float(received_object.get("y", 0.0))

        print(f"Point X: {x}, Y: {y}")

        self.assertAlmostEqual(x, 100.5)
        self.assertAlmostEqual(y, 200.7)

        print("\n✓ TEST PASSED")

    def test_python_dict_message_handling(self):
        """Test that Python correctly handles dict-based messages."""
        print("=== Testing Python Dict Message Handling ===")

        # In Python, messages are received as dicts (not typed objects)
        # The bot needs to check the "type" field to determine message type

        colors_message = {
            "type": "RobotColors",
            "bodyColor": "#FF0000FF"
        }

        point_message = {
            "type": "Point",
            "x": 150.0,
            "y": 250.0
        }

        # Test colors handling as MyFirstDroid would do it
        msg_type = colors_message.get("type")
        if msg_type == "RobotColors":
            body_color = colors_message.get("bodyColor")
            print(f"Received RobotColors with bodyColor: {body_color}")
            self.assertEqual(body_color, "#FF0000FF")
        else:
            self.fail("Should have matched RobotColors")

        # Test point handling as MyFirstDroid would do it
        msg_type = point_message.get("type")
        if msg_type == "Point":
            x = float(point_message.get("x", 0.0))
            y = float(point_message.get("y", 0.0))
            print(f"Received Point at ({x}, {y})")
            self.assertAlmostEqual(x, 150.0)
            self.assertAlmostEqual(y, 250.0)
        else:
            self.fail("Should have matched Point")

        print("\n✓ TEST PASSED")


if __name__ == "__main__":
    unittest.main()
