"""Export directly annotated scoring JUnit methods with stable JVM identities."""

import json
from pathlib import Path
import re
import sys


CLASS_NAME = re.compile(r"\bclass\s+([A-Za-z_][A-Za-z0-9_]*)")
KOTLIN_METHOD = re.compile(r"\bfun\s+(?P<name>`[^`]+`|[A-Za-z_$][A-Za-z0-9_$]*)\s*\(")
CLASSIFIED_NAME = re.compile(
    r"test(?P<prefix>SCR)_(?P<number>[0-9]+[a-z]*)_"
    r"(?P<type>Unit|Integration|E2E|Performance)"
    r"(?P<direction>Positive|Negative)_(?P<description>[A-Za-z0-9_$]+)"
)


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

        pending_test = False
        for line_number, line in enumerate(source.splitlines(), start=1):
            stripped = line.strip()
            if not stripped:
                continue
            if stripped.startswith("@"):
                pending_test = pending_test or stripped == "@Test"
                continue

            method_match = KOTLIN_METHOD.search(line)
            if method_match is not None:
                method_name = method_match.group("name").strip("`")
                if pending_test and method_name.startswith("testSCR_"):
                    subject = f"{class_match.group(1)}.{method_name}"
                    classified = CLASSIFIED_NAME.fullmatch(method_name)
                    if classified is None:
                        report(name, subject, "scoring JUnit method does not follow the classified JVM naming convention")
                    else:
                        references.append({
                            "id": f"{classified.group('prefix')}-{classified.group('number')}",
                            "path": name,
                            "subject": subject,
                            "type": classified.group("type"),
                            "direction": classified.group("direction").lower(),
                        })
                pending_test = False
                continue

            pending_test = False

    return {"references": references, "diagnostics": diagnostics}


if __name__ == "__main__":
    request = json.load(sys.stdin)
    print(json.dumps(collect(Path(request["root"]), request["files"])))
