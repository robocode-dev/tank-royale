"""Export direct Kotest scoring cases using the repository's stable JVM names."""

import json
from pathlib import Path
import re
import sys


TEST_CALL = re.compile(r'\btest\s*\(\s*"(?P<name>[^"\n]+)"\s*\)', re.MULTILINE)
TEST_INVOCATION = re.compile(r"\btest\s*\(")
CLASS_NAME = re.compile(r"\bclass\s+([A-Za-z_][A-Za-z0-9_]*)")
CLASSIFIED_NAME = re.compile(
    r"test(?P<prefix>SCR)(?P<number>[0-9]+[a-z]*)_"
    r"(?P<type>Unit|Integration|E2E|Performance)"
    r"(?P<direction>Positive|Negative)_(?P<description>[A-Za-z0-9_$]+)"
)
TAG = re.compile(r'\bTag\(\s*"(?P<id>[A-Z][A-Z0-9-]*)"\s*\)')


def collect(root, files):
    references = []
    diagnostics = []

    def report(path, subject, message):
        diagnostics.append({"path": path, "subject": subject, "message": message})

    for name in sorted(files):
        if not name.endswith(".kt") or "Scoring" not in Path(name).name or "Test" not in Path(name).name:
            continue
        source = (root / name).read_text(encoding="utf-8")
        class_match = CLASS_NAME.search(source)
        if class_match is None:
            report(name, "", "scoring test source has no directly named test class")
            continue

        calls = list(TEST_CALL.finditer(source))
        if len(calls) != len(list(TEST_INVOCATION.finditer(source))):
            report(name, "", "dynamic or unsupported Kotest test title")
            continue

        for call in calls:
            title = call.group("name")
            if not title.startswith("testSCR"):
                continue
            subject = f"{class_match.group(1)}.{title}"
            classified = CLASSIFIED_NAME.fullmatch(title)
            if classified is None:
                report(name, subject, "scoring test title does not follow the classified JVM naming convention")
                continue

            body_start = source.find("{", call.end())
            if body_start < 0:
                report(name, subject, "Kotest executable has no directly following test body")
                continue
            tags = [match.group("id") for match in TAG.finditer(source[call.end():body_start])]
            expected_id = f"{classified.group('prefix')}-{classified.group('number')}"
            if tags != [expected_id]:
                report(name, subject, f"test title identity {expected_id} does not match its direct Kotest tag {tags}")
                continue

            references.append({
                "id": expected_id,
                "path": name,
                "subject": subject,
                "type": classified.group("type"),
                "direction": classified.group("direction").lower(),
            })

    return {"references": references, "diagnostics": diagnostics}


if __name__ == "__main__":
    request = json.load(sys.stdin)
    print(json.dumps(collect(Path(request["root"]), request["files"])))
