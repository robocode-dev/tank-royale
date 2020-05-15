package dev.robocode.tankroyale.server.server;

import com.google.gson.Gson;
import dev.robocode.tankroyale.server.Server;
import dev.robocode.tankroyale.server.engine.ModelUpdater;
import dev.robocode.tankroyale.server.events.SkippedTurnEvent;
import dev.robocode.tankroyale.server.mappers.*;
import dev.robocode.tankroyale.server.model.GameState;
import dev.robocode.tankroyale.server.model.Round;
import dev.robocode.tankroyale.server.model.Turn;
import lombok.val;
import dev.robocode.tankroyale.schema.*;
import lombok.var;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class GameServer {

  private static final Logger log = LoggerFactory.getLogger(GameServer.class);

  private final String gameTypes;

  private final ConnHandler connHandler;

  private RunningState runningState;
  private dev.robocode.tankroyale.server.model.GameSetup gameSetup;

  private Set<WebSocket> participants;
  private Set<WebSocket> readyParticipants;

  private final Map<WebSocket, Integer> participantIds = new HashMap<>();

  private final Map<WebSocket, dev.robocode.tankroyale.server.model.BotIntent> botIntents =
      new ConcurrentHashMap<>();

  private Timer readyTimeoutTimer;
  private Timer turnTimeoutTimer;
  private Timer tpsTimer;

  private ModelUpdater modelUpdater;

  private final Gson gson = new Gson();

  public GameServer(String gameTypes, String clientSecret) {
    if (gameTypes == null) {
      gameTypes = "";
    } else {
      gameTypes = gameTypes.replaceAll("\\s", "");
    }

    this.gameTypes = gameTypes;
    val serverSetup = new ServerSetup();
    if (!gameTypes.isEmpty()) {
      serverSetup.setGameTypes(new HashSet<>(Arrays.asList(gameTypes.split(","))));
    }

    val connListener = new GameServerConnListener();
    this.connHandler = new ConnHandler(serverSetup, connListener, clientSecret);
    this.runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
  }

  public static void main(String[] args) {
    GameServer server = new GameServer(null, null);
    server.start();
  }

  public void start() {
    log.info("Starting server on port " + Server.getPort() + " with game types: " + gameTypes);
    connHandler.start();
  }

  public void stop() {
    log.info("Stopping server");
    connHandler.stop();
  }

  private void startGameIfParticipantsReady() {
    if (readyParticipants.size() == participants.size()) {

      readyTimeoutTimer.stop();
      readyParticipants.clear();
      botIntents.clear();

      startGame();
    }
  }

  private void prepareGame() {
    log.debug("Preparing game");

    runningState = RunningState.WAIT_FOR_READY_PARTICIPANTS;

    participantIds.clear();

    // Send NewBattle to all participant bots to get them started

    GameStartedEventForBot gameStartedForBot = new GameStartedEventForBot();
    gameStartedForBot.set$type(GameStartedEventForBot.$type.GAME_STARTED_EVENT_FOR_BOT);
    gameStartedForBot.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup));

    int id = 1;
    for (WebSocket conn : participants) {
      participantIds.put(conn, id);
      gameStartedForBot.setMyId(id++);

      String msg = gson.toJson(gameStartedForBot);
      send(conn, msg);
    }

    readyParticipants = new HashSet<>();

    // Start 'bot-ready' timeout timer
    readyTimeoutTimer = new Timer(gameSetup.getReadyTimeout(), this::onReadyTimeout);
    readyTimeoutTimer.start();
  }

  private void startGame() {
    log.info("Starting game");

    runningState = RunningState.GAME_RUNNING;

    List<Participant> participantList = new ArrayList<>();
    for (WebSocket conn : participants) {
      BotHandshake h = connHandler.getBotHandshakes().get(conn);
      Participant p = new Participant();
      p.setId(participantIds.get(conn));
      p.setName(h.getName());
      p.setVersion(h.getVersion());
      p.setDescription(h.getDescription());
      p.setAuthor(h.getAuthor());
      p.setUrl(h.getUrl());
      p.setCountryCode(h.getCountryCode());
      p.setGameTypes(h.getGameTypes());
      p.setPlatform(h.getPlatform());
      p.setProgrammingLang(h.getProgrammingLang());
      participantList.add(p);
    }

    // Send GameStarted to all participant observers to get them started
    if (connHandler.getObserverAndControllerConnections().size() > 0) {
      GameStartedEventForObserver gameStartedForObserver = new GameStartedEventForObserver();
      gameStartedForObserver.set$type(
          GameStartedEventForObserver.$type.GAME_STARTED_EVENT_FOR_OBSERVER);
      gameStartedForObserver.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup));
      gameStartedForObserver.setParticipants(participantList);
      broadcastToObserverAndControllers(gson.toJson(gameStartedForObserver));
    }

    // Prepare model update

    modelUpdater = ModelUpdater.create(gameSetup, new HashSet<>(participantIds.values()));

    val turnTimeout = gameSetup.getTurnTimeout();

    // Restart timers (turn timeout + tps)
    turnTimeoutTimer = new Timer(turnTimeout, this::onTurnTimeout);

    val tps = gameSetup.getDefaultTurnsPerSecond();
    var tpsTimeout = 1_000_000 / tps;
    if (turnTimeout > tpsTimeout) {
      tpsTimeout = turnTimeout;
    }
    tpsTimer = new Timer(tpsTimeout, this::onTpsTimeout);

    turnTimeoutTimer.start();
    tpsTimer.start();
  }

  private void startGame(
      dev.robocode.tankroyale.schema.GameSetup gameSetup, Collection<BotAddress> botAddresses) {
    this.gameSetup = GameSetupToGameSetupMapper.map(gameSetup);
    participants = connHandler.getBotConnections(botAddresses);
    if (participants.size() > 0) {
      prepareGame();
    }
  }

  private void abortGame() {
    log.info("Aborting game");

    runningState = RunningState.GAME_STOPPED;

    GameAbortedEventForObserver abortedEvent = new GameAbortedEventForObserver();
    abortedEvent.set$type(GameAbortedEventForObserver.$type.GAME_ABORTED_EVENT_FOR_OBSERVER);
    broadcastToObserverAndControllers(gson.toJson(abortedEvent));

    // No score is generated for aborted games
  }

  private List<BotResultsForBot> getResultsForBots() {
    List<BotResultsForBot> botResultsList = new ArrayList<>();

    this.modelUpdater
        .getResults()
        .forEach(
            score -> {
              BotResultsForBot botResults = new BotResultsForBot();
              botResultsList.add(botResults);

              botResults.setId(score.getBotId());
              botResults.setSurvival((int) Math.round(score.getSurvival()));
              botResults.setLastSurvivorBonus((int) Math.round(score.getLastSurvivorBonus()));
              botResults.setBulletDamage((int) Math.round(score.getBulletDamage()));
              botResults.setBulletKillBonus((int) Math.round(score.getBulletKillBonus()));
              botResults.setRamDamage((int) Math.round(score.getRamDamage()));
              botResults.setRamKillBonus((int) Math.round(score.getRamKillBonus()));
              botResults.setTotalScore((int) Math.round(score.getTotalScore()));
              botResults.setFirstPlaces(score.getFirstPlaces());
              botResults.setSecondPlaces(score.getSecondPlaces());
              botResults.setThirdPlaces(score.getThirdPlaces());
            });

    int rank = 1;
    for (BotResultsForBot botResult : botResultsList) {
      botResult.setRank(rank++);
    }

    return botResultsList;
  }

  private List<BotResultsForObserver> getResultsForObservers() {
    List<BotResultsForObserver> botResultsList = new ArrayList<>();

    this.modelUpdater
        .getResults()
        .forEach(
            score -> {
              BotResultsForObserver botResults = new BotResultsForObserver();
              botResultsList.add(botResults);

              botResults.setId(score.getBotId());
              botResults.setSurvival((int) Math.round(score.getSurvival()));
              botResults.setLastSurvivorBonus((int) Math.round(score.getLastSurvivorBonus()));
              botResults.setBulletDamage((int) Math.round(score.getBulletDamage()));
              botResults.setBulletKillBonus((int) Math.round(score.getBulletKillBonus()));
              botResults.setRamDamage((int) Math.round(score.getRamDamage()));
              botResults.setRamKillBonus((int) Math.round(score.getRamKillBonus()));
              botResults.setTotalScore((int) Math.round(score.getTotalScore()));
              botResults.setFirstPlaces(score.getFirstPlaces());
              botResults.setSecondPlaces(score.getSecondPlaces());
              botResults.setThirdPlaces(score.getThirdPlaces());

              WebSocket conn = null;
              for (Entry<WebSocket, Integer> entry : participantIds.entrySet()) {
                if (entry.getValue().equals(score.getBotId())) {
                  conn = entry.getKey();
                  break;
                }
              }

              BotHandshake botHandshake = connHandler.getBotHandshakes().get(conn);
              botResults.setName(botHandshake.getName());
              botResults.setVersion(botHandshake.getVersion());
            });

    int rank = 1;
    for (BotResultsForObserver botResult : botResultsList) {
      botResult.setRank(rank++);
    }

    return botResultsList;
  }

  private void pauseGame() {
    log.info("Pausing game");

    GamePausedEventForObserver pausedEvent = new GamePausedEventForObserver();
    pausedEvent.set$type(GamePausedEventForObserver.$type.GAME_PAUSED_EVENT_FOR_OBSERVER);
    broadcastToObserverAndControllers(gson.toJson(pausedEvent));

    runningState = RunningState.GAME_PAUSED;

    tpsTimer.stop();
    turnTimeoutTimer.stop();
  }

  private void resumeGame() {
    log.info("Resuming game");

    GameResumedEventForObserver resumedEvent = new GameResumedEventForObserver();
    resumedEvent.set$type(GameResumedEventForObserver.$type.GAME_RESUMED_EVENT_FOR_OBSERVER);
    broadcastToObserverAndControllers(gson.toJson(resumedEvent));

    if (runningState == RunningState.GAME_PAUSED) {
      runningState = RunningState.GAME_RUNNING;

      tpsTimer.start();
      turnTimeoutTimer.start();
    }
  }

  private GameState updateGameState() {
    Map<Integer /* BotId */, dev.robocode.tankroyale.server.model.BotIntent> mappedBotIntents =
        new HashMap<>();

    for (Entry<WebSocket, dev.robocode.tankroyale.server.model.BotIntent> entry :
        botIntents.entrySet()) {
      val key = entry.getKey();
      if (key != null) {
        int botId = participantIds.get(key);
        mappedBotIntents.put(botId, entry.getValue());
      }
    }

    return modelUpdater.update(Collections.unmodifiableMap(mappedBotIntents));
  }

  private void onReadyTimeout() {
    log.debug("Ready timeout");

    if (readyParticipants.size() >= gameSetup.getMinNumberOfParticipants()) {
      // Start the game with the participants that are ready
      participants = readyParticipants;
      startGame();

    } else {
      // Not enough participants -> prepare another game
      runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
    }
  }

  private void onTurnTimeout() {
    turnTimeoutTimer.stop();

    // Send SkippedTurnEvents to all bots that skipped a turn, i.e. where the server did not receive
    // a bot intent
    // before the turn ended.
    participantIds.forEach(
        (conn, id) -> {
          if (botIntents.get(conn) == null) {
            dev.robocode.tankroyale.server.events.SkippedTurnEvent skippedTurnEvent =
                    new SkippedTurnEvent(modelUpdater.getTurnNumber());

            send(conn, gson.toJson(skippedTurnEvent));
          }
        });
  }

  private void onTpsTimeout() {
    nextTurnTick();
  }

  private synchronized void nextTurnTick() {
    log.debug("Updating game state");

    if (runningState != RunningState.GAME_STOPPED) {
      // Update game state
      GameState gameState = updateGameState();

      if (gameState.isGameEnded()) {

        // Restart turn timeout timer
        turnTimeoutTimer.stop();
        tpsTimer.stop();

        runningState = RunningState.GAME_STOPPED;

        log.info("Game ended");

        modelUpdater.calculatePlacements();

        // End game for bots
        GameEndedEventForBot endEventForBot = new GameEndedEventForBot();
        endEventForBot.set$type(GameEndedEventForObserver.$type.GAME_ENDED_EVENT_FOR_BOT);
        endEventForBot.setNumberOfRounds(modelUpdater.getNumberOfRounds());
        endEventForBot.setResults(getResultsForBots());
        broadcastToBots(gson.toJson(endEventForBot));

        // End game for observers
        GameEndedEventForObserver endEventForObserver = new GameEndedEventForObserver();
        endEventForObserver.set$type(GameEndedEventForObserver.$type.GAME_ENDED_EVENT_FOR_OBSERVER);
        endEventForObserver.setNumberOfRounds(modelUpdater.getNumberOfRounds());
        endEventForObserver.setResults(getResultsForObservers()); // Use the stored score!
        broadcastToObserverAndControllers(gson.toJson(endEventForObserver));

      } else {
        // Clear bot intents
        botIntents.clear();

        // Send tick

        Round round = gameState.getLastRound();
        if (round != null) {
          Turn turn = round.getLastTurn();
          if (turn != null) {
            // Send game state as 'game tick' to participants
            for (WebSocket conn : participants) {
              TickEventForBot gameTickForBot =
                  TurnToGameTickForBotMapper.map(round, turn, participantIds.get(conn));
              if (gameTickForBot != null) { // Bot alive?
                String msg = gson.toJson(gameTickForBot);
                send(conn, msg);
              }
            }
            TickEventForObserver gameTickForObserver =
                TurnToGameTickForObserverMapper.map(round, turn);
            broadcastToObserverAndControllers(gson.toJson(gameTickForObserver));
          }
        }
        // Restart turn timeout timer
        turnTimeoutTimer.start();
        tpsTimer.start();
      }
    }
  }

  private static void send(WebSocket conn, String message) {
    log.debug("Sending to: " + conn.getRemoteSocketAddress() + ", message: " + message);

    try {
      conn.send(message);
    } catch (WebsocketNotConnectedException ignore) {
      // Bot cannot receive events and send new intents.
    }
  }

  private void updateBotIntent(WebSocket conn, dev.robocode.tankroyale.schema.BotIntent intent) {
    if (!participants.contains(conn)) {
      return;
    }
    dev.robocode.tankroyale.server.model.BotIntent botIntent = botIntents.get(conn);
    if (botIntent == null) {
      botIntent = dev.robocode.tankroyale.server.model.BotIntent.builder().build();
    }
    botIntent = botIntent.update(BotIntentToBotIntentMapper.map(intent));
    botIntents.put(conn, botIntent);
  }

  private String createBotListUpdateMessage() {
    BotListUpdate botListUpdate = new BotListUpdate();
    botListUpdate.set$type(Message.$type.BOT_LIST_UPDATE);
    List<BotInfo> bots = new ArrayList<>();
    botListUpdate.setBots(bots);

    Set<WebSocket> botConnections = connHandler.getBotConnections();
    for (WebSocket conn : botConnections) {
      InetSocketAddress address = conn.getRemoteSocketAddress();
      BotInfo botInfo =
          BotHandshakeToBotInfoMapper.map(
              connHandler.getBotHandshakes().get(conn), address.getHostString(), address.getPort());
      bots.add(botInfo);
    }
    return gson.toJson(botListUpdate);
  }

  public void broadcastToBots(String msg) {
    connHandler.broadcastToBots(msg);
  }

  private void broadcastToObserverAndControllers(String msg) {
    connHandler.broadcastToObserverAndControllers(msg);
  }

  private void sendBotListUpdateToObservers() {
    broadcastToObserverAndControllers(createBotListUpdateMessage());
  }

  @SuppressWarnings("unused")
  private class GameServerConnListener implements ConnListener {

    @Override
    public void onException(Exception exception) {
      exception.printStackTrace();
    }

    @Override
    public void onBotJoined(WebSocket conn, BotHandshake handshake) {
      log.info("Bot joined: " + getDisplayName(handshake));
      sendBotListUpdateToObservers();
    }

    @Override
    public void onBotLeft(WebSocket conn) {
      log.info("Bot joined: " + getDisplayName(connHandler.getBotHandshakes().get(conn)));

      // If a bot leaves while in a game, make sure to reset all intent values to zeroes
      botIntents.put(
          conn, dev.robocode.tankroyale.server.model.BotIntent.builder().build().zeroed());

      sendBotListUpdateToObservers();
    }

    @Override
    public void onObserverJoined(WebSocket conn, ObserverHandshake handshake) {
      log.info("Observer joined: " + getDisplayName(handshake));
      String msg = createBotListUpdateMessage();
      send(conn, msg);
    }

    @Override
    public void onObserverLeft(WebSocket conn) {
      log.info("Observer left: " + getDisplayName(connHandler.getObserverHandshakes().get(conn)));
    }

    @Override
    public void onControllerJoined(WebSocket conn, ControllerHandshake handshake) {
      log.info("Controller joined: " + getDisplayName(handshake));
      String msg = createBotListUpdateMessage();
      send(conn, msg);
    }

    @Override
    public void onControllerLeft(WebSocket conn) {
      log.info("Controller left: " + getDisplayName(connHandler.getControllerHandshakes().get(conn)));
    }

    @Override
    public void onBotReady(WebSocket conn) {
      if (runningState == RunningState.WAIT_FOR_READY_PARTICIPANTS) {
        readyParticipants.add(conn);
        startGameIfParticipantsReady();
      }
    }

    @Override
    public void onBotIntent(WebSocket conn, dev.robocode.tankroyale.schema.BotIntent intent) {
      updateBotIntent(conn, intent);
    }

    @Override
    public void onStartGame(
        dev.robocode.tankroyale.schema.GameSetup gameSetup, Collection<BotAddress> botAddresses) {
      startGame(gameSetup, botAddresses);
    }

    @Override
    public void onAbortGame() {
      abortGame();
    }

    @Override
    public void onPauseGame() {
      pauseGame();
    }

    @Override
    public void onResumeGame() {
      resumeGame();
    }

    private String getDisplayName(BotHandshake handshake) {
      return getDisplayName(handshake.getName(), handshake.getVersion());
    }

    private String getDisplayName(ObserverHandshake handshake) {
      return getDisplayName(handshake.getName(), handshake.getVersion());
    }

    private String getDisplayName(ControllerHandshake handshake) {
      return getDisplayName(handshake.getName(), handshake.getVersion());
    }

    private String getDisplayName(String name, String version) {
      String displayName = "";
      if (name != null) {
        name = name.trim();
        if (!name.isEmpty()) {
          displayName = name;
        }
      }
      if (version != null) {
        version = version.trim();
        if (!version.isEmpty()) {
          displayName += ' ' + version;
        }
      }
      return displayName;
    }
  }
}
