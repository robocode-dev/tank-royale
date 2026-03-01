# Diagram Generator

This Gradle/Kotlin CLI regenerates the Mermaid diagrams embedded in `schema/schemas/README.md`.

## Usage

Run the generator to update all diagrams:

Windows:
```shell
cd schema/scripts/diagram-gen
..\..\..\gradlew updateSchemaReadme -PschemaReadmePath="C:/Code/tank-royale/schema/schemas/README.md"
```

Linux/macOS:
```bash
cd schema/scripts/diagram-gen
../../../gradlew updateSchemaReadme -PschemaReadmePath="$(cd ../..; pwd)/schemas/README.md"
```
