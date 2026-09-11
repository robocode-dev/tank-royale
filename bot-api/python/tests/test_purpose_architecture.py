"""Architecture guard for the one-purpose-per-test contract."""

from __future__ import annotations

import ast
import re
from pathlib import Path

import pytest


AC_MARKER = re.compile(r"(?:[A-Z][A-Z0-9]*_)+\d+[a-z]?$")
GENERIC_PURPOSES = {"Unit", "Sanity", "Arch"}


def _marker_name(node: ast.AST) -> str | None:
    if not isinstance(node, ast.Attribute) or node.attr in {"parametrize", "usefixtures"}:
        return None
    parent = node.value
    if isinstance(parent, ast.Attribute) and parent.attr == "mark" and isinstance(parent.value, ast.Name) and parent.value.id == "pytest":
        return node.attr
    return None


def _is_acceptance_marker(name: str) -> bool:
    return AC_MARKER.fullmatch(name) is not None


def _effective_purposes(names: list[str]) -> list[str]:
    acceptance = [name for name in names if _is_acceptance_marker(name)]
    return acceptance if acceptance else [name for name in names if name in GENERIC_PURPOSES]


def _module_markers(tree: ast.Module) -> list[str]:
    result: list[str] = []
    for node in tree.body:
        assignment = node.value if isinstance(node, ast.Expr) else node
        if not isinstance(assignment, (ast.Assign, ast.AnnAssign)):
            continue
        targets = assignment.targets if isinstance(assignment, ast.Assign) else [assignment.target]
        if not any(isinstance(target, ast.Name) and target.id == "pytestmark" for target in targets):
            continue
        values = assignment.value.elts if isinstance(assignment.value, (ast.List, ast.Tuple, ast.Set)) else [assignment.value]
        result.extend(name for value in values if (name := _marker_name(value)))
    return result


def _test_functions(tree: ast.Module) -> list[ast.FunctionDef | ast.AsyncFunctionDef]:
    return [
        node
        for node in ast.walk(tree)
        if isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef)) and node.name.startswith("test_")
    ]


@pytest.mark.Arch
def test_every_python_test_has_exactly_one_effective_purpose() -> None:
    failures: list[str] = []
    tests_root = Path(__file__).parent

    for path in sorted(tests_root.rglob("test*.py")):
        tree = ast.parse(path.read_text(encoding="utf-8"), filename=str(path))
        fallback = _effective_purposes(_module_markers(tree))
        for function in _test_functions(tree):
            direct_names = [name for decorator in function.decorator_list if (name := _marker_name(decorator))]
            declared = direct_names or _module_markers(tree)
            if len(set(declared)) != len(declared):
                failures.append(f"{path.relative_to(tests_root)}:{function.lineno} repeated purpose declaration -> {declared}")
            direct = _effective_purposes(direct_names)
            effective = direct or fallback
            if len(effective) != 1:
                failures.append(f"{path.relative_to(tests_root)}:{function.lineno} -> {effective}")

    assert not failures, "Python test-purpose violations:\n" + "\n".join(failures)
