# Implementation Tasks: Template-based Booting

- [✓] 1. Update `IBootEntry.kt` and `BootEntry.kt` (in common/booter) to include the `base: String?` property.
- [✓] 2. Create the templates directory at `booter/src/main/resources/templates/`.
- [✓] 3. Add default templates: `jvm.boot`, `dotnet.boot`, `python.boot`.
- [✓] 4. Implement `TemplateManager.kt` to load and parse templates from resources.
- [✓] 5. Implement `TemplateBooter.kt` to handle the replacement logic and `ProcessBuilder` creation.
- [✓] 6. Update `BotBooter.kt` to use `TemplateBooter` when no OS-specific script is found.
- [✓] 7. Update sample bots' JSON configurations to include the `base` property.
    - [✓] `Corners.json` (Java): `"base": "Corners"`
    - [✓] `Corners.json` (C#): `"base": "Corners"`
    - [✓] `Corners.json` (Python): `"base": "Corners"`
- [✓] 8. Verify the changes by booting bots without their platform-specific scripts.
- [✓] 9. Update `sample-bots/java/build.gradle.kts` to skip generating `.cmd` and `.sh` files if `base` is present in the bot configuration.
- [✓] 10. Update `sample-bots/csharp/build.gradle.kts` to skip generating `.cmd` and `.sh` files if `base` is present in the bot configuration.
- [✓] 11. Update `sample-bots/python/build.gradle.kts` to skip generating `.cmd` and `.sh` files if `base` is present in the bot configuration.
- [✓] 12. Verify the changes by running the build and checking the generated archive for missing scripts.
- [✓] 13. Fix `BotBooter.bootTeamMember` to allow template-based booting for team members.
- [✓] 14. Final verification by the user (DO NOT ARCHIVE UNTIL APPROVED).
