# Sphinx configuration for Robocode Tank Royale Bot API (Python)
# This configuration enables Google-style docstrings via Napoleon.

from __future__ import annotations

import os
from pathlib import Path

# -- Project information -----------------------------------------------------

def _find_repo_root(start: Path) -> Path:
    p = start.resolve()
    for parent in [p, *p.parents]:
        if (parent / "VERSION").exists():
            return parent
    # Fallback to start if VERSION not found
    return p

_here = Path(__file__).resolve()
_repo_root = _find_repo_root(_here)

project = "Robocode Tank Royale Bot API (Python)"

# Read version from top-level VERSION file if present
version_file = _repo_root / "VERSION"
release = version = version_file.read_text(encoding="utf-8").strip() if version_file.exists() else "0.0.0"

# -- General configuration ---------------------------------------------------

extensions = [
    "sphinx.ext.autodoc",
    "sphinx.ext.napoleon",  # Google/NumPy style docstrings
    "sphinx.ext.viewcode",
]

# Napoleon settings for Google-style docstrings
napoleon_google_docstring = True
napoleon_numpy_docstring = False
napoleon_include_init_with_doc = True
napoleon_include_private_with_doc = False
napoleon_include_special_with_doc = False
napoleon_use_param = True
napoleon_use_ivar = True

templates_path = ["_templates"]
exclude_patterns: list[str] = [
    "_build",
    "Thumbs.db",
    ".DS_Store",
]

# -- Options for HTML output -------------------------------------------------

html_theme = "alabaster"
html_static_path = ["_static"]
