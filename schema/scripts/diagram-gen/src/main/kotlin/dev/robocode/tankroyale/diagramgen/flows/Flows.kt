package dev.robocode.tankroyale.diagramgen.flows

import dev.robocode.tankroyale.diagramgen.data.*

object Flows {

    val all: List<Flow> = listOf(
        Flow(
            id = "bot-joining",
            title = "Bot joining",
            description = "The handshake flow when a bot connects to the server.",
            participants = listOf("Bot", "Server", "Observer", "Controller"),
            steps = listOf(
                NoteStep(listOf("Bot"), "WebSocket connection is opened"),
                MessageStep("Bot", "Server", "<<event>> connection established", Arrow.SYNC, activateTarget = true),
                MessageStep("Server", "Bot", "server-handshake (session-id)", Arrow.SYNC, deactivateTarget = true),
                MessageStep("Bot", "Server", "bot-handshake (session-id, secret, boot-id)", Arrow.SYNC),
                ConditionStep(
                    branches = listOf(
                        branch("if session-id or secret is invalid",
                            MessageStep("Server", "Bot", "disconnect")
                        ),
                        branch("else",
                            NoteStep(listOf("Server"), "Produces: <<event>> Bot joined"),
                            MessageStep("Server", "Observer", "bot-list-update"),
                            MessageStep("Server", "Controller", "bot-list-update"),
                            NoteStep(listOf("Server"), "New bot is considered for future start-game requests")
                        )
                    )
                )
            )
        ),
        Flow(
            id = "bot-leaving",
            title = "Bot leaving",
            description = "When a bot disconnects, observers/controllers receive updated bot list.",
            participants = listOf("Bot", "Server", "Observer", "Controller"),
            steps = listOf(
                MessageStep("Bot", "Server", "<<event>> disconnected"),
                NoteStep(listOf("Server"), "Produces: <<event>> Bot left"),
                MessageStep("Server", "Observer", "bot-list-update"),
                MessageStep("Server", "Controller", "bot-list-update"),
                optional("if no bots remain while game running",
                    NoteStep(listOf("Server"), "Server aborts game"),
                    MessageStep("Server", "Bot", "game-aborted-event"),
                    MessageStep("Server", "Observer", "game-aborted-event"),
                    MessageStep("Server", "Controller", "game-aborted-event")
                )
            )
        ),
        Flow(
            id = "observer-joining",
            title = "Observer joining",
            description = "Handshake for observers joining the server.",
            participants = listOf("Observer", "Server"),
            steps = listOf(
                NoteStep(listOf("Observer"), "WebSocket connection is opened"),
                MessageStep("Observer", "Server", "<<event>> connection established", Arrow.SYNC, activateTarget = true),
                MessageStep("Server", "Observer", "server-handshake (session-id)", deactivateTarget = true),
                MessageStep("Observer", "Server", "observer-handshake (session-id, secret)", Arrow.SYNC),
                ConditionStep(
                    branches = listOf(
                        branch("if session-id or secret is invalid",
                            MessageStep("Server", "Observer", "disconnect")
                        ),
                        branch("else",
                            NoteStep(listOf("Server"), "Produces: <<event>> Observer joined"),
                            MessageStep("Server", "Observer", "bot-list-update")
                        )
                    )
                )
            )
        ),
        Flow(
            id = "controller-joining",
            title = "Controller joining",
            description = "Handshake for controllers joining the server.",
            participants = listOf("Controller", "Server"),
            steps = listOf(
                NoteStep(listOf("Controller"), "WebSocket connection is opened"),
                MessageStep("Controller", "Server", "<<event>> connection established", Arrow.SYNC, activateTarget = true),
                MessageStep("Server", "Controller", "server-handshake (session-id)", deactivateTarget = true),
                MessageStep("Controller", "Server", "controller-handshake (session-id, secret)", Arrow.SYNC),
                ConditionStep(
                    branches = listOf(
                        branch("if session-id or secret is invalid",
                            MessageStep("Server", "Controller", "disconnect")
                        ),
                        branch("else",
                            NoteStep(listOf("Server"), "Produces: <<event>> Controller joined"),
                            MessageStep("Server", "Controller", "bot-list-update")
                        )
                    )
                )
            )
        ),
        Flow(
            id = "starting-game",
            title = "Starting a game",
            description = "Controller selects bots, server waits for bot-ready responses, and either starts or aborts.",
            participants = listOf("Controller", "Server", "Bot", "Observer"),
            steps = listOf(
                NoteStep(listOf("Server"), "Server state = WAIT_FOR_PARTICIPANTS_TO_JOIN"),
                MessageStep("Controller", "Server", "start-game"),
                NoteStep(listOf("Server"), "Server state = WAIT_FOR_READY_PARTICIPANTS"),
                MessageStep("Server", "Bot", "game-started-event-for-bot"),
                ConditionStep(
                    branches = listOf(
                        branch("if bot is ready",
                            MessageStep("Bot", "Server", "bot-ready"),
                            NoteStep(listOf("Server"), "Bot becomes a participant"),
                            optional("if ready participants >= min participants",
                                NoteStep(listOf("Server"), "Server state = GAME_RUNNING"),
                                MessageStep("Server", "Observer", "game-started-event-for-observer"),
                                MessageStep("Server", "Controller", "game-started-event-for-observer"),
                                NoteStep(listOf("Server"), "Start turn timeout timer")
                            )
                        ),
                        branch("else Ready timer time-out",
                            SelfMessageStep("Server", "<<event>> Ready timer time-out"),
                            optional("if ready participants >= min participants",
                                NoteStep(listOf("Server"), "Server state = GAME_RUNNING"),
                                MessageStep("Server", "Observer", "game-started-event-for-observer"),
                                MessageStep("Server", "Controller", "game-started-event-for-observer"),
                                NoteStep(listOf("Server"), "Start turn timeout timer")
                            ),
                            optional("else the game is not started",
                                NoteStep(listOf("Server"), "Server state = WAIT_FOR_PARTICIPANTS_TO_JOIN"),
                                MessageStep("Server", "Observer", "game-aborted-event"),
                                MessageStep("Server", "Bot", "game-aborted-event"),
                                MessageStep("Server", "Controller", "game-aborted-event")
                            )
                        )
                    )
                )
            )
        ),
        Flow(
            id = "running-next-turn",
            title = "Running next turn",
            description = "Turn loop covering tick broadcast, bot intents, turn timeout, and skipped turns.",
            participants = listOf("Server", "Bot", "Observer", "Controller"),
            steps = listOf(
                NoteStep(listOf("Server"), "Server state = GAME_RUNNING"),
                SelfMessageStep("Server", "<<event>> next turn"),
                NoteStep(listOf("Server"), "Reset turn timer"),
                ConditionStep(
                    branches = listOf(
                        branch("if first round",
                            MessageStep("Server", "Bot", "round-started-event"),
                            MessageStep("Server", "Observer", "round-started-event"),
                            MessageStep("Server", "Controller", "round-started-event")
                        ),
                        branch("else if previous round has ended",
                            MessageStep("Server", "Bot", "round-ended-event"),
                            MessageStep("Server", "Observer", "round-ended-event"),
                            MessageStep("Server", "Controller", "round-ended-event")
                        )
                    )
                ),
                MessageStep("Server", "Bot", "tick-event-for-bot"),
                MessageStep("Server", "Observer", "tick-event-for-observer"),
                MessageStep("Server", "Controller", "tick-event-for-observer"),
                MessageStep("Bot", "Server", "bot-intent"),
                NoteStep(listOf("Server"), "Bot will not skip this turn"),
                SelfMessageStep("Server", "Turn timeout"),
                optional("if bot did not send intent before timeout",
                    MessageStep("Server", "Bot", "skipped-turn-event")
                )
            )
        ),
        Flow(
            id = "game-ending",
            title = "Game is ending",
            description = "Server resolves results once a winner is found.",
            participants = listOf("Server", "Bot", "Observer", "Controller"),
            steps = listOf(
                NoteStep(listOf("Server"), "Server state = GAME_RUNNING"),
                SelfMessageStep("Server", "<<event>> game ended"),
                optional("if bot won round",
                    MessageStep("Server", "Bot", "won-round-event")
                ),
                MessageStep("Server", "Bot", "game-ended-event-for-bot"),
                MessageStep("Server", "Observer", "game-ended-event-for-observer"),
                MessageStep("Server", "Controller", "game-ended-event-for-observer"),
                NoteStep(listOf("Server"), "Server state = GAME_STOPPED")
            )
        ),
        Flow(
            id = "abort-game",
            title = "Aborting a game",
            description = "Controller stops a running game; no results are produced.",
            participants = listOf("Controller", "Server", "Bot", "Observer"),
            steps = listOf(
                NoteStep(listOf("Server"), "Server state = GAME_RUNNING"),
                MessageStep("Controller", "Server", "stop-game"),
                MessageStep("Server", "Bot", "game-aborted-event"),
                MessageStep("Server", "Observer", "game-aborted-event"),
                MessageStep("Server", "Controller", "game-aborted-event"),
                NoteStep(listOf("Server"), "Server state = GAME_STOPPED")
            )
        ),
        Flow(
            id = "pause-game",
            title = "Pausing a game",
            description = "Controller pauses gameplay; observers/controllers are notified.",
            participants = listOf("Controller", "Server", "Observer"),
            steps = listOf(
                NoteStep(listOf("Server"), "Server state = GAME_RUNNING"),
                MessageStep("Controller", "Server", "pause-game"),
                MessageStep("Server", "Observer", "game-paused-event-for-observers"),
                MessageStep("Server", "Controller", "game-paused-event-for-observers"),
                NoteStep(listOf("Server"), "Server state = GAME_PAUSED"),
                NoteStep(listOf("Server"), "Bots keep sending intents even if paused")
            )
        ),
        Flow(
            id = "step-next-turn",
            title = "Step to the next turn while paused",
            description = "Controller can single-step while paused; server resumes one turn then pauses again.",
            participants = listOf("Controller", "Server", "Observer"),
            steps = listOf(
                NoteStep(listOf("Server"), "Server state = GAME_PAUSED"),
                MessageStep("Controller", "Server", "next-turn"),
                NoteStep(listOf("Server"), "Server temporarily resumes"),
                SelfMessageStep("Server", "Process next turn"),
                NoteStep(listOf("Server"), "Server pauses immediately after turn"),
                MessageStep("Server", "Observer", "game-paused-event-for-observers"),
                MessageStep("Server", "Controller", "game-paused-event-for-observers")
            )
        ),
        Flow(
            id = "resume-game",
            title = "Resuming a paused game",
            description = "Controller resumes gameplay and observers/controllers are informed.",
            participants = listOf("Controller", "Server", "Observer"),
            steps = listOf(
                NoteStep(listOf("Server"), "Server state = GAME_PAUSED"),
                MessageStep("Controller", "Server", "resume-game"),
                MessageStep("Server", "Observer", "game-resumed-event-for-observers"),
                MessageStep("Server", "Controller", "game-resumed-event-for-observers"),
                NoteStep(listOf("Server"), "Server state = GAME_RUNNING")
            )
        ),
        Flow(
            id = "change-tps",
            title = "Changing the TPS",
            description = "Controller adjusts turns-per-second; observers/controllers are notified.",
            participants = listOf("Controller", "Server", "Observer"),
            steps = listOf(
                MessageStep("Controller", "Server", "change-tps"),
                NoteStep(listOf("Server"), "Server updates turn timer and state"),
                MessageStep("Server", "Observer", "tps-changed-event"),
                MessageStep("Server", "Controller", "tps-changed-event"),
                optional("if new TPS = 0",
                    NoteStep(listOf("Server"), "Server state becomes GAME_PAUSED"),
                    MessageStep("Server", "Observer", "game-paused-event-for-observers"),
                    MessageStep("Server", "Controller", "game-paused-event-for-observers")
                ),
                optional("if TPS becomes non-zero while paused",
                    NoteStep(listOf("Server"), "Server resumes and restarts turn timer"),
                    MessageStep("Server", "Observer", "game-resumed-event-for-observers"),
                    MessageStep("Server", "Controller", "game-resumed-event-for-observers")
                )
            )
        ),
        Flow(
            id = "debug-graphics",
            title = "Enable or disable graphical debugging",
            description = "Controller toggles per-bot debugging permission; bots respecting flag in tick state.",
            participants = listOf("Controller", "Server", "Bot"),
            steps = listOf(
                MessageStep("Controller", "Server", "bot-policy-update"),
                MessageStep("Server", "Bot", "debug-policy-applied"),
                NoteStep(listOf("Server"), "Server updates the bot's debug flag"),
                NoteStep(listOf("Bot"), "Bot may only send debug graphics when permitted"),
                NoteStep(listOf("Server"), "Observers learn permissions through the next tick state")
            )
        )
    )
}
