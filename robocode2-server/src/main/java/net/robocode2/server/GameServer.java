package net.robocode2.server;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lombok.val;
import net.robocode2.Server;
import net.robocode2.model.BotIntent;
import net.robocode2.model.GameSetup;
import net.robocode2.schema.*;
import net.robocode2.model.*;
import org.java_websocket.WebSocket;

import com.google.gson.Gson;

import net.robocode2.engine.ModelUpdater;
import net.robocode2.schema.Participant;
import net.robocode2.schema.BotAddress;
import net.robocode2.schema.BotHandshake;
import net.robocode2.schema.BotInfo;
import net.robocode2.schema.BotListUpdate;
import net.robocode2.schema.ControllerHandshake;
import net.robocode2.schema.Message;
import net.robocode2.schema.ObserverHandshake;
import net.robocode2.mappers.BotHandshakeToBotInfoMapper;
import net.robocode2.mappers.BotIntentToBotIntentMapper;
import net.robocode2.mappers.GameSetupToGameSetupMapper;
import net.robocode2.mappers.TurnToGameTickForBotMapper;
import net.robocode2.mappers.TurnToGameTickForObserverMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.round;

public final class GameServer {

  private static Logger logger = LoggerFactory.getLogger(GameServer.class);

  private ConnHandler connHandler;

  private RunningState runningState;
  private GameSetup gameSetup;

  private Set<String /* clientKey */> participants;
  private Set<String /* clientKey */> readyParticipants;

  private Map<String /* clientKey */, Integer> participantIds = new HashMap<>();

  private Map<String /* clientKey */, BotIntent> botIntents = new ConcurrentHashMap<>();

  private Timer readyTimer;
  private Timer turnTimer;

  private ModelUpdater modelUpdater;

  private final Gson gson = new Gson();

  public GameServer() {
    val serverSetup = new ServerSetup();
    val connListener = new GameServerConnListener();
    this.connHandler = new ConnHandler(serverSetup, connListener);
    this.runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
  }

  public static void main(String[] args) {
    GameServer server = new GameServer();
    server.start();
  }

  public void start() {
    logger.info("Starting server on port " + Server.getPort());
    connHandler.start();
  }

  private void startGameIfParticipantsReady() {
    if (readyParticipants.size() == participants.size()) {

      if (readyTimer != null) {
        readyTimer.cancel();
      }
      readyParticipants.clear();
      botIntents.clear();

      startGame();
    }
  }

  private void prepareGame() {
    logger.debug("Preparing game");

    runningState = RunningState.WAIT_FOR_READY_PARTICIPANTS;

    participantIds.clear();

    // Send NewBattle to all participant bots to get them started

    GameStartedEventForBot gameStartedForBot = new GameStartedEventForBot();
    gameStartedForBot.setType(GameStartedEventForBot.Type.GAME_STARTED_EVENT_FOR_BOT);
    gameStartedForBot.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup));

    int id = 1;
    for (String botKey : participants) {
      participantIds.put(botKey, id);
      gameStartedForBot.setMyId(id);

      String msg = gson.toJson(gameStartedForBot);
      send(botKey, msg);

      id++;
    }

    readyParticipants = new HashSet<>();

    // Start 'ready' timer

    readyTimer = new Timer("Bot-ready-timer");
    readyTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            onReadyTimeout();
          }
        },
        gameSetup.getReadyTimeout());
  }

  private void startGame() {
    logger.debug("Starting game");

    runningState = RunningState.GAME_RUNNING;

    List<Participant> participantList = new ArrayList<>();
    for (String botKey : participants) {
      BotHandshake h = connHandler.getBotHandshakes().get(botKey);
      Participant p = new Participant();
      p.setId(participantIds.get(botKey));
      p.setAuthor(h.getAuthor());
      p.setCountryCode(h.getCountryCode());
      p.setGameTypes(h.getGameTypes());
      p.setName(h.getName());
      p.setProgrammingLang(h.getProgrammingLang());
      p.setVersion(h.getVersion());
      participantList.add(p);
    }

    // Send GameStarted to all participant observers to get them started
    if (connHandler.getObserverAndControllerConnections().size() > 0) {
      GameStartedEventForObserver gameStartedForObserver = new GameStartedEventForObserver();
      gameStartedForObserver.setType(
          GameStartedEventForObserver.Type.GAME_STARTED_EVENT_FOR_OBSERVER);
      gameStartedForObserver.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup));
      gameStartedForObserver.setParticipants(participantList);
      sendMessageToObservers(gson.toJson(gameStartedForObserver));
    }

    // Prepare model update

    modelUpdater = ModelUpdater.create(gameSetup, new HashSet<>(participantIds.values()));

    // Create timer to updating game state

    turnTimer = new Timer("turn-timer");
    turnTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            onUpdateGameState();
          }
        },
        gameSetup.getTurnTimeout(),
        gameSetup.getTurnTimeout());
  }

  private void startGame(
      net.robocode2.schema.GameSetup gameSetup, Collection<BotAddress> botAddresses) {
    this.gameSetup = GameSetupToGameSetupMapper.map(gameSetup);
    participants = connHandler.getBotKeys(botAddresses);
    if (participants.size() > 0) {
      prepareGame();
    }
  }

  private void abortGame() {
    logger.info("Aborting game");

    runningState = RunningState.GAME_STOPPED;

    GameAbortedEventForObserver abortedEvent = new GameAbortedEventForObserver();
    abortedEvent.setType(GameAbortedEventForObserver.Type.GAME_ABORTED_EVENT_FOR_OBSERVER);
    sendMessageToObservers(gson.toJson(abortedEvent));

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
              botResults.setSurvival((int) round(score.getSurvival()));
              botResults.setLastSurvivorBonus((int) round(score.getLastSurvivorBonus()));
              botResults.setBulletDamage((int) round(score.getBulletDamage()));
              botResults.setBulletKillBonus((int) round(score.getBulletKillBonus()));
              botResults.setRamDamage((int) round(score.getRamDamage()));
              botResults.setRamKillBonus((int) round(score.getRamKillBonus()));
              botResults.setTotalScore((int) round(score.getTotalScore()));
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
              botResults.setSurvival((int) round(score.getSurvival()));
              botResults.setLastSurvivorBonus((int) round(score.getLastSurvivorBonus()));
              botResults.setBulletDamage((int) round(score.getBulletDamage()));
              botResults.setBulletKillBonus((int) round(score.getBulletKillBonus()));
              botResults.setRamDamage((int) round(score.getRamDamage()));
              botResults.setRamKillBonus((int) round(score.getRamKillBonus()));
              botResults.setTotalScore((int) round(score.getTotalScore()));
              botResults.setFirstPlaces(score.getFirstPlaces());
              botResults.setSecondPlaces(score.getSecondPlaces());
              botResults.setThirdPlaces(score.getThirdPlaces());

              String clientKey = null;
              for (Entry<String, Integer> entry2 : participantIds.entrySet()) {
                if (entry2.getValue().equals(score.getBotId())) {
                  clientKey = entry2.getKey();
                  break;
                }
              }

              BotHandshake botHandshake = connHandler.getBotHandshakes().get(clientKey);
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
    logger.info("Pausing game");

    GamePausedEventForObserver pausedEvent = new GamePausedEventForObserver();
    pausedEvent.setType(GamePausedEventForObserver.Type.GAME_PAUSED_EVENT_FOR_OBSERVER);
    sendMessageToObservers(gson.toJson(pausedEvent));

    runningState = RunningState.GAME_PAUSED;
  }

  private void resumeGame() {
    logger.info("Resuming game");

    GameResumedEventForObserver resumedEvent = new GameResumedEventForObserver();
    resumedEvent.setType(GameResumedEventForObserver.Type.GAME_RESUMED_EVENT_FOR_OBSERVER);
    sendMessageToObservers(gson.toJson(resumedEvent));

    if (runningState == RunningState.GAME_PAUSED) {
      runningState = RunningState.GAME_RUNNING;
    }
  }

  private GameState updateGameState() {
    Map<Integer /* BotId */, BotIntent> mappedBotIntents = new HashMap<>();

    for (Entry<String, BotIntent> entry : botIntents.entrySet()) {
      int botId = participantIds.get(entry.getKey());
      mappedBotIntents.put(botId, entry.getValue());
    }

    return modelUpdater.update(Collections.unmodifiableMap(mappedBotIntents));
  }

  private void onReadyTimeout() {
    logger.debug("Ready timer timed out");

    if (readyParticipants.size() >= gameSetup.getMinNumberOfParticipants()) {
      // Start the game with the participants that are ready
      participants = readyParticipants;
      startGame();

    } else {
      // Not enough participants -> prepare another game
      runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
    }
  }

  private void onUpdateGameState() {
    if (runningState == RunningState.GAME_PAUSED) {
      return;
    }

    logger.debug("Updating game state");

    if (runningState == RunningState.GAME_STOPPED) {
      // Stop timer for updating game state
      turnTimer.cancel();
    } else {
      // Update game state
      GameState gameState = updateGameState();

      if (gameState.isGameEnded()) {
        runningState = RunningState.GAME_STOPPED;

        logger.info("Game ended");

        // Stop timer for updating game state
        turnTimer.cancel();

        modelUpdater.calculatePlacements();

        // End game for bots
        GameEndedEventForBot endEventForBot = new GameEndedEventForBot();
        endEventForBot.setType(GameEndedEventForObserver.Type.GAME_ENDED_EVENT_FOR_BOT);
        endEventForBot.setNumberOfRounds(modelUpdater.getNumberOfRounds());
        endEventForBot.setResults(getResultsForBots());
        sendMessageToBots(gson.toJson(endEventForBot));

        // End game for observers
        GameEndedEventForObserver endEventForObserver = new GameEndedEventForObserver();
        endEventForObserver.setType(GameEndedEventForObserver.Type.GAME_ENDED_EVENT_FOR_OBSERVER);
        endEventForObserver.setNumberOfRounds(modelUpdater.getNumberOfRounds());
        endEventForObserver.setResults(getResultsForObservers()); // Use the stored score!
        sendMessageToObservers(gson.toJson(endEventForObserver));

        // runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN; // TODO: Correct?
      } else {
        // Clear bot intents
        botIntents.clear();

        // Send tick

        Round round = gameState.getLastRound();
        if (round != null) {
          Turn turn = round.getLastTurn();
          if (turn != null) {
            // Send game state as 'game tick' to participants
            for (String botKey : participants) {
              TickEventForBot gameTickForBot =
                  TurnToGameTickForBotMapper.map(round, turn, participantIds.get(botKey));
              if (gameTickForBot != null) { // Bot alive?
                String msg = gson.toJson(gameTickForBot);
                send(botKey, msg);
              }
            }
            TickEventForObserver gameTickForObserver =
                TurnToGameTickForObserverMapper.map(round, turn);
            sendMessageToObservers(gson.toJson(gameTickForObserver));
          }
        }
      }
    }
  }

  private void send(String clientKey, String message) {
    WebSocket conn = connHandler.getConnection(clientKey);
    send(conn, message);
  }

  private static void send(WebSocket conn, String message) {
    logger.debug("Sending to: " + conn.getRemoteSocketAddress() + ", message: " + message);

    conn.send(message);
  }

  private void updateBotIntent(String botKey, net.robocode2.schema.BotIntent intent) {
    if (!participants.contains(botKey)) {
      return;
    }
    BotIntent botIntent = botIntents.get(botKey);
    if (botIntent == null) {
      botIntent = BotIntent.builder().build();
    }
    botIntent = botIntent.update(BotIntentToBotIntentMapper.map(intent));
    botIntents.put(botKey, botIntent);
  }

  private String createBotListUpdateMessage() {
    BotListUpdate botListUpdate = new BotListUpdate();
    botListUpdate.setType(Message.Type.BOT_LIST_UPDATE);
    List<BotInfo> bots = new ArrayList<>();
    botListUpdate.setBots(bots);

    Map<String, WebSocket> botConnections = connHandler.getBotConnections();
    for (Entry<String, WebSocket> entry : botConnections.entrySet()) {
      String clientKey = entry.getKey();
      InetSocketAddress address = entry.getValue().getRemoteSocketAddress();
      BotInfo botInfo =
          BotHandshakeToBotInfoMapper.map(
              connHandler.getBotHandshakes().get(clientKey),
              address.getHostString(),
              address.getPort());
      bots.add(botInfo);
    }
    return gson.toJson(botListUpdate);
  }

  private void sendMessageToBots(String msg) {
    connHandler.getBotConnections().values().forEach(conn -> send(conn, msg));
  }

  private void sendMessageToObservers(String msg) {
    connHandler.getObserverAndControllerConnections().values().forEach(conn -> send(conn, msg));
  }

  private void sendBotListUpdateToObservers() {
    sendMessageToObservers(createBotListUpdateMessage());
  }

  private class GameServerConnListener implements ConnListener {

    @Override
    public void onException(Exception exception) {
      exception.printStackTrace();
    }

    @Override
    public void onBotJoined(String clientKey, BotHandshake bot) {
      sendBotListUpdateToObservers();
    }

    @Override
    public void onBotLeft(String clientKey) {
      sendBotListUpdateToObservers();
    }

    @Override
    public void onObserverJoined(String clientKey, ObserverHandshake bot) {
      String msg = createBotListUpdateMessage();
      send(clientKey, msg);
    }

    @Override
    public void onObserverLeft(String clientKey) {
      // Do nothing
    }

    @Override
    public void onControllerJoined(String clientKey, ControllerHandshake bot) {
      String msg = createBotListUpdateMessage();
      send(clientKey, msg);
    }

    @Override
    public void onControllerLeft(String clientKey) {
      // Do nothing
    }

    @Override
    public void onBotReady(String clientKey) {
      if (runningState == RunningState.WAIT_FOR_READY_PARTICIPANTS) {
        readyParticipants.add(clientKey);
        startGameIfParticipantsReady();
      }
    }

    @Override
    public void onBotIntent(String clientKey, net.robocode2.schema.BotIntent intent) {
      updateBotIntent(clientKey, intent);
    }

    @Override
    public void onStartGame(
        net.robocode2.schema.GameSetup gameSetup, Collection<BotAddress> botAddresses) {
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
  }
}
