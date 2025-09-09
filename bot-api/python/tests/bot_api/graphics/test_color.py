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


if __name__ == "__main__":
    unittest.main()
