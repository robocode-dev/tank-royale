## 1. Booter Discovery Updates

- [x] 1.1 Modify `DirCommand.listBotDirectories()` to include all directories regardless of `.json` file presence
- [x] 1.2 Update `Command.getBootEntry()` to return a default `BootEntry` when the `.json` file is missing
- [x] 1.3 Implement heuristic platform detection in `Command` or `TemplateBooter` based on file extensions (`.jar`, `.py`, `.dll`, etc.)
- [x] 1.4 Ensure `DirCommand` handles filtering (game types, bots vs teams) correctly for bots without `.json`

## 2. Bot API Runtime Validation

- [x] 2.1 Update Java Bot API: Add validation for `name`, `version`, and `authors` in `BaseBotInternals` before connection
- [x] 2.2 Update Python Bot API: Add validation for `name`, `version`, and `authors` in `base_bot_internals.py` before connection
- [x] 2.3 Update .NET Bot API: Add validation for `name`, `version`, and `authors` in `BaseBotInternals.cs` before connection
- [x] 2.4 Verify all APIs throw `BotException` with clear messages when validation fails
- [ ] 2.5 Update `BotException` messages to explain that metadata is required for bot recognition during booting and when joining the game

## 3. Test Bots & Verification

- [x] 3.1 Use existing `bot-api/tests/bots` for storing new test bots
- [x] 3.2 Create a Java test bot in `bot-api/tests/bots/java/ConfigLessBot` that sets properties in code and has no `.json` file
- [x] 3.3 Create a Python test bot in `bot-api/tests/bots/python/ConfigLessBot` that sets properties in code and has no `.json` file
- [x] 3.4 Create a C# test bot in `bot-api/tests/bots/dotnet/ConfigLessBot` that sets properties in code and has no `.json` file
- [x] 3.5 Use `BattleRunner` as-is to verify the `booter` correctly identifies and boots these bots
- [x] 3.6 Verify the bots successfully connect to the server and play a game via `BattleRunner`
- [x] 3.7 Create negative test cases for Java, Python, and C# where required properties are missing, and verify `BotException` is thrown with the correct message
- [x] 3.8 Add integration tests using `BattleRunner` to verify `BotException` behavior when booting a "misconfigured" bot directory
- [ ] 3.9 Ensure `BattleRunner` tests are added to the CI/CD pipeline if applicable

## 4. Documentation

- [x] 4.1 Update bot development guides to explain that `.json` is optional
- [x] 4.2 Document how to set required properties in code for each supported language
- [x] 4.3 Add a note about runtime validation and `BotException`

## 5. VitePress Documentation Updates (docs-build)

- [x] 5.1 Update `booter.md` to explain `.json` and script optionality
- [x] 5.2 Update `my-first-bot.md` tutorials for Java, Python, and .NET
- [x] 5.3 Ensure all mentions of required `.json` files are corrected to "optional"

## 6. MyFirstBot Example Updates

- [x] 6.1 Update Java `MyFirstBot` documentation/samples to show property setting in code
- [x] 6.2 Update Python `MyFirstBot` documentation/samples to show property setting in code
- [x] 6.3 Update C# `MyFirstBot` documentation/samples to show property setting in code
