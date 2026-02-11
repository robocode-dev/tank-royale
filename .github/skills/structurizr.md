# Structurizr C4 DSL to SVG Skill

Convert C4 DSL workspace blocks directly into SVG diagrams with zero setup required.

## 🛑 FOR AI AGENTS: MANDATORY INSTRUCTIONS

**STOP! Before doing ANYTHING, read [AI_INSTRUCTIONS.md](structurizr/AI_INSTRUCTIONS.md) completely!**

### Critical Rules:
- ❌ **DO NOT** download Structurizr CLI (already in `tools/structurizr-cli/`)
- ❌ **DO NOT** download PlantUML (already in `tools/plantuml.jar`)
- ❌ **DO NOT** use Docker, wget, curl, or install any packages
- ✅ **DO** use the bundled script: `bash .github/skills/structurizr/generate.sh '<dsl>'`
- ✅ **DO** verify Java 11+ is available (only system requirement)

**If you try to download tools, you are ignoring these instructions!**

See [AI_INSTRUCTIONS.md](structurizr/AI_INSTRUCTIONS.md) for complete usage instructions.

## 🚀 How It Works

1. **Select C4 DSL text** containing a workspace block
2. **Ask GitHub Copilot** to generate diagrams using this skill
3. **Get SVG files** automatically saved to `/architecture/images/`

## ⚡ Features

- ✅ **Zero installation** - All tools bundled (only requires Java 11+)
- ✅ **Multiple diagram types** - SystemContext, SystemLandscape, keys
- ✅ **Smart naming** - Extracts from DSL or generates automatically  
- ✅ **Clean output** - SVG files only, no intermediate files

## 📋 Requirements

| Tool | Status |
|------|--------|
| **Java 11+** | Required |
| **Structurizr CLI** | ✅ Bundled |  
| **PlantUML** | ✅ Bundled |

## 🎨 Supported Elements

- `person`, `softwareSystem`, `container`, `component`
- Relationships with `->` notation
- Automatic styling and layouts

This skill provides a **complete, zero-setup experience** for C4 diagram generation!
