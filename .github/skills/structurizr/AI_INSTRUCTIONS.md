# AI Agent Instructions for Structurizr Skill

## 🛑 MANDATORY RULE FOR AI AGENTS

**BEFORE doing ANYTHING, read this entire file!**

### ⚠️ CRITICAL: Tools Are Downloaded On-Demand - DO NOT INSTALL MANUALLY

**STOP! DO NOT manually download, install, or fetch any tools.**

All required tools are **automatically downloaded** via `.tools/download-tools.sh`:

- **Structurizr CLI**: Downloaded to `.tools/structurizr-cli/`
- **PlantUML JAR**: Downloaded to `.tools/plantuml.jar`
- **Structurizr DSL JAR**: Symlinked at `.tools/structurizr-dsl.jar`

The `generate.sh` script will **automatically run the download script** if tools are missing.

**IF YOU TRY TO MANUALLY DOWNLOAD OR INSTALL TOOLS, YOU ARE DOING IT WRONG!**

## How to Generate SVG Diagrams

### Step 1: Use the Bundled Script

Call the generation script with the workspace DSL content:

```bash
bash .github/skills/structurizr/generate.sh '<workspace_dsl_content>'
```

### Step 2: Script Behavior

The `generate.sh` script:
1. Performs pre-checks (Java availability, tool presence)
2. **Automatically downloads tools if missing** using `.tools/download-tools.sh`
3. Uses `.tools/structurizr-cli/structurizr.sh` for DSL processing
4. Uses `.tools/plantuml.jar` for SVG generation
5. Outputs SVG files to `/docs-internal/architecture/c4-views/images/`

### Step 3: Verify Output

The script will output:
- Number of diagrams generated
- SVG file names and locations
- Markdown references for the generated diagrams

## Common Mistakes to Avoid

❌ **DO NOT** try to manually download Structurizr CLI  
❌ **DO NOT** try to manually download PlantUML JAR  
❌ **DO NOT** use Docker images  
❌ **DO NOT** install npm packages  
❌ **DO NOT** use `sudo` to install tools  

✅ **DO** use the bundled `generate.sh` script (it handles downloads automatically)  
✅ **DO** verify Java is available (only system requirement)  
✅ **DO** trust the pre-checks in the script  
✅ **DO** let the script download tools on first run  

## Example Usage

```bash
cd /path/to/project
bash .github/skills/structurizr/generate.sh 'workspace "Example" {
    model {
        user = person "User"
        system = softwareSystem "System"
        user -> system "uses"
    }
}'
```

## Tool Locations Reference

Tools are downloaded on-demand to `.tools/` at the project root:

- **Download script**: `.tools/download-tools.sh` ⚙️ VERSION CONTROLLED
- **Generation script**: `.github/skills/structurizr/generate.sh` ⚙️ VERSION CONTROLLED
- **Structurizr CLI**: `.tools/structurizr-cli/` 📦 DOWNLOADED (gitignored)
- **PlantUML JAR**: `.tools/plantuml.jar` 📦 DOWNLOADED (gitignored)
- **Structurizr DSL JAR**: `.tools/structurizr-dsl.jar` 🔗 SYMLINK (created by download script)
- **Output directory**: `docs-internal/architecture/c4-views/images/` 📁 VERSION CONTROLLED

See `.tools/README.md` for detailed tool documentation.

## Pre-Check Validation

The script automatically checks:
1. ✅ Java 11+ is installed and available
2. ✅ Structurizr CLI exists (downloads if missing)
3. ✅ PlantUML JAR exists (downloads if missing)

If Java is missing, the script will report the issue and exit. All other tools are downloaded automatically.

## Summary

**The skill is self-contained and ready to use. Just call the generate.sh script with your DSL content - it will automatically download any missing tools on first run.**
