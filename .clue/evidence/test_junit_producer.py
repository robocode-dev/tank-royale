"""Sanity checks for executable attribution in the repository's JUnit export."""

from pathlib import Path
import tempfile
import unittest

from junit_scoring_producer import collect


class JunitProducerTest(unittest.TestCase):
    def export(self, source):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            name = "GameTypeDropdownTest.kt"
            (root / name).write_text(source, encoding="utf-8")
            return collect(root, [name])

    def test_Sanity_exports_both_directions_on_executable_methods(self):
        result = self.export('''class GameTypeDropdownTest {
    @Test
    @Tag("GTD-001")
    @Tag("Unit")
    @Tag("Positive")
    fun testGTD001_UnitPositive_selectsPreset() {}
    @Test
    fun testGTD001_UnitNegative_rejectsAlias() {}
}''')
        self.assertEqual([], result["diagnostics"])
        self.assertEqual(["positive", "negative"], [r["direction"] for r in result["references"]])
        self.assertTrue(all(r["id"] == "GTD-001" and r["type"] == "Unit" for r in result["references"]))
        self.assertEqual("GameTypeDropdownTest.testGTD001_UnitPositive_selectsPreset", result["references"][0]["subject"])

    def test_Sanity_does_not_attribute_class_tags_or_unannotated_helpers(self):
        result = self.export('''@Test
@Tag("GTD-001")
class GameTypeDropdownTest {
    fun testGTD001_UnitPositive_helper() {}
    // @Test
    fun testGTD001_UnitNegative_anotherHelper() {}
}''')
        self.assertEqual([], result["references"])

    def test_Sanity_reports_malformed_classification(self):
        result = self.export('''class GameTypeDropdownTest {
    @Test
    fun testGTD001_UnknownPositive_selectsPreset() {}
}''')
        self.assertEqual([], result["references"])
        self.assertEqual(1, len(result["diagnostics"]))


if __name__ == "__main__":
    unittest.main()
