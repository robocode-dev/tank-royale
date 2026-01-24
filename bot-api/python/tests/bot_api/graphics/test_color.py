import unittest

from robocode_tank_royale.bot_api.color import Color


class TestColor(unittest.TestCase):
    def test_given_rgba_int_when_from_rgba_value_then_channels_and_roundtrip_match(self):
        rgba_value = 0x112233FF  # R=17, G=34, B=51, A=255
        color = Color.from_rgba_value(rgba_value)
        self.assertEqual(255, color.alpha)
        self.assertEqual(17, color.red)
        self.assertEqual(34, color.green)
        self.assertEqual(51, color.blue)
        self.assertEqual(rgba_value, color.to_rgba())

    def test_given_rgb_when_from_rgb_then_alpha_is_255_and_roundtrip_matches(self):
        color = Color.from_rgb(100, 150, 200)
        self.assertEqual(255, color.alpha)
        self.assertEqual(100, color.red)
        self.assertEqual(150, color.green)
        self.assertEqual(200, color.blue)
        self.assertEqual(0x6496C8FF, color.to_rgba())

    def test_given_rgba_components_when_from_rgba_then_channels_and_roundtrip_match(self):
        color = Color.from_rgba(64, 32, 16, 128)
        self.assertEqual(128, color.alpha)
        self.assertEqual(64, color.red)
        self.assertEqual(32, color.green)
        self.assertEqual(16, color.blue)
        self.assertEqual(0x40201080, color.to_rgba())

    def test_given_base_color_and_alpha_when_from_color_with_alpha_then_has_base_rgb_and_new_alpha(self):
        base = Color.from_rgb(255, 200, 100)
        color = Color.from_color_with_alpha(base, 50)
        self.assertEqual(50, color.alpha)
        self.assertEqual(255, color.red)
        self.assertEqual(200, color.green)
        self.assertEqual(100, color.blue)

    def test_given_out_of_range_rgba_when_from_rgba_then_channels_are_masked_to_byte(self):
        color = Color.from_rgba(300, 300, 300, 300)
        self.assertEqual(300 & 0xFF, color.alpha)
        self.assertEqual(300 & 0xFF, color.red)
        self.assertEqual(300 & 0xFF, color.green)
        self.assertEqual(300 & 0xFF, color.blue)

    def test_given_predefined_colors_when_to_rgba_then_values_match_and_transparent_alpha_is_zero(self):
        self.assertEqual(0x000000FF, Color.BLACK.to_rgba())
        self.assertEqual(0xFFFFFFFF, Color.WHITE.to_rgba())
        self.assertEqual(0x0000FFFF, Color.BLUE.to_rgba())
        self.assertEqual(0x00FF00FF, Color.LIME.to_rgba())
        self.assertEqual(0xFF0000FF, Color.RED.to_rgba())
        self.assertEqual(0, Color.TRANSPARENT.alpha)

    def test_given_colors_when_equals_and_hash_code_then_behave_as_expected(self):
        color1 = Color.from_rgba_value(0x112233FF)
        color2 = Color.from_rgba_value(0x112233FF)
        color3 = Color.from_rgba_value(0x112244FF)
        self.assertEqual(color1, color2)
        self.assertNotEqual(color1, color3)
        self.assertEqual(hash(color1), hash(color2))
        self.assertNotEqual(hash(color1), hash(color3))
        self.assertNotEqual(color1, None)
        self.assertNotEqual(color1, "not-a-color")

    def test_given_opaque_and_transparent_colors_when_str_then_formatted_string_matches(self):
        self.assertEqual("Color(r=100, g=150, b=200)", str(Color.from_rgb(100, 150, 200)))
        self.assertEqual(
            "Color(r=100, g=150, b=200, a=128)", str(Color.from_rgba(100, 150, 200, 128))
        )

    def test_given_opaque_and_transparent_colors_when_to_hex_color_then_hex_string_matches(self):
        self.assertEqual("#112233", Color.from_rgb(17, 34, 51).to_hex_color())
        self.assertEqual("#11223380", Color.from_rgba(17, 34, 51, 128).to_hex_color())

    # --- Tests for from_hex_color() ---

    def test_given_valid_six_digit_hex_color_when_from_hex_color_then_color_matches(self):
        test_cases = [
            ("#000000", 0, 0, 0),
            ("#FFFFFF", 255, 255, 255),
            ("#102030", 16, 32, 48),
            ("#0099CC", 0, 153, 204),
        ]
        for hex_str, expected_r, expected_g, expected_b in test_cases:
            with self.subTest(hex_str=hex_str):
                color = Color.from_hex_color(hex_str)
                self.assertEqual(expected_r, color.red)
                self.assertEqual(expected_g, color.green)
                self.assertEqual(expected_b, color.blue)
                self.assertEqual(255, color.alpha)

    def test_given_valid_three_digit_hex_color_when_from_hex_color_then_color_is_expanded(self):
        test_cases = [
            ("#000", 0, 0, 0),
            ("#FFF", 255, 255, 255),
            ("#09C", 0, 153, 204),
            ("#ABC", 170, 187, 204),
        ]
        for hex_str, expected_r, expected_g, expected_b in test_cases:
            with self.subTest(hex_str=hex_str):
                color = Color.from_hex_color(hex_str)
                self.assertEqual(expected_r, color.red)
                self.assertEqual(expected_g, color.green)
                self.assertEqual(expected_b, color.blue)

    def test_given_valid_eight_digit_hex_color_when_from_hex_color_then_color_has_alpha(self):
        color = Color.from_hex_color("#0099CC80")
        self.assertEqual(0, color.red)
        self.assertEqual(153, color.green)
        self.assertEqual(204, color.blue)
        self.assertEqual(128, color.alpha)

    def test_given_none_when_from_hex_color_then_returns_none(self):
        self.assertIsNone(Color.from_hex_color(None))

    def test_given_invalid_hex_color_when_from_hex_color_then_raises_value_error(self):
        invalid_cases = ["", "000000", "FFFFFF", "#GGG", "#12345", "#1234567", "not-a-color"]
        for invalid_str in invalid_cases:
            with self.subTest(invalid_str=invalid_str):
                with self.assertRaises(ValueError):
                    Color.from_hex_color(invalid_str)

    # --- Tests for from_hex() ---

    def test_given_valid_six_digit_hex_when_from_hex_then_color_matches(self):
        test_cases = [
            ("000000", 0, 0, 0),
            ("FFFFFF", 255, 255, 255),
            ("102030", 16, 32, 48),
            ("0099CC", 0, 153, 204),
            ("FDB975", 253, 185, 117),
        ]
        for hex_str, expected_r, expected_g, expected_b in test_cases:
            with self.subTest(hex_str=hex_str):
                color = Color.from_hex(hex_str)
                self.assertEqual(expected_r, color.red)
                self.assertEqual(expected_g, color.green)
                self.assertEqual(expected_b, color.blue)

    def test_given_valid_three_digit_hex_when_from_hex_then_color_is_expanded(self):
        test_cases = [
            ("000", 0, 0, 0),
            ("FFF", 255, 255, 255),
            ("09C", 0, 153, 204),
        ]
        for hex_str, expected_r, expected_g, expected_b in test_cases:
            with self.subTest(hex_str=hex_str):
                color = Color.from_hex(hex_str)
                self.assertEqual(expected_r, color.red)
                self.assertEqual(expected_g, color.green)
                self.assertEqual(expected_b, color.blue)

    def test_given_valid_eight_digit_hex_when_from_hex_then_color_has_alpha(self):
        color = Color.from_hex("0099CC80")
        self.assertEqual(0, color.red)
        self.assertEqual(153, color.green)
        self.assertEqual(204, color.blue)
        self.assertEqual(128, color.alpha)

    def test_given_invalid_hex_when_from_hex_then_raises_value_error(self):
        invalid_cases = ["", "GGG", "12345", "1234567", "not-a-color"]
        for invalid_str in invalid_cases:
            with self.subTest(invalid_str=invalid_str):
                with self.assertRaises(ValueError):
                    Color.from_hex(invalid_str)

    def test_given_lowercase_hex_when_from_hex_then_color_matches(self):
        color = Color.from_hex("aabbcc")
        self.assertEqual(170, color.red)
        self.assertEqual(187, color.green)
        self.assertEqual(204, color.blue)

    def test_given_same_colors_via_from_hex_when_hash_code_then_matches(self):
        self.assertEqual(
            hash(Color.from_hex("102030")),
            hash(Color.from_rgb(0x10, 0x20, 0x30))
        )
        self.assertEqual(
            hash(Color.from_hex("112233")),
            hash(Color.from_rgb(0x11, 0x22, 0x33))
        )

    def test_given_different_colors_when_hash_code_then_not_equal(self):
        self.assertNotEqual(
            hash(Color.from_rgb(10, 20, 30)),
            hash(Color.from_hex("123456"))
        )

    def test_given_from_hex_and_from_rgb_with_same_values_when_equals_then_true(self):
        self.assertEqual(Color.from_hex("FDB975"), Color.from_rgb(0xFD, 0xB9, 0x75))


if __name__ == "__main__":
    unittest.main()
