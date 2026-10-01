"""Repository-owned aggregator. JSON output is valid YAML; no PyYAML is needed.

Run: python .clue/evidence/aggregate.py .clue/evidence/producers.json
Each command reads {root, files} on stdin and emits {references, diagnostics}.
This program runs producers explicitly. clue validate never invokes it.
"""
from functools import lru_cache
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import subprocess
import sys
import tempfile


def safe_path(name):
    return (isinstance(name, str) and name and name != "."
            and not any(c in name for c in "\\:\x00")
            and not name.startswith("/")
            and str(PurePosixPath(name)) == name
            and ".." not in PurePosixPath(name).parts)


def segment_tokens(pattern):
    """The escape-free subset of Go path.Match used by repository-safe globs."""
    tokens, i = [], 0
    while i < len(pattern):
        char = pattern[i]
        i += 1
        if char != "[":
            tokens.append((char if char in "*?" else "literal", char))
            continue
        negate = i < len(pattern) and pattern[i] == "^"
        if negate:
            i += 1
        ranges = []
        while i < len(pattern) and pattern[i] != "]":
            low = high = pattern[i]
            if low == "-":
                raise ValueError(f"invalid character class: {pattern}")
            i += 1
            if i < len(pattern) and pattern[i] == "-":
                i += 1
                if i == len(pattern) or pattern[i] in "-]":
                    raise ValueError(f"invalid character class: {pattern}")
                high = pattern[i]
                i += 1
            ranges.append((low, high))
        if not ranges or i == len(pattern):
            raise ValueError(f"invalid character class: {pattern}")
        i += 1
        tokens.append(("class", (negate, ranges)))
    return tokens


def segment_match(pattern, name):
    tokens = segment_tokens(pattern)

    @lru_cache(None)
    def walk(i, j):
        if i == len(tokens):
            return j == len(name)
        kind, value = tokens[i]
        if kind == "*":
            return walk(i + 1, j) or (j < len(name) and walk(i, j + 1))
        if j == len(name):
            return False
        if kind == "class":
            negate, ranges = value
            accepted = any(low <= name[j] <= high for low, high in ranges) != negate
        else:
            accepted = kind == "?" or value == name[j]
        return accepted and walk(i + 1, j + 1)

    return walk(0, 0)


def match(pattern, name):
    p, n = pattern.split("/"), name.split("/")

    @lru_cache(None)
    def walk(i, j):
        if i == len(p):
            return j == len(n)
        if p[i] == "**":
            return walk(i + 1, j) or (j < len(n) and walk(i, j + 1))
        return j < len(n) and segment_match(p[i], n[j]) and walk(i + 1, j + 1)

    return walk(0, 0)


def read(root, name):
    if not safe_path(name):
        raise ValueError(f"unsafe input: {name}")
    current = root
    for part in PurePosixPath(name).parts:
        current /= part
        if current.is_symlink():
            raise ValueError(f"symlink input: {name}")
    return current.read_bytes()


def snapshot(root, producer):
    includes, excludes = producer["include"], producer.get("exclude", [])
    if not includes:
        raise ValueError("producer needs an include scope")
    for pattern in includes + excludes:
        if not safe_path(pattern) or any("**" in p and p != "**" for p in pattern.split("/")):
            raise ValueError(f"unsafe pattern: {pattern}")
        for segment in pattern.split("/"):
            if segment != "**":
                segment_tokens(segment)

    def excluded(name):
        parts = name.split("/")
        return any(match(p, "/".join(parts[:i])) for p in excludes for i in range(1, len(parts) + 1))

    inputs = {}
    for pattern in includes:
        parts = []
        for part in pattern.split("/"):
            if any(c in part for c in "*?["):
                break
            parts.append(part)
        prefix = "/".join(parts)
        start = root / prefix
        current = root
        for part in parts:
            current /= part
            if current.is_symlink():
                raise ValueError(f"symlink scope: {prefix}")
        if not start.exists():
            if len(parts) == len(pattern.split("/")):
                raise ValueError(f"missing exact input: {prefix}")
            continue
        candidates = [start] if start.is_file() else start.rglob("*")
        for candidate in candidates:
            name = candidate.relative_to(root).as_posix()
            if excluded(name):
                continue
            if candidate.is_symlink():
                raise ValueError(f"symlink scope: {name}")
            if not candidate.is_file() or not any(match(p, name) for p in includes):
                continue
            if name == ".clue/evidence.yaml":
                raise ValueError("evidence cannot fingerprint itself")
            data = read(root, name).replace(b"\r\n", b"\n")
            inputs[name] = {"path": name, "sha256": hashlib.sha256(data).hexdigest()}
    return [inputs[name] for name in sorted(inputs)]


def aggregate(root, config_path):
    config_name = config_path.relative_to(root).as_posix()
    config = json.loads(read(root, config_name))
    if set(config) != {"producers"} or not config["producers"]:
        raise ValueError("configuration must declare every producer")
    producers, ids = [], set()
    for entry in config["producers"]:
        if set(entry) - {"id", "framework", "language", "include", "exclude", "command"}:
            raise ValueError("unknown producer configuration field")
        identity = entry["id"]
        if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.-]*", identity) or identity in ids:
            raise ValueError(f"duplicate or invalid producer: {identity}")
        ids.add(identity)
        producer = {k: v for k, v in entry.items() if k != "command"}
        producer["include"] = sorted(set(producer["include"] + [config_name]))
        producer["exclude"] = sorted(producer.get("exclude", []))
        producer["inputs"] = snapshot(root, producer)
        names = {row["path"] for row in producer["inputs"]}
        command = entry["command"]
        if not isinstance(command, list) or not command or not all(isinstance(arg, str) for arg in command):
            raise ValueError(f"producer {identity} needs an argument-array command")
        run = subprocess.run(command, cwd=root, input=json.dumps({"root": str(root), "files": sorted(names)}),
                             text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)
        result = json.loads(run.stdout)
        if set(result) - {"references", "diagnostics"} or "references" not in result:
            raise ValueError(f"producer {identity} returned an invalid result")
        if result.get("diagnostics"):
            raise ValueError(f"producer {identity} diagnostics: {result['diagnostics']}")
        references, seen = [], set()
        for ref in result["references"]:
            if set(ref) - {"id", "path", "subject", "type", "direction"}:
                raise ValueError("unknown reference field")
            if not re.fullmatch(r"[A-Z][A-Z0-9]*(?:-[A-Z][A-Z0-9]*)*-[0-9]+[a-z]*", ref["id"]):
                raise ValueError(f"malformed AC identity: {ref['id']}")
            if ref["path"] not in names or not ref["subject"].strip():
                raise ValueError("reference source/subject is missing")
            if "type" in ref or "direction" in ref:
                if ref.get("type") not in {"Unit", "Integration", "E2E", "Performance"} or ref.get("direction") not in {"positive", "negative"}:
                    raise ValueError("malformed reference classification")
            key = (ref["path"], ref["subject"])
            if key in seen:
                raise ValueError(f"duplicate or conflicting executable: {key}")
            seen.add(key)
            references.append(ref)
        producer["references"] = sorted(references, key=lambda r: (r["path"], r["subject"], r["id"]))
        if producer["inputs"] != snapshot(root, producer):
            raise ValueError(f"producer {identity} inputs changed during export")
        producers.append(producer)
    # Check all producer scopes again: a later producer can modify an earlier input.
    for producer in producers:
        if producer["inputs"] != snapshot(root, producer):
            raise ValueError("inputs changed while aggregating")
    return {"version": 1, "producers": sorted(producers, key=lambda p: p["id"])}


def main():
    root = Path.cwd().resolve()
    config = root / (sys.argv[1] if len(sys.argv) > 1 else ".clue/evidence/producers.json")
    result = aggregate(root, config)
    directory = root / ".clue"
    directory.mkdir(exist_ok=True)
    if directory.is_symlink():
        raise ValueError("unsafe output directory")
    # A valid YAML JSON document avoids a new serializer dependency.
    data = json.dumps(result, indent=2, ensure_ascii=False) + "\n"
    name = None
    try:
        with tempfile.NamedTemporaryFile(mode="w", encoding="utf-8", newline="\n", dir=directory, delete=False) as out:
            name = out.name
            out.write(data)
        os.replace(name, directory / "evidence.yaml")
    finally:
        if name and Path(name).exists():
            Path(name).unlink()


if __name__ == "__main__":
    main()
