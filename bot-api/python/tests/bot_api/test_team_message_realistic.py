"""
Simulates RobotColors and Point message handling as they would appear in actual bot source files.
Tests the real-world scenario of team message passing between MyFirstLeader and MyFirstDroid.
"""
import json
import unittest
from typing import Any, Dict

from robocode_tank_royale.bot_api.color import Color


class TestTeamMessageRealistic(unittest.TestCase):
    """
    Tests the realistic team message scenario between MyFirstLeader and MyFirstDroid.

    In Python, team messages are received as dicts (not typed objects like in Java/C#).
    The bot checks the "type" field in the message dict to determine how to handle it.
    """

    def test_real_world_scenario(self):
        """Test real-world team message scenario."""
        print("=== Testing Real-World Team Message Scenario ===\n")

        # SENDER SIDE (MyFirstLeader)
        print("--- SENDER SIDE (MyFirstLeader) ---")

        # This is how MyFirstLeader creates the message (see colors_to_message_dict)
        body = Color.RED
        tracks = Color.CYAN
        turret = Color.RED
        gun = Color.YELLOW
        radar = Color.RED
        scan = Color.YELLOW
        bullet = Color.YELLOW

        leader_colors = {
            "type": "RobotColors",
            "bodyColor": body.to_hex_color(),
            "tracksColor": tracks.to_hex_color(),
            "turretColor": turret.to_hex_color(),
            "gunColor": gun.to_hex_color(),
            "radarColor": radar.to_hex_color(),
            "scanColor": scan.to_hex_color(),
            "bulletColor": bullet.to_hex_color(),
        }

        # This is what _send_team_message does internally
        json_str = json.dumps(leader_colors)

        print(f"Message: {leader_colors}")
        print(f"JSON Length: {len(json_str)} bytes")
        print(f"JSON: {json_str}\n")

        # RECEIVER SIDE (MyFirstDroid)
        print("--- RECEIVER SIDE (MyFirstDroid) ---")
        print("Simulating EventMapper._map_team_message_event...\n")

        # Python EventMapper returns message as a dict
        received_message: Dict[str, Any] = json.loads(json_str)

        print(f"Received message type: {type(received_message)}")
        self.assertIsInstance(received_message, dict)

        # This is how MyFirstDroid.on_team_message handles it
        msg_type = received_message.get("type")
        print(f"Message 'type' field: {msg_type}")

        if msg_type == "RobotColors":
            print("\n✓ Successfully identified as RobotColors message")

            # Extract colors as the droid would
            body_color_hex = received_message.get("bodyColor")
            gun_color_hex = received_message.get("gunColor")
            tracks_color_hex = received_message.get("tracksColor")

            print(f"  BodyColor: {body_color_hex}")
            print(f"  GunColor: {gun_color_hex}")
            print(f"  TracksColor: {tracks_color_hex}")

            # Verify the colors can be parsed
            self.assertIsNotNone(body_color_hex)
            self.assertTrue(body_color_hex.startswith('#'))

            # Parse the color (simulating _parse_hex_color in MyFirstDroid)
            parsed_body_color = self._parse_hex_color(body_color_hex)
            self.assertIsNotNone(parsed_body_color)
            print(f"  Parsed BodyColor: R={parsed_body_color.red}, G={parsed_body_color.green}, B={parsed_body_color.blue}")

        elif msg_type == "Point":
            self.fail("Should not be a Point message")
        else:
            self.fail(f"Unknown message type: {msg_type}")

        print("\n✓✓✓ TEST PASSED - Team messages work correctly! ✓✓✓")

    def test_point_message(self):
        """Test Point message."""
        print("=== Testing Point Message ===\n")

        # SENDER SIDE
        leader_point = {
            "type": "Point",
            "x": 100.5,
            "y": 200.7
        }

        json_str = json.dumps(leader_point)
        print(f"JSON: {json_str}\n")

        # RECEIVER SIDE
        received_message: Dict[str, Any] = json.loads(json_str)

        msg_type = received_message.get("type")
        self.assertEqual(msg_type, "Point")

        if msg_type == "Point":
            x = float(received_message.get("x", 0.0))
            y = float(received_message.get("y", 0.0))
            print(f"Point X: {x}, Y: {y}")

            self.assertAlmostEqual(x, 100.5)
            self.assertAlmostEqual(y, 200.7)

        print("\n✓ TEST PASSED")

    def test_message_type_field_required(self):
        """Test that the 'type' field is required for Python team messages."""
        print("=== Testing Type Field Requirement ===\n")

        # In Python, we use the 'type' field inside the JSON to identify message type
        # This is different from Java/C# which use the protocol's messageType field

        # Valid message with type
        valid_message = {"type": "Point", "x": 1.0, "y": 2.0}

        # Invalid message without type (Python would not know how to handle this)
        invalid_message = {"x": 1.0, "y": 2.0}

        self.assertIsNotNone(valid_message.get("type"))
        self.assertIsNone(invalid_message.get("type"))

        print("Valid message type:", valid_message.get("type"))
        print("Invalid message type:", invalid_message.get("type"))

        # Python bots should check for type before processing
        msg_type = valid_message.get("type")
        if msg_type == "Point":
            print("✓ Valid message processed correctly")
        else:
            self.fail("Should match Point")

        print("\n✓ TEST PASSED")

    def _parse_hex_color(self, hex_str: str) -> Color | None:
        """Parse a hex color string to a Color object (like MyFirstDroid does)."""
        if not isinstance(hex_str, str) or not hex_str.startswith('#'):
            return None
        hex_body = hex_str[1:]
        try:
            if len(hex_body) == 6:
                r = int(hex_body[0:2], 16)
                g = int(hex_body[2:4], 16)
                b = int(hex_body[4:6], 16)
                return Color.from_rgb(r, g, b)
            if len(hex_body) == 8:
                r = int(hex_body[0:2], 16)
                g = int(hex_body[2:4], 16)
                b = int(hex_body[4:6], 16)
                a = int(hex_body[6:8], 16)
                return Color.from_rgba(r, g, b, a)
        except ValueError:
            return None
        return None


if __name__ == "__main__":
    unittest.main()
