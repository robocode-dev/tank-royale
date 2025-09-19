"""
Compatibility layer exposing generated schema at `robocode_tank_royale.schema`.
This re-exports all types from the generated module located at
`generated.robocode_tank_royale.tank_royale.schema` so imports like
`from robocode_tank_royale.schema import Color` work during development and tests
without installing the package.
"""
from __future__ import annotations

from importlib import import_module
from pathlib import Path
from typing import Any
import sys

# Ensure the top-level `generated` directory is on sys.path so we can import
# `generated.robocode_tank_royale.tank_royale.schema` as a proper package,
# allowing its relative imports to resolve correctly.
_base = Path(__file__).resolve().parents[3]  # repo root: .../bot-api/python
_gen_dir = _base / "generated"
_gen_dir_str = str(_gen_dir)
if _gen_dir_str not in sys.path:
    sys.path.insert(0, _gen_dir_str)

# Create a lightweight namespace package shim for `generated` if it cannot be imported
try:
    _gen_schema = import_module("generated.robocode_tank_royale.tank_royale.schema")
except ModuleNotFoundError:
    import types
    import sys as _sys
    _generated_pkg = types.ModuleType("generated")
    _generated_pkg.__path__ = [str(_gen_dir)]  # type: ignore[attr-defined]
    _sys.modules.setdefault("generated", _generated_pkg)
    _gen_schema = import_module("generated.robocode_tank_royale.tank_royale.schema")

# Export the names defined by the generated module's __all__
__all__ = list(getattr(_gen_schema, "__all__", []))
for _name in __all__:
    globals()[_name] = getattr(_gen_schema, _name)

# Also expose CLASS_MAP and any other non-__all__ helpers explicitly referenced
CLASS_MAP: dict[str, type[Any]] = getattr(_gen_schema, "CLASS_MAP")

# Provide submodule forwarding so imports like
# `from robocode_tank_royale.schema.results_for_bot import ResultsForBot` work.
# We import each submodule from the generated package and register it under the
# `robocode_tank_royale.schema.*` namespace in sys.modules.
import sys as _sys2
from importlib import import_module as _import_module2
_schema_pkg_dir = _gen_dir / "robocode_tank_royale" / "tank_royale" / "schema"
if _schema_pkg_dir.is_dir():
    for _py in _schema_pkg_dir.glob("*.py"):
        if _py.name == "__init__.py":
            continue
        _modname = _py.stem
        _full_gen_name = f"generated.robocode_tank_royale.tank_royale.schema.{_modname}"
        _module = _import_module2(_full_gen_name)
        _sys2.modules[f"robocode_tank_royale.schema.{_modname}"] = _module

# Ensure this module is also registered as robocode_tank_royale.schema
_sys2.modules.setdefault("robocode_tank_royale.schema", _sys2.modules[__name__])
