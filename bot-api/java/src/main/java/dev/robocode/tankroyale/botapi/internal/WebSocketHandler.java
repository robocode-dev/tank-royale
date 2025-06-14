package dev.robocode.tankroyale.botapi.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.ConnectedEvent;
import dev.robocode.tankroyale.botapi.events.ConnectionErrorEvent;
import dev.robocode.tankroyale.botapi.events.DisconnectedEvent;
import dev.robocode.tankroyale.botapi.events.GameEndedEvent;
import dev.robocode.tankroyale.botapi.events.GameStartedEvent;
import dev.robocode.tankroyale.botapi.events.RoundEndedEvent;
import dev.robocode.tankroyale.botapi.events.RoundStartedEvent;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.internal.json.JsonConverter;
import dev.robocode.tankroyale.botapi.mapper.EventMapper;
import dev.robocode.tankroyale.botapi.mapper.GameSetupMapper;
import dev.robocode.tankroyale.schema.BotReady;
import dev.robocode.tankroyale.schema.GameEndedEventForBot;
import dev.robocode.tankroyale.schema.GameStartedEventForBot;
import dev.robocode.tankroyale.schema.Message;
import dev.robocode.tankroyale.schema.ServerHandshake;
import dev.robocode.tankroyale.schema.TickEventForBot;

import java.net.URI;
import java.net.http.WebSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

import static dev.robocode.tankroyale.botapi.mapper.ResultsMapper.map;

final class WebSocketHandler implements WebSocket.Listener {

    private final BaseBotInternals baseBotInternals;
    private final URI serverUrl;
    private final String serverSecret;
    private final IBaseBot baseBot;
    private final BotInfo botInfo;
    private final BotEventHandlers botEventHandlers;
    private final InternalEventHandlers internalEventHandlers;
    private final CountDownLatch closedLatch;

    private WebSocket socket;
    private final StringBuilder payload = new StringBuilder();

    WebSocketHandler(
            BaseBotInternals baseBotInternals,
            URI serverUrl,
            String serverSecret,
            IBaseBot baseBot,
            BotInfo botInfo,
            BotEventHandlers botEventHandlers,
            InternalEventHandlers internalEventHandlers,
            CountDownLatch closedLatch) {
        this.baseBotInternals = baseBotInternals;
        this.serverUrl = serverUrl;
        this.serverSecret = serverSecret;
        this.baseBot = baseBot;
        this.botInfo = botInfo;
        this.botEventHandlers = botEventHandlers;
        this.internalEventHandlers = internalEventHandlers;
        this.closedLatch = closedLatch;
    }

    @Override
    public void onOpen(WebSocket websocket) {
        this.socket = websocket; // To prevent null pointer exception

        botEventHandlers.onConnected.publish(new ConnectedEvent(serverUrl));
        WebSocket.Listener.super.onOpen(websocket);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket websocket, int statusCode, String reason) {
        var disconnectedEvent = new DisconnectedEvent(serverUrl, true, statusCode, reason);

        botEventHandlers.onDisconnected.publish(disconnectedEvent);
        internalEventHandlers.onDisconnected.publish(disconnectedEvent);

        closedLatch.countDown();
        return null;
    }

    @Override
    public void onError(WebSocket websocket, Throwable error) {
        botEventHandlers.onConnectionError.publish(new ConnectionErrorEvent(serverUrl, error));

        closedLatch.countDown();
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        payload.append(data);
        if (last) {
            JsonObject jsonMsg = JsonConverter.fromJson(payload.toString(), JsonObject.class);
            payload.delete(0, payload.length()); // clear payload buffer

            JsonElement jsonType = jsonMsg.get("type");
            if (jsonType != null) {
                String type = jsonType.getAsString();

                switch (dev.robocode.tankroyale.schema.Message.Type.fromValue(type)) {
                    case TICK_EVENT_FOR_BOT:
                        handleTick(jsonMsg);
                        break;
                    case ROUND_STARTED_EVENT:
                        handleRoundStarted(jsonMsg);
                        break;
                    case ROUND_ENDED_EVENT_FOR_BOT:
                        handleRoundEnded(jsonMsg);
                        break;
                    case GAME_STARTED_EVENT_FOR_BOT:
                        handleGameStarted(jsonMsg);
                        break;
                    case GAME_ENDED_EVENT_FOR_BOT:
                        handleGameEnded(jsonMsg);
                        break;
                    case SKIPPED_TURN_EVENT:
                        handleSkippedTurn(jsonMsg);
                        break;
                    case SERVER_HANDSHAKE:
                        handleServerHandshake(jsonMsg);
                        break;
                    case GAME_ABORTED_EVENT:
                        handleGameAborted();
                        break;
                    default:
                        throw new BotException("Unsupported WebSocket message type: " + type);
                }
            }
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    private void handleTick(JsonObject jsonMsg) {
        if (baseBotInternals.getEventHandlingDisabledTurn()) return;

        baseBotInternals.setTickStartNanoTime(System.nanoTime());

        var tickEventForBot = JsonConverter.fromJson(jsonMsg, TickEventForBot.class);

        var mappedTickEvent = EventMapper.map(tickEventForBot, baseBot);
        baseBotInternals.addEventsFromTick(mappedTickEvent);

        if (baseBotInternals.getBotIntent().getRescan() != null && baseBotInternals.getBotIntent().getRescan()) {
            baseBotInternals.getBotIntent().setRescan(false);
        }

        baseBotInternals.setTickEvent(mappedTickEvent);

        mappedTickEvent.getEvents().forEach(internalEventHandlers::fireEvent);

        // Trigger next turn (not tick-event!)
        internalEventHandlers.onNextTurn.publish(mappedTickEvent);
    }

    private void handleRoundStarted(JsonObject jsonMsg) {
        var roundStartedEvent = JsonConverter.fromJson(jsonMsg, RoundStartedEvent.class);

        var mappedRoundStartedEvent = new RoundStartedEvent(roundStartedEvent.getRoundNumber());

        botEventHandlers.onRoundStarted.publish(mappedRoundStartedEvent);
        internalEventHandlers.onRoundStarted.publish(mappedRoundStartedEvent);
    }

    private void handleRoundEnded(JsonObject jsonMsg) {
        var roundEndedEvent = JsonConverter.fromJson(jsonMsg, RoundEndedEvent.class);

        var mappedRoundEndedEvent = new RoundEndedEvent(
                roundEndedEvent.getRoundNumber(), roundEndedEvent.getTurnNumber(), roundEndedEvent.getResults());

        botEventHandlers.onRoundEnded.publish(mappedRoundEndedEvent);
        internalEventHandlers.onRoundEnded.publish(mappedRoundEndedEvent);
    }

    private void handleGameStarted(JsonObject jsonMsg) {
        var gameStartedEventForBot = JsonConverter.fromJson(jsonMsg, GameStartedEventForBot.class);

        baseBotInternals.setMyId(gameStartedEventForBot.getMyId());

        Set<Integer> teammateIds = gameStartedEventForBot.getTeammateIds() == null ?
                Set.of() : new HashSet<>(gameStartedEventForBot.getTeammateIds());
        baseBotInternals.setTeammateIds(teammateIds);

        baseBotInternals.setGameSetup(GameSetupMapper.map(gameStartedEventForBot.getGameSetup()));

        InitialPosition initialPosition = new InitialPosition(
                gameStartedEventForBot.getStartX(),
                gameStartedEventForBot.getStartY(),
                gameStartedEventForBot.getStartDirection());
        baseBotInternals.setInitialPosition(initialPosition);

        // Send ready signal
        var ready = new BotReady();
        ready.setType(Message.Type.BOT_READY);

        String msg = JsonConverter.toJson(ready);
        socket.sendText(msg, true);

        botEventHandlers.onGameStarted.publish(
                new GameStartedEvent(gameStartedEventForBot.getMyId(), initialPosition, baseBotInternals.getGameSetup()));
    }

    private void handleGameEnded(JsonObject jsonMsg) {
        // Send the game ended event
        var gameEndedEventForBot = JsonConverter.fromJson(jsonMsg, GameEndedEventForBot.class);

        var mappedGameEnded = new GameEndedEvent(
                gameEndedEventForBot.getNumberOfRounds(),
                map(gameEndedEventForBot.getResults()));

        botEventHandlers.onGameEnded.publish(mappedGameEnded);
        internalEventHandlers.onGameEnded.publish(mappedGameEnded);
    }

    private void handleGameAborted() {
        botEventHandlers.onGameAborted.publish(null);
        internalEventHandlers.onGameAborted.publish(null);
    }

    private void handleSkippedTurn(JsonObject jsonMsg) {
        if (baseBotInternals.getEventHandlingDisabledTurn()) return;

        var skippedTurnEvent = JsonConverter.fromJson(jsonMsg, dev.robocode.tankroyale.schema.SkippedTurnEvent.class);

        botEventHandlers.onSkippedTurn.publish((SkippedTurnEvent) EventMapper.map(skippedTurnEvent, baseBot));
    }

    private void handleServerHandshake(JsonObject jsonMsg) {
        var serverHandshake = JsonConverter.fromJson(jsonMsg, ServerHandshake.class);
        baseBotInternals.setServerHandshake(serverHandshake);

        // Reply by sending bot handshake
        var isDroid = baseBot instanceof Droid;
        var botHandshake = BotHandshakeFactory.create(serverHandshake.getSessionId(), botInfo, isDroid, serverSecret);
        String msg = JsonConverter.toJson(botHandshake);

        socket.sendText(msg, true);
    }
}
