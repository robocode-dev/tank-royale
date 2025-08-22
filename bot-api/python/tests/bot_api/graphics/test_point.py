import unittest

from robocode_tank_royale.bot_api.graphics import Point


class TestPoint(unittest.TestCase):
    def test_given_coordinates_when_constructing_point_then_getters_return_same_values(self):
        x = 10.5
        y = -5.25
        p = Point(x, y)
        self.assertEqual(x, p.x)
        self.assertEqual(y, p.y)

    def test_given_points_when_comparing_equality_then_behave_as_expected(self):
        p1 = Point(1.0, 2.0)
        p2 = Point(1.0, 2.0)
        p3 = Point(1.0, 3.0)
        p4 = Point(3.0, 2.0)
        self.assertEqual(p1, p2)
        self.assertNotEqual(p1, p3)
        self.assertNotEqual(p1, p4)
        self.assertNotEqual(None, p1)
        self.assertNotEqual("Not a point", p1)

    def test_given_equal_points_when_hash_code_then_same_hash_code(self):
        p1 = Point(1.0, 2.0)
        p2 = Point(1.0, 2.0)
        self.assertEqual(hash(p1), hash(p2))

    def test_given_point_when_to_string_then_includes_x_and_y_coordinates(self):
        p = Point(1.0, 2.0)
        s = str(p)
        self.assertIn("x=1.0", s)
        self.assertIn("y=2.0", s)

    def test_given_point_when_checking_reflexive_equality_then_equal_to_itself(self):
        p = Point(1.0, 2.0)
        self.assertEqual(p, p)


if __name__ == "__main__":
    unittest.main()
