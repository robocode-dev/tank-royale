"""Sanity checks for executable attribution in the Rumble unittest export."""

from pathlib import Path
import tempfile
import unittest

from python_unittest_producer import collect


class PythonProducerTest(unittest.TestCase):
    def export(self, source):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            name = "scripts/rumble/test_example.py"
            (root / "scripts/rumble").mkdir(parents=True)
            (root / name).write_text(source, encoding="utf-8")
            return collect(root, [name])

    def test_Sanity_exports_both_directions_with_class_qualified_subjects(self):
        result = self.export('''import unittest
class Example(unittest.TestCase):
    def test_RDA_010_UnitPositive_works(self): pass
    def test_RDA_011_UnitNegative_refuses(self): pass
    def helper(self): pass
''')
        self.assertEqual([], result["diagnostics"])
        self.assertEqual(["positive", "negative"], [r["direction"] for r in result["references"]])
        self.assertEqual(["RDA-010", "RDA-011"], [r["id"] for r in result["references"]])
        self.assertEqual("Example.test_RDA_010_UnitPositive_works", result["references"][0]["subject"])

    def test_Sanity_reports_malformed_classification_and_ignores_helpers(self):
        result = self.export('''import unittest
class Example(unittest.TestCase):
    def test_RDA_010_UnknownPositive_works(self): pass
    def not_a_test(self): pass
''')
        self.assertEqual([], result["references"])
        self.assertEqual(1, len(result["diagnostics"]))


if __name__ == "__main__":
    unittest.main()
