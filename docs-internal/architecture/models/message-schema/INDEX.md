# INDEX — message-schema/

| File | Coverage | Purpose |
|------|----------|---------|
| [README.md](./README.md) | Overview | Message schema entry point and navigation |
| [handshakes.md](./handshakes.md) | 5 message types | Connection establishment (BotHandshake, ServerHandshake, ObserverHandshake, ControllerHandshake, BotReady) |
| [commands.md](./commands.md) | 9 command types | Controller operations (StartGame, StopGame, PauseGame, ResumeGame, NextTurn, ChangeTps, EnableDebugMode, DisableDebugMode, BotPolicyUpdate) |
| [events.md](./events.md) | 26 event types | Server-to-client notifications (TickEvent, GameStartedEvent, BotDeathEvent, etc.) |
| [intents.md](./intents.md) | 2 intent types | Bot action messages (BotIntent, TeamMessageIntent) |
| [state.md](./state.md) | 13 DTO schemas | Shared state data transfer objects (BotState, BulletState, GameState, etc.) |
