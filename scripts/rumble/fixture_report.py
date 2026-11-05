"""Group the Rumble fixture checks into named cases, write a result file and a report, and remove secrets from both.

This is the second half of the Gradle task `rumbleFixtureCheck`: Gradle runs the offline tests of the Rumble modules,
and this script reads their JUnit result files and the Python checks of this folder, groups them into cases, and writes
`result.json` and `report.md`. It uses only the standard library. It never runs a live check and never touches the
network.

Exit codes: 0 every case passed, 1 a case failed, 2 refused because the live switch is on, 3 the files were not written
because a secret survived redaction.
"""

import argparse
import getpass
import io
import json
import os
import platform
import re
import sys
import unittest
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path

SCHEMA_VERSION = 1
EXIT_PASSED, EXIT_FAILED, EXIT_LIVE_SWITCH, EXIT_SECRET_SURVIVED = 0, 1, 2, 3

# The modules whose offline tests make up the fixture check.
MODULES = ["lib/rumble-protocol", "rumble", "rumble-cli", "lib/rumble", "lib/bot-project"]

# Cases in the order they are reported: (name, title).
CASES = [
    ("environments-and-reset-boundaries", "Environments and reset boundaries"),
    ("sign-in-and-registration", "Sign-in and registration"),
    ("catalog-download-and-staging", "Catalog, download and staging"),
    ("bot-submission-and-progress", "Bot submission and progress"),
    ("battle-worker", "Battle worker and its host"),
    ("credential-boundary", "Credential boundary between host and worker"),
    ("legacy-client-import", "Import of the old client's state"),
    ("command-line", "Command line"),
]
RESULTS_CASE = ("test-results", "Test results")

# Which case a JVM test class belongs to, first match wins. A class that matches none is reported as a problem, so a new
# test cannot vanish from the report by being forgotten here.
CLASS_CASES = [
    (re.compile(r"^(RumbleCli\w*Test|RumbleProgramTest)$"), "command-line"),
    (re.compile(r"^RumbleLegacyImportTest$"), "legacy-client-import"),
    (re.compile(r"^(RumbleCredentialBoundaryTest|WorkerClasspathTest|RumbleProtocolCredentialFieldsTest)$"), "credential-boundary"),
    (re.compile(r"^(RumbleEnvironmentTest|RumbleDependencyRulesTest)$"), "environments-and-reset-boundaries"),
    (re.compile(r"^(RumbleSignIn\w*|RumbleSavedSignInTest|RumbleRegistration\w*|RumbleGitHubApiTest|RumbleTokenSecrecyTest|RumbleAccount\w*Test|RumbleMenuSwitchTest)$"), "sign-in-and-registration"),
    (re.compile(r"^(RumbleCatalogTest|RumbleBotFetchTest|RumbleTreeHashTest|RumbleBotProjectCrossCheckTest|BotProject\w*)$"), "catalog-download-and-staging"),
    (re.compile(r"^(RumbleBotSubmissionTest|RumbleSubmissionPrecheckTest|RumbleSubmissionProgressTest)$"), "bot-submission-and-progress"),
    (re.compile(r"^(RumbleProtocol\w*|Worker\w*Test|RumbleWorkerHostTest)$"), "battle-worker"),
]

NOT_CLAIMED = (
    "A green run says nothing about the sign-in App being installed on a real fork, about public installation "
    "permissions or about a real account signing in. Fixtures cannot prove those; real outside-account experiments "
    "remain separate evidence."
)

SECRET_ENV_NAME = re.compile(r"(?i)(token|secret|password|passwd|credential|api[_-]?key|private[_-]?key)")
TOKEN_SHAPES = [
    re.compile(r"\bgh[pousr]_[A-Za-z0-9]{16,}"),
    re.compile(r"\bgithub_pat_[A-Za-z0-9_]{20,}"),
]
AUTH_HEADER = re.compile(r"(?i)\b(authorization|bearer)\b(\s*[:=]?\s*)(?:(?:bearer|token|basic)\s+)?[^\s\"',;]+")
KEY_VALUE = re.compile(r"(?i)\b((?:secret|token|password|passwd|api[_-]?key|private[_-]?key|credential)\w*)(\"?\s*[=:]\s*\"?)[A-Za-z0-9+/_\-]{12,}")
EMAIL = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")
WINDOWS_PATH = re.compile(r"[A-Za-z]:[\\/][^\s\"'<>|]*")
UNIX_HOME_PATH = re.compile(r"/(?:home|Users)/[^\s\"'<>:]+")
# What the self-check looks for after redaction: the same shapes, and a long key after a secret-sounding word with any
# separator, which the redaction rules deliberately do not remove, so that an unknown shape fails closed.
LOOSE_SECRET = re.compile(r"(?i)\b(secret|token|password|passwd|api[_-]?key|private[_-]?key|credential)\b(?:\W+[a-z]{1,8}){0,2}\W+[A-Za-z0-9+/_\-]{24,}")


class SecretSurvived(Exception):
    """Raised when redacted text still looks like it holds a secret. The message names the kind, never the secret."""


# ---- Redaction ----

def redaction_context(root, environ=None):
    environ = dict(os.environ if environ is None else environ)
    try:
        user = getpass.getuser()
    except Exception:
        user = ""
    return {"environ": environ, "root": str(Path(root).resolve()), "home": str(Path.home()), "user": user}


def _path_forms(path):
    return sorted({path, path.replace("\\", "/"), path.replace("/", "\\")}, key=len, reverse=True)


def redact(text, context):
    """Removes secrets and machine details from [text]. Ordinary text is left unchanged."""
    for name, value in context["environ"].items():
        if SECRET_ENV_NAME.search(name) and len(value) >= 8:
            text = text.replace(value, "<secret>")
    for shape in TOKEN_SHAPES:
        text = shape.sub("<token>", text)
    text = AUTH_HEADER.sub(lambda m: f"{m.group(1)}{m.group(2)}<redacted>", text)
    text = KEY_VALUE.sub(lambda m: f"{m.group(1)}{m.group(2)}<redacted>", text)
    for form in _path_forms(context["root"]):
        text = _strip_root(text, form)
    for form in _path_forms(context["home"]):
        text = text.replace(form, "~")
    text = WINDOWS_PATH.sub("<path>", text)
    text = UNIX_HOME_PATH.sub("<path>", text)
    text = EMAIL.sub("<email>", text)
    if len(context["user"]) >= 3:
        text = re.sub(r"(?i)\b" + re.escape(context["user"]) + r"\b", "<user>", text)
    return text


def _strip_root(text, form):
    """Shows a path inside the repository relative to it, and the repository itself as a dot."""
    pattern = re.compile(re.escape(form) + r"(?=[\\/])([\\/])?", re.IGNORECASE)
    text = pattern.sub("", text)
    return re.sub(re.escape(form), ".", text, flags=re.IGNORECASE)


def scan_for_secrets(text, context):
    """Raises [SecretSurvived] when [text] still looks like it holds a secret or a machine detail."""
    for shape in TOKEN_SHAPES:
        if shape.search(text):
            raise SecretSurvived("a GitHub token shape")
    if re.search(r"(?i)\b(authorization|bearer)\b\s*[:=]?\s*(?!<redacted>)(?:bearer|token|basic)\s+[A-Za-z0-9._~+/=-]{16,}", text):
        raise SecretSurvived("an authorization header")
    if LOOSE_SECRET.search(text):
        raise SecretSurvived("a long key next to a secret-sounding word")
    if EMAIL.search(text):
        raise SecretSurvived("an email address")
    for form in _path_forms(context["home"]):
        if len(form) > 3 and form in text:
            raise SecretSurvived("the home folder")
    for name, value in context["environ"].items():
        if SECRET_ENV_NAME.search(name) and len(value) >= 8 and value in text:
            raise SecretSurvived("the value of a secret environment variable")


# ---- Collecting results ----

def case_of(class_name):
    simple = class_name.rsplit(".", 1)[-1]
    for pattern, case in CLASS_CASES:
        if pattern.match(simple):
            return case
    return None


def collect_jvm(root, modules=MODULES):
    """Reads the JUnit result files of [modules]. Returns (checks, problems, evidence)."""
    checks, problems, evidence = [], [], []
    for module in modules:
        folder = Path(root) / module / "build" / "test-results" / "test"
        files = sorted(folder.glob("TEST-*.xml")) if folder.is_dir() else []
        if not files:
            problems.append(f"No test results were found for {module}.")
            continue
        evidence.append(f"{module}/build/test-results/test")
        for file in files:
            try:
                suite = ET.parse(file).getroot()
            except (ET.ParseError, OSError):
                problems.append(f"The test result file {file.name} in {module} could not be read.")
                continue
            for testcase in suite.iter("testcase"):
                class_name = testcase.get("classname", "")
                name = testcase.get("name", "")
                status, message = "passed", ""
                failure = testcase.find("failure") if testcase.find("failure") is not None else testcase.find("error")
                if failure is not None:
                    status, message = "failed", failure.get("message") or (failure.text or "").strip()
                elif testcase.find("skipped") is not None:
                    status, message = "skipped", testcase.find("skipped").get("message") or ""
                case = case_of(class_name)
                if case is None:
                    problems.append(f"The test class {class_name.rsplit('.', 1)[-1]} in {module} belongs to no case.")
                    continue
                checks.append({"case": case, "test": f"{class_name.rsplit('.', 1)[-1]}.{name.split('(')[0]}", "status": status, "message": message})
    return checks, problems, evidence


class _Collector(unittest.TestResult):
    def __init__(self):
        super().__init__()
        self.rows = []

    def _name(self, test):
        return f"{type(test).__name__}.{test._testMethodName}"

    def addSuccess(self, test):
        self.rows.append((self._name(test), "passed", ""))

    def addFailure(self, test, err):
        self.rows.append((self._name(test), "failed", str(err[1]).splitlines()[0] if str(err[1]) else "failed"))

    def addError(self, test, err):
        self.rows.append((self._name(test), "failed", f"{err[0].__name__}: {err[1]}".splitlines()[0]))

    def addSkip(self, test, reason):
        self.rows.append((self._name(test), "skipped", reason))


def collect_python(scripts_folder):
    """Runs the Python checks of the scripts folder, except the ones of this tool. Returns (checks, problems, evidence)."""
    folder = Path(scripts_folder)
    suite = unittest.TestSuite()
    for path in sorted(folder.glob("test_*.py")):
        if path.name == "test_fixture_report.py":
            continue
        sys.modules.pop(path.stem, None)  # a module of the same name from another folder must not be reused
        suite.addTests(unittest.defaultTestLoader.discover(str(folder), pattern=path.name, top_level_dir=str(folder)))
    collector = _Collector()
    sys.path.insert(0, str(folder))
    try:
        suite.run(collector)
    finally:
        sys.path.remove(str(folder))
    checks = [{"case": "environments-and-reset-boundaries", "test": name, "status": status, "message": message}
              for name, status, message in collector.rows]
    problems = [] if checks else ["No Python checks were found in scripts/rumble."]
    return checks, problems, ["scripts/rumble (run in process)"]


def read_environments(root):
    path = Path(root) / "lib" / "rumble" / "src" / "main" / "resources" / "dev" / "robocode" / "tankroyale" / "rumble" / "environments.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    return [{key: env[key] for key in ("name", "displayName", "botsRepository", "dataRepository", "resettable")} for env in data["environments"]]


def read_versions(root, java_version):
    root = Path(root)
    behavior = re.search(r"CURRENT_BEHAVIOR_VERSION\s*=\s*(\d+)",
                         (root / "lib" / "common" / "src" / "main" / "kotlin" / "dev" / "robocode" / "tankroyale" / "common" / "rules" / "RuleDefaults.kt").read_text(encoding="utf-8"))
    return {
        "tankRoyale": (root / "VERSION").read_text(encoding="utf-8").strip(),
        "behaviorVersion": int(behavior.group(1)) if behavior else None,
        "java": java_version or "unknown",
        "os": f"{platform.system()} {platform.release()}",
        "python": platform.python_version(),
    }


# ---- Building the result and the report ----

def group(checks, problems, evidence, versions, environments, now=None):
    cases = []
    for name, title in CASES:
        mine = [c for c in checks if c["case"] == name]
        failures = [{"test": c["test"], "message": c["message"]} for c in mine if c["status"] == "failed"]
        passed = sum(1 for c in mine if c["status"] == "passed")
        skipped = sum(1 for c in mine if c["status"] == "skipped")
        if passed + len(failures) == 0:
            failures.append({"test": "(case)", "message": "No checks ran in this case."})
        cases.append({"name": name, "title": title, "outcome": "failed" if failures else "passed",
                      "passed": passed, "failed": len([f for f in failures if f["test"] != "(case)"]), "skipped": skipped, "failures": failures})
    if problems:
        name, title = RESULTS_CASE
        cases.append({"name": name, "title": title, "outcome": "failed", "passed": 0, "failed": 0, "skipped": 0,
                      "failures": [{"test": "(results)", "message": p} for p in problems]})
    skipped_live = [{"case": c["case"], "test": c["test"], "reason": c["message"]} for c in checks if c["status"] == "skipped"]
    return {
        "schemaVersion": SCHEMA_VERSION,
        "generatedAt": (now or datetime.now(timezone.utc)).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "outcome": "failed" if any(c["outcome"] == "failed" for c in cases) else "passed",
        "versions": versions,
        "environments": environments,
        "cases": cases,
        "skippedLiveChecks": skipped_live,
        "evidence": evidence,
        "notClaimed": NOT_CLAIMED,
    }


def render_report(result):
    lines = ["# Rumble fixture check", "", f"Outcome: **{result['outcome']}**  ", f"Generated: {result['generatedAt']}", ""]
    v = result["versions"]
    lines += ["## Versions", "",
              f"- Tank Royale {v['tankRoyale']}, engine behavior version {v['behaviorVersion']}",
              f"- Java {v['java']}, Python {v['python']}, {v['os']}", "", "## Environments", ""]
    lines += [f"- {e['displayName']} (`{e['name']}`): `{e['botsRepository']}`, `{e['dataRepository']}`"
              + (", resettable" if e["resettable"] else "") for e in result["environments"]]
    lines += ["", "## Cases", ""]
    for c in result["cases"]:
        lines.append(f"- **{c['title']}**: {c['outcome']} ({c['passed']} passed, {c['failed']} failed, {c['skipped']} skipped)")
    failed = [c for c in result["cases"] if c["failures"]]
    if failed:
        lines += ["", "## Failures", ""]
        for c in failed:
            lines.append(f"### {c['title']}")
            lines += [f"- `{f['test']}`: {f['message'] or 'failed'}" for f in c["failures"]]
            lines.append("")
    lines += ["", "## Live checks skipped by design", ""]
    if result["skippedLiveChecks"]:
        lines += [f"- `{s['test']}` ({s['case']})" for s in result["skippedLiveChecks"]]
    else:
        lines.append("- None.")
    lines += ["", "## Evidence", ""] + [f"- `{e}`" for e in result["evidence"]]
    lines += ["", "## What a green run does not claim", "", result["notClaimed"], "",
              "Both files were redacted automatically. Redaction removes known secret shapes and machine details and fails closed "
              "when a secret survives, but it is not proven complete.", ""]
    return "\n".join(lines)


def write_files(result, out_folder, context):
    """Redacts, scans and writes both files, or writes nothing and raises [SecretSurvived]."""
    text_json = redact(json.dumps(result, indent=2, ensure_ascii=False), context)
    text_report = redact(render_report(result), context)
    for text in (text_json, text_report):
        scan_for_secrets(text, context)
    json.loads(text_json)  # redaction must not break the result file
    out = Path(out_folder)
    out.mkdir(parents=True, exist_ok=True)
    (out / "result.json").write_text(text_json + "\n", encoding="utf-8", newline="\n")
    (out / "report.md").write_text(text_report, encoding="utf-8", newline="\n")


def run(root, out_folder, java_version="", live="false", environ=None, jvm_modules=MODULES, python_checks=True):
    """Runs the whole second half of the fixture check and returns the exit code."""
    if str(live).lower() == "true":
        print("The Rumble fixture check never runs live checks. Run it without -Drumble.live=true.", file=sys.stderr)
        return EXIT_LIVE_SWITCH
    checks, problems, evidence = collect_jvm(root, jvm_modules)
    if python_checks:
        py_checks, py_problems, py_evidence = collect_python(Path(root) / "scripts" / "rumble")
        checks, problems, evidence = checks + py_checks, problems + py_problems, evidence + py_evidence
    result = group(checks, problems, evidence, read_versions(root, java_version), read_environments(root))
    try:
        write_files(result, out_folder, redaction_context(root, environ))
    except SecretSurvived as survived:
        print(f"Nothing was written: the output still contained {survived}.", file=sys.stderr)
        return EXIT_SECRET_SURVIVED
    print(f"Rumble fixture check: {result['outcome']}. See {Path(out_folder) / 'report.md'}")
    return EXIT_PASSED if result["outcome"] == "passed" else EXIT_FAILED


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--root", default=str(Path(__file__).resolve().parents[2]))
    parser.add_argument("--out", required=True)
    parser.add_argument("--java-version", default="")
    parser.add_argument("--live", default="false")
    args = parser.parse_args(argv)
    return run(args.root, args.out, args.java_version, args.live)


if __name__ == "__main__":
    sys.exit(main())
