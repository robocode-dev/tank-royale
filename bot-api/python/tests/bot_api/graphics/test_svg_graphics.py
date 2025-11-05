import unittest

from robocode_tank_royale.bot_api.graphics import SvgGraphics, Point
from robocode_tank_royale.bot_api.color import Color


class TestSvgGraphics(unittest.TestCase):
    def setUp(self) -> None:
        self.graphics = SvgGraphics()

    def test_given_new_graphics_when_to_svg_then_contains_root_svg_and_closed(self):
        svg = self.graphics.to_svg()
        self.assertIn('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 5000 5000">', svg)
        self.assertTrue(svg.strip().endswith("</svg>"))

    def test_given_stroke_set_when_draw_line_then_line_element_with_attributes_present(self):
        self.graphics.set_stroke_color(Color.from_rgb(255, 0, 0))  # RED
        self.graphics.set_stroke_width(2)
        self.graphics.draw_line(10, 20, 30, 40)
        svg = self.graphics.to_svg()
        self.assertIn("<line ", svg)
        self.assertIn('x1="10" ', svg)
        self.assertIn('y1="20" ', svg)
        self.assertIn('x2="30" ', svg)
        self.assertIn('y2="40" ', svg)
        self.assertIn('stroke="#FF0000" ', svg)
        self.assertIn('stroke-width="2" ', svg)

    def test_given_stroke_set_when_draw_rectangle_then_rect_element_with_attributes_present(self):
        self.graphics.set_stroke_color(Color.from_rgb(0, 0, 255))  # BLUE
        self.graphics.set_stroke_width(3)
        self.graphics.draw_rectangle(10, 20, 100, 50)
        svg = self.graphics.to_svg()
        self.assertIn("<rect ", svg)
        self.assertIn('x="10" ', svg)
        self.assertIn('y="20" ', svg)
        self.assertIn('width="100" ', svg)
        self.assertIn('height="50" ', svg)
        self.assertIn('fill="none" ', svg)
        self.assertIn('stroke="#0000FF" ', svg)
        self.assertIn('stroke-width="3" ', svg)

    def test_given_fill_and_stroke_set_when_fill_rectangle_then_rect_element_with_attributes_present(self):
        self.graphics.set_fill_color(Color.from_rgb(0, 128, 0))  # GREEN
        self.graphics.set_stroke_color(Color.from_rgb(255, 0, 0))  # RED
        self.graphics.set_stroke_width(1)
        self.graphics.fill_rectangle(10, 20, 100, 50)
        svg = self.graphics.to_svg()
        self.assertIn("<rect ", svg)
        self.assertIn('x="10" ', svg)
        self.assertIn('y="20" ', svg)
        self.assertIn('width="100" ', svg)
        self.assertIn('height="50" ', svg)
        self.assertIn('fill="#008000" ', svg)
        self.assertIn('stroke="#FF0000" ', svg)
        self.assertIn('stroke-width="1" ', svg)

    def test_given_stroke_set_when_draw_circle_then_circle_element_with_attributes_present(self):
        self.graphics.set_stroke_color(Color.from_rgb(128, 0, 128))  # PURPLE
        self.graphics.set_stroke_width(2)
        self.graphics.draw_circle(100, 100, 50)
        svg = self.graphics.to_svg()
        self.assertIn("<circle ", svg)
        self.assertIn('cx="100" ', svg)
        self.assertIn('cy="100" ', svg)
        self.assertIn('r="50" ', svg)
        self.assertIn('fill="none" ', svg)
        self.assertIn('stroke="#800080" ', svg)
        self.assertIn('stroke-width="2" ', svg)

    def test_given_fill_and_stroke_set_when_fill_circle_then_circle_element_with_attributes_present(self):
        self.graphics.set_fill_color(Color.from_rgb(255, 255, 0))  # YELLOW
        self.graphics.set_stroke_color(Color.from_rgb(255, 165, 0))  # ORANGE
        self.graphics.set_stroke_width(1)
        self.graphics.fill_circle(100, 100, 50)
        svg = self.graphics.to_svg()
        self.assertIn("<circle ", svg)
        self.assertIn('cx="100" ', svg)
        self.assertIn('cy="100" ', svg)
        self.assertIn('r="50" ', svg)
        self.assertIn('fill="#FFFF00" ', svg)
        self.assertIn('stroke="#FFA500" ', svg)
        self.assertIn('stroke-width="1" ', svg)

    def test_given_stroke_set_when_draw_polygon_then_polygon_element_with_attributes_present(self):
        self.graphics.set_stroke_color(Color.from_rgb(0, 0, 0))  # BLACK
        self.graphics.set_stroke_width(2)
        points = [Point(10, 10), Point(50, 10), Point(30, 40)]
        self.graphics.draw_polygon(points)
        svg = self.graphics.to_svg()
        self.assertIn("<polygon ", svg)
        self.assertIn('points="10,10 50,10 30,40" ', svg)
        self.assertIn('fill="none" ', svg)
        self.assertIn('stroke="#000000" ', svg)
        self.assertIn('stroke-width="2" ', svg)

    def test_given_fill_and_stroke_set_when_fill_polygon_then_polygon_element_with_attributes_present(self):
        self.graphics.set_fill_color(Color.from_rgb(0, 0, 255))  # BLUE
        self.graphics.set_stroke_color(Color.from_rgb(0, 0, 0))  # BLACK
        self.graphics.set_stroke_width(1)
        points = [Point(10, 10), Point(50, 10), Point(30, 40)]
        self.graphics.fill_polygon(points)
        svg = self.graphics.to_svg()
        self.assertIn("<polygon ", svg)
        self.assertIn('points="10,10 50,10 30,40" ', svg)
        self.assertIn('fill="#0000FF" ', svg)
        self.assertIn('stroke="#000000" ', svg)
        self.assertIn('stroke-width="1" ', svg)

    def test_given_too_few_points_when_draw_or_fill_polygon_then_no_polygon_is_added(self):
        self.graphics.set_stroke_color(Color.from_rgb(0, 0, 0))
        points = [Point(10, 10), Point(50, 10)]
        self.graphics.draw_polygon(points)
        self.graphics.fill_polygon(points)
        svg = self.graphics.to_svg()
        self.assertNotIn("<polygon ", svg)

    def test_given_stroke_and_font_set_when_draw_text_then_text_element_with_attributes_present(self):
        self.graphics.set_stroke_color(Color.from_rgb(0, 0, 255))
        self.graphics.set_font("Verdana", 24)
        self.graphics.draw_text("Hello World", 100, 200)
        svg = self.graphics.to_svg()
        self.assertIn("<text ", svg)
        self.assertIn('x="100" ', svg)
        self.assertIn('y="200" ', svg)
        self.assertIn('font-family="Verdana" ', svg)
        self.assertIn('font-size="24" ', svg)
        self.assertIn('fill="#0000FF"', svg)
        self.assertIn(">Hello World</text>", svg)

    def test_TR_API_GFX_003_text_is_escaped_in_svg_output(self):
        """TR-API-GFX-003 SvgGraphics text: correct attributes and escaping"""
        self.graphics.set_stroke_color(Color.from_rgb(0, 0, 0))
        self.graphics.set_font("Arial", 12)
        self.graphics.draw_text('5 < 7 & "quote"', 10, 20)
        svg = self.graphics.to_svg()
        self.assertIn(">5 &lt; 7 &amp; &quot;quote&quot;</text>", svg)

    def test_given_various_elements_drawn_when_to_svg_then_counts_match(self):
        self.graphics.set_stroke_color(Color.from_rgb(255, 0, 0))
        self.graphics.draw_line(10, 10, 20, 20)
        self.graphics.set_fill_color(Color.from_rgb(0, 0, 255))
        self.graphics.fill_circle(100, 100, 50)
        svg = self.graphics.to_svg()
        self.assertIn("<line ", svg)
        self.assertIn("<circle ", svg)
        self.assertEqual(svg.count("<line "), 1)
        self.assertEqual(svg.count("<circle "), 1)

    def test_clear(self):
        self.graphics.set_stroke_color(Color.from_rgb(255, 0, 0))
        self.graphics.draw_line(10, 10, 20, 20)
        self.graphics.set_fill_color(Color.from_rgb(0, 0, 255))
        self.graphics.fill_circle(100, 100, 50)
        svg_before = self.graphics.to_svg()
        self.assertIn("<line ", svg_before)
        self.assertIn("<circle ", svg_before)
        self.graphics.clear()
        svg_after = self.graphics.to_svg()
        self.assertNotIn("<line ", svg_after)
        self.assertNotIn("<circle ", svg_after)

    def test_default_stroke_values(self):
        self.graphics.draw_rectangle(10, 20, 100, 50)
        svg = self.graphics.to_svg()
        self.assertIn('stroke="#000000" ', svg)
        self.assertIn('stroke-width="1" ', svg)
        self.graphics.clear()
        self.graphics.draw_circle(100, 100, 50)
        svg = self.graphics.to_svg()
        self.assertIn('stroke="#000000" ', svg)
        self.assertIn('stroke-width="1" ', svg)

    def test_number_formatting(self):
        self.graphics.draw_line(10.123, 20.456, 30.789, 40.987)
        svg = self.graphics.to_svg()
        self.assertIn('x1="10.123" ', svg)
        self.assertIn('y1="20.456" ', svg)
        self.assertIn('x2="30.789" ', svg)
        self.assertIn('y2="40.987" ', svg)
        self.graphics.clear()
        self.graphics.draw_line(10.12345, 20.45678, 30.78912, 40.98765)
        svg = self.graphics.to_svg()
        self.assertIn('x1="10.123" ', svg)
        self.assertIn('y1="20.457" ', svg)
        self.assertIn('x2="30.789" ', svg)
        self.assertIn('y2="40.988" ', svg)

    def test_TR_API_GFX_002_alpha_on_stroke_and_fill_is_applied(self):
        """TR-API-GFX-002 SvgGraphics styles: stroke/fill/alpha apply as expected"""
        # Stroke with alpha
        self.graphics.set_stroke_color(Color.from_rgba(255, 0, 0, 128))
        self.graphics.set_stroke_width(2)
        self.graphics.draw_line(1, 2, 3, 4)
        svg = self.graphics.to_svg()
        self.assertIn('stroke="#FF000080" ', svg)
        self.assertIn('stroke-width="2" ', svg)

        # Fill with alpha
        self.graphics.clear()
        self.graphics.set_fill_color(Color.from_rgba(0, 0, 255, 64))
        self.graphics.set_stroke_color(Color.from_rgb(0, 0, 0))
        self.graphics.fill_rectangle(10, 20, 30, 40)
        svg = self.graphics.to_svg()
        self.assertIn('fill="#0000FF40" ', svg)
        self.assertIn('stroke="#000000" ', svg)

    def test_TR_API_GFX_002_outline_shapes_ignore_fill_and_default_stroke(self):
        """TR-API-GFX-002 SvgGraphics styles: outline shapes not filled and default stroke applied"""
        # Only fill set
        self.graphics.set_fill_color(Color.from_rgb(0, 255, 0))
        self.graphics.draw_rectangle(10, 20, 100, 50)
        svg = self.graphics.to_svg()
        self.assertIn('fill="none" ', svg)
        self.assertIn('stroke="#000000" ', svg)
        self.assertIn('stroke-width="1" ', svg)


if __name__ == "__main__":
    unittest.main()
