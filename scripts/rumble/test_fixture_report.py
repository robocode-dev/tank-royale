import contextlib
import io
import json
from pathlib import Path
import tempfile
import unittest

import fixture_report as fr

REPO = Path(__file__).resolve().parents[2]

TOKEN = "ghu_" + "A1b2C3d4E5f6G7h8I9j0K1l2M3n4"
REFRESH = "ghr_" + "Z9y8X7w6V5u4T3s2R1q0P9o8N7m6"


def context(root="C:\\Code\\tank-royale", home="C:\\Users\\alice", user="alice", environ=None):
    return {"environ": environ or {}, "root": root, "home": home, "user": user}


def junit(folder, module, classname, cases):
    """Writes a JUnit result file with cases given as (name, status, message)."""
    results = Path(folder) / module / "build" / "test-results" / "test"
    results.mkdir(parents=True, exist_ok=True)
    body = []
    for name, status, message in cases:
        inner = ""
        if status == "failed":
            inner = f'<failure message="{message}">trace</failure>'
        elif status == "skipped":
            inner = f'<skipped message="{message}"/>'
        body.append(f'<testcase name="{name}()" classname="{classname}" time="0.1">{inner}</testcase>')
    (results / f"TEST-{classname}.xml").write_text(f'<testsuite name="{classname}">{"".join(body)}</testsuite>', encoding="utf-8")


def fake_repo(root):
    """A copy of just what the report reads: versions, behavior version and the environments file."""
    root = Path(root)
    root.mkdir(parents=True, exist_ok=True)
    (root / "VERSION").write_text("9.9.9\n", encoding="utf-8")
    rules = root / "lib" / "common" / "src" / "main" / "kotlin" / "dev" / "robocode" / "tankroyale" / "common" / "rules"
    rules.mkdir(parents=True)
    (rules / "RuleDefaults.kt").write_text("const val CURRENT_BEHAVIOR_VERSION = 7\n", encoding="utf-8")
    target = root / "lib" / "rumble" / "src" / "main" / "resources" / "dev" / "robocode" / "tankroyale" / "rumble"
    target.mkdir(parents=True)
    source = REPO / "lib" / "rumble" / "src" / "main" / "resources" / "dev" / "robocode" / "tankroyale" / "rumble" / "environments.json"
    (target / "environments.json").write_text(source.read_text(encoding="utf-8"), encoding="utf-8")
    (root / "scripts" / "rumble").mkdir(parents=True)
    return root


ALL_CLASSES = {
    "lib/rumble-protocol": [
        ("dev.robocode.tankroyale.rumble.protocol.RumbleProtocolMessagesTest", "testOne"),
        ("dev.robocode.tankroyale.rumble.protocol.RumbleProtocolCredentialFieldsTest", "testEight"),
    ],
    "rumble": [
        ("dev.robocode.tankroyale.rumble.worker.WorkerTest", "testTwo"),
        ("dev.robocode.tankroyale.rumble.worker.WorkerClasspathTest", "testNine"),
    ],
    "lib/rumble": [
        ("dev.robocode.tankroyale.rumble.RumbleEnvironmentTest", "testThree"),
        ("dev.robocode.tankroyale.rumble.RumbleSignInTest", "testFour"),
        ("dev.robocode.tankroyale.rumble.RumbleCatalogTest", "testFive"),
        ("dev.robocode.tankroyale.rumble.RumbleBotSubmissionTest", "testSix"),
        ("dev.robocode.tankroyale.rumble.RumbleCredentialBoundaryTest", "testTen"),
    ],
    "lib/bot-project": [("dev.robocode.tankroyale.botproject.BotProjectStagingTest", "testSeven")],
}


def full_results(root, overrides=None):
    for module, classes in ALL_CLASSES.items():
        for classname, test in classes:
            status = (overrides or {}).get(test, ("passed", ""))
            junit(root, module, classname, [(test, status[0], status[1]), (test + "Extra", "passed", "")])


def python_test_stub(root):
    """One passing Python check in the fake repository's scripts folder."""
    (Path(root) / "scripts" / "rumble" / "test_stub.py").write_text(
        "import unittest\n\nclass Stub(unittest.TestCase):\n    def test_passes(self):\n        self.assertTrue(True)\n", encoding="utf-8")


class GroupingIntoCases(unittest.TestCase):
    def test_RFX_001_UnitPositive_every_check_belongs_to_one_named_case_with_counts(self):
        with tempfile.TemporaryDirectory() as folder:
            root = fake_repo(folder)
            full_results(root, {"testFour": ("skipped", "Set -Drumble.live=true")})
            checks, problems, evidence = fr.collect_jvm(root)

            result = fr.group(checks, problems, evidence, {}, [])

            self.assertEqual([], problems)
            self.assertEqual(["environments-and-reset-boundaries", "sign-in-and-registration", "catalog-download-and-staging",
                              "bot-submission-and-progress", "battle-worker", "credential-boundary"], [c["name"] for c in result["cases"]])
            by_name = {c["name"]: c for c in result["cases"]}
            self.assertEqual(4, by_name["battle-worker"]["passed"])
            self.assertEqual(6, by_name["credential-boundary"]["passed"])
            self.assertEqual(1, by_name["sign-in-and-registration"]["skipped"])
            self.assertEqual(1, by_name["sign-in-and-registration"]["passed"])

    def test_RFX_001_UnitNegative_an_empty_case_a_missing_module_and_an_unassigned_class_fail(self):
        with tempfile.TemporaryDirectory() as folder:
            root = fake_repo(folder)
            junit(root, "lib/rumble", "dev.robocode.tankroyale.rumble.RumbleCatalogTest", [("testFive", "passed", "")])
            junit(root, "lib/bot-project", "dev.robocode.tankroyale.botproject.MysteryTest", [("testNew", "passed", "")])
            (root / "rumble" / "build" / "test-results" / "test").mkdir(parents=True)
            (root / "rumble" / "build" / "test-results" / "test" / "TEST-broken.xml").write_text("<testsuite", encoding="utf-8")

            checks, problems, evidence = fr.collect_jvm(root)
            result = fr.group(checks, problems, evidence, {}, [])

            self.assertTrue(any("lib/rumble-protocol" in p for p in problems))
            self.assertTrue(any("could not be read" in p for p in problems))
            self.assertTrue(any("MysteryTest" in p and "belongs to no case" in p for p in problems))
            by_name = {c["name"]: c for c in result["cases"]}
            self.assertEqual("failed", by_name["sign-in-and-registration"]["outcome"])
            self.assertIn("No checks ran", by_name["sign-in-and-registration"]["failures"][0]["message"])
            self.assertEqual("failed", by_name["test-results"]["outcome"])
            self.assertEqual("failed", result["outcome"])


class ResultAndReport(unittest.TestCase):
    def run_check(self, root, out, **kwargs):
        python_test_stub(root)
        with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
            code = fr.run(root, out, java_version="25", jvm_modules=list(ALL_CLASSES), **kwargs)
        return code

    def test_RFX_002_UnitPositive_the_result_has_versions_environments_cases_and_skipped_live_checks(self):
        with tempfile.TemporaryDirectory() as folder:
            root = fake_repo(Path(folder) / "repo")
            full_results(root, {"testFour": ("skipped", "Set -Drumble.live=true")})

            code = self.run_check(root, Path(folder) / "out")

            self.assertEqual(fr.EXIT_PASSED, code)
            result = json.loads((Path(folder) / "out" / "result.json").read_text(encoding="utf-8"))
            self.assertEqual(1, result["schemaVersion"])
            self.assertEqual({"tankRoyale": "9.9.9", "behaviorVersion": 7, "java": "25"}, {k: result["versions"][k] for k in ("tankRoyale", "behaviorVersion", "java")})
            self.assertIn("os", result["versions"])
            self.assertEqual({"public", "test"}, {e["name"] for e in result["environments"]})
            self.assertTrue(all(set(e) == {"name", "displayName", "botsRepository", "dataRepository", "resettable"} for e in result["environments"]))
            self.assertEqual(["RumbleSignInTest.testFour"], [s["test"] for s in result["skippedLiveChecks"]])
            self.assertEqual("passed", result["outcome"])

    def test_RFX_002_UnitNegative_no_account_or_credential_value_is_in_the_environment_entries(self):
        for env in fr.read_environments(REPO):
            self.assertEqual({"name", "displayName", "botsRepository", "dataRepository", "resettable"}, set(env))

    def test_RFX_003_UnitPositive_the_report_lists_cases_and_always_says_what_a_green_run_does_not_claim(self):
        with tempfile.TemporaryDirectory() as folder:
            root = fake_repo(Path(folder) / "repo")
            full_results(root, {"testFour": ("skipped", "Set -Drumble.live=true")})

            self.run_check(root, Path(folder) / "out")

            report = (Path(folder) / "out" / "report.md").read_text(encoding="utf-8")
            self.assertIn("Outcome: **passed**", report)
            for title, _ in [(t, n) for n, t in fr.CASES]:
                self.assertIn(title, report)
            self.assertIn("What a green run does not claim", report)
            self.assertIn("public installation", report)
            self.assertIn("RumbleSignInTest.testFour", report)

    def test_RFX_003_UnitNegative_the_sentence_is_in_a_failing_report_too(self):
        with tempfile.TemporaryDirectory() as folder:
            root = fake_repo(Path(folder) / "repo")
            full_results(root, {"testSix": ("failed", "expected 1 but was 2")})

            self.run_check(root, Path(folder) / "out")

            report = (Path(folder) / "out" / "report.md").read_text(encoding="utf-8")
            self.assertIn("Outcome: **failed**", report)
            self.assertIn("does not claim", report)
            self.assertIn("expected 1 but was 2", report)


class Redaction(unittest.TestCase):
    def test_RFX_004_UnitPositive_secrets_and_machine_details_are_removed(self):
        ctx = context(environ={"MY_SECRET_THING": "hunter2hunter2", "PATH": "x"})
        text = (
            f"token {TOKEN} and {REFRESH}; Authorization: Bearer abc123def456ghi789 ; api_key=AbCdEf0123456789AbCdEf0123456789\n"
            "value hunter2hunter2 in C:\\Code\\tank-royale\\lib\\rumble\\src\\X.kt and C:\\Users\\alice\\.ssh\\id and D:\\other\\place\\f.txt\n"
            "mail alice@example.com user alice /home/bob/file.txt"
        )

        out = fr.redact(text, ctx)

        for leaked in (TOKEN, REFRESH, "abc123def456ghi789", "AbCdEf0123456789AbCdEf0123456789", "hunter2hunter2", "C:\\Users", "alice", "D:\\other", "/home/bob", "example.com"):
            self.assertNotIn(leaked, out, leaked)
        self.assertIn("lib\\rumble\\src\\X.kt", out)
        self.assertNotIn("C:\\Code", out)

    def test_RFX_004_UnitPositive_a_token_in_a_path_an_address_and_a_stack_trace_is_removed(self):
        ctx = context()
        text = (f"/repos/o/r?access_token={TOKEN}\nhttps://x.test/{REFRESH}/y\n"
                f"\tat dev.Foo.bar(Foo.kt:1) caused by IOException: bad {TOKEN}\nfile C:/Code/tank-royale/{TOKEN}/f")

        out = fr.redact(text, ctx)

        self.assertNotIn(TOKEN, out)
        self.assertNotIn(REFRESH, out)

    def test_RFX_004_UnitNegative_ordinary_text_is_left_unchanged(self):
        ctx = context()
        text = "The RumbleTokenSecrecyTest.testRCL040 case passed: 12 checks, sha256:" + "ab" * 32 + " in lib/rumble/build."

        self.assertEqual(text, fr.redact(text, ctx))


class FailingClosed(unittest.TestCase):
    def test_RFX_005_UnitNegative_text_that_still_looks_like_a_secret_is_never_written(self):
        ctx = context()
        for text, kind in (
            (f"token {TOKEN}", "token shape"),
            ("the secret is Zm9vYmFyYmF6cXV4MDEyMzQ1Njc4OWFiY2RlZg", "secret-sounding word"),
            ("mail someone@example.com", "email"),
            ("folder C:\\Users\\alice\\x", "home folder"),
        ):
            with self.assertRaises(fr.SecretSurvived, msg=text) as raised:
                fr.scan_for_secrets(text, ctx)
            self.assertNotIn("Zm9vYmFy", str(raised.exception))
            self.assertNotIn(TOKEN, str(raised.exception))

    def test_RFX_005_UnitPositive_the_command_ends_with_its_own_code_and_writes_nothing(self):
        with tempfile.TemporaryDirectory() as folder:
            root = fake_repo(Path(folder) / "repo")
            full_results(root, {"testSix": ("failed", "the secret is Zm9vYmFyYmF6cXV4MDEyMzQ1Njc4OWFiY2RlZg")})
            python_test_stub(root)
            err = io.StringIO()

            with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(err):
                code = fr.run(root, Path(folder) / "out", jvm_modules=list(ALL_CLASSES))

            self.assertEqual(fr.EXIT_SECRET_SURVIVED, code)
            self.assertFalse((Path(folder) / "out").exists())
            self.assertNotIn("Zm9vYmFy", err.getvalue())


class LiveSwitchAndFailures(unittest.TestCase):
    def test_RFX_006_UnitNegative_the_live_switch_stops_the_command_before_anything_runs(self):
        with tempfile.TemporaryDirectory() as folder:
            err = io.StringIO()
            with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(err):
                code = fr.run(Path(folder) / "nothing", Path(folder) / "out", live="true")

            self.assertEqual(fr.EXIT_LIVE_SWITCH, code)
            self.assertFalse((Path(folder) / "out").exists())
            self.assertIn("never runs live checks", err.getvalue())

    def test_RFX_006_UnitPositive_with_the_switch_off_live_checks_are_skipped_and_listed(self):
        with tempfile.TemporaryDirectory() as folder:
            root = fake_repo(Path(folder) / "repo")
            full_results(root, {"testFive": ("skipped", "Set -Drumble.live=true")})
            python_test_stub(root)

            with contextlib.redirect_stdout(io.StringIO()):
                code = fr.run(root, Path(folder) / "out", live="false", jvm_modules=list(ALL_CLASSES))

            self.assertEqual(fr.EXIT_PASSED, code)
            result = json.loads((Path(folder) / "out" / "result.json").read_text(encoding="utf-8"))
            self.assertEqual(["RumbleCatalogTest.testFive"], [s["test"] for s in result["skippedLiveChecks"]])

    def test_RFX_007_UnitNegative_a_failed_check_fails_the_command_and_is_fully_reported(self):
        with tempfile.TemporaryDirectory() as folder:
            root = fake_repo(Path(folder) / "repo")
            full_results(root, {"testSix": ("failed", "expected 1 but was 2")})
            python_test_stub(root)

            with contextlib.redirect_stdout(io.StringIO()):
                code = fr.run(root, Path(folder) / "out", jvm_modules=list(ALL_CLASSES))

            self.assertEqual(fr.EXIT_FAILED, code)
            result = json.loads((Path(folder) / "out" / "result.json").read_text(encoding="utf-8"))
            by_name = {c["name"]: c for c in result["cases"]}
            self.assertEqual("failed", by_name["bot-submission-and-progress"]["outcome"])
            self.assertEqual([{"test": "RumbleBotSubmissionTest.testSix", "message": "expected 1 but was 2"}], by_name["bot-submission-and-progress"]["failures"])
            self.assertEqual("passed", by_name["battle-worker"]["outcome"])
            self.assertTrue((Path(folder) / "out" / "report.md").exists())


if __name__ == "__main__":
    unittest.main()
