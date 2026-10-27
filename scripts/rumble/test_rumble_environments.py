import json
from pathlib import Path
import subprocess
import tempfile
import unittest

import rumble_environments as env


def checkout_with(origin, files=()):
    root = Path(tempfile.mkdtemp())
    subprocess.run(["git", "init", "-q", str(root)], check=True)
    if origin is not None:
        subprocess.run(["git", "-C", str(root), "remote", "add", "origin", origin], check=True)
    for name in files:
        path = root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text("{}", encoding="utf-8")
    return root


class ResetTargetsTestRepositoriesOnly(unittest.TestCase):
    def test_RDA_010_UnitPositive_test_repositories_are_resettable_in_any_url_form(self):
        for reference in (
            "robocode-dev/rumble-data-test",
            "robocode-dev/rumble-bots-test",
            "https://github.com/robocode-dev/rumble-data-test.git",
            "git@github.com:robocode-dev/rumble-data-test.git",
            "https://github.com/Robocode-Dev/Rumble-Data-Test/",
        ):
            with self.subTest(reference):
                self.assertEqual("test", env.require_resettable(reference).name)

    def test_RDA_010_UnitPositive_reset_removes_facts_and_raises_the_generation(self):
        root = checkout_with(
            "https://github.com/robocode-dev/rumble-data-test.git",
            ["results/a.json", "leaderboard/1v1.json", "matchmaking/m.json", "bans.json"])
        self.assertEqual(3, len(env.plan_reset(root)))
        self.assertEqual(1, env.apply_reset(root))
        self.assertEqual(2, env.apply_reset(root))
        self.assertFalse((root / "results" / "a.json").exists())
        self.assertTrue((root / "bans.json").exists())
        self.assertEqual({"generation": 2}, json.loads((root / env.GENERATION_FILE).read_text()))

    def test_RDA_010_UnitPositive_same_environment_repositories_are_accepted_together(self):
        env.require_same_environment(env.TEST, "robocode-dev/rumble-bots-test", "robocode-dev/rumble-data-test")
        env.require_same_environment(env.PUBLIC, "robocode-dev/rumble-bots", "robocode-dev/rumble-data")

    def test_RDA_011_UnitNegative_public_repositories_are_never_resettable(self):
        for reference in (
            "robocode-dev/rumble-data",
            "robocode-dev/rumble-bots",
            "https://github.com/robocode-dev/rumble-data.git",
            "https://github.com/ROBOCODE-DEV/Rumble-Data",
        ):
            with self.subTest(reference), self.assertRaises(env.EnvironmentRefused):
                env.require_resettable(reference)

    def test_RDA_011_UnitNegative_lookalike_and_foreign_repositories_are_refused(self):
        for reference in (
            "robocode-dev/rumble-data-test2",
            "robocode-dev/rumble-data-test-copy",
            "robocode-dev/xrumble-data-test",
            "someone-else/rumble-data-test",
            "robocode-rumble-test/rumble-bots",
            "robocode-dev/rumble-client",
            "not a repository",
            "",
        ):
            with self.subTest(reference), self.assertRaises(env.EnvironmentRefused):
                env.require_resettable(reference)

    def test_RDA_011_UnitNegative_reset_of_a_public_or_unknown_checkout_removes_nothing(self):
        for origin in ("https://github.com/robocode-dev/rumble-data.git", None,
                       "https://github.com/robocode-dev/rumble-bots-test.git"):
            with self.subTest(origin):
                root = checkout_with(origin, ["results/a.json"])
                with self.assertRaises(env.EnvironmentRefused):
                    env.apply_reset(root)
                self.assertTrue((root / "results" / "a.json").exists())
                self.assertFalse((root / env.GENERATION_FILE).exists())

    def test_RDA_011_UnitNegative_cross_environment_repositories_are_refused(self):
        for expected, other in ((env.TEST, "robocode-dev/rumble-data"), (env.PUBLIC, "robocode-dev/rumble-data-test"),
                                (env.TEST, "robocode-dev/rumble-client")):
            with self.subTest(other), self.assertRaises(env.EnvironmentRefused):
                env.require_same_environment(expected, "robocode-dev/rumble-bots-test", other)


if __name__ == "__main__":
    unittest.main()
