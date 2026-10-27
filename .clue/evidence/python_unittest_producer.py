"""Export Rumble environment unittest methods with stable Python identities."""

import ast
import json
from pathlib import Path
import re
import sys


CLASSIFIED_NAME = re.compile(
    r"test_(?P<prefix>[A-Z]+)_(?P<number>[0-9]+[a-z]*)_"
    r"(?P<type>Unit|Integration|E2E|Performance)"
    r"(?P<direction>Positive|Negative)_(?P<description>[A-Za-z0-9_]+)"
)


def collect(root, files):
    references = []
    diagnostics = []

    for name in sorted(files):
        if not (name.startswith("scripts/rumble/test_") and name.endswith(".py")):
            continue
        tree = ast.parse((root / name).read_text(encoding="utf-8"), filename=name)
        for cls in (n for n in tree.body if isinstance(n, ast.ClassDef)):
            for method in (n for n in cls.body if isinstance(n, ast.FunctionDef)):
                if not method.name.startswith("test_") or method.name.startswith("test_Sanity"):
                    continue
                subject = f"{cls.name}.{method.name}"
                classified = CLASSIFIED_NAME.fullmatch(method.name)
                if classified is None:
                    diagnostics.append({"path": name, "subject": subject,
                                        "message": "unittest method does not follow the classified naming convention"})
                    continue
                references.append({
                    "id": f"{classified.group('prefix')}-{classified.group('number')}",
                    "path": name,
                    "subject": subject,
                    "type": classified.group("type"),
                    "direction": classified.group("direction").lower(),
                })

    return {"references": references, "diagnostics": diagnostics}


if __name__ == "__main__":
    request = json.load(sys.stdin)
    print(json.dumps(collect(Path(request["root"]), request["files"])))
