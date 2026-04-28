---
description: Generate SVG diagrams from C4 DSL workspace blocks using bundled Structurizr CLI and PlantUML. Use when asked to generate C4 architecture diagrams.
argument-hint: ""
allowed-tools: Bash
version: 1.0.0
authors: Flemming N. Larsen (https://github.com/flemming-n-larsen)
---

# Structurizr C4 DSL → SVG

Generate SVG diagrams from C4 DSL. Java 11+ is the only system requirement — all other tools are downloaded automatically on first run.

## Usage

```bash
bash .github/skills/structurizr/generate.sh '<workspace_dsl_content>'
```

The script auto-downloads Structurizr CLI and PlantUML if missing, then outputs SVG files to `docs-internal/architecture/c4-views/images/`.

## Rules

- ❌ **DO NOT** manually download Structurizr CLI, PlantUML, or any other tool
- ❌ **DO NOT** use Docker, npm, or system package managers
- ✅ **DO** call `generate.sh` — it handles everything
- ✅ **DO** verify Java 11+ is available if the script fails

## Supported DSL Elements

`person`, `softwareSystem`, `container`, `component`, relationships with `->` notation.
