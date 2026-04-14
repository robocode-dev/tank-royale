package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import test_utils.MockedServer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Abstract base class for bot API tests.
 *
 * <h3>Bot Type</h3>
 * Tests use {@link BaseBot} (not {@code Bot}). BaseBot has NO internal thread and
 * never sends automatic intents — the test controls exactly when intents are sent
 * via {@link #goAsync(BaseBot)}.
 *
 * <h3>Intent-Capture Protocol</h3>
 * To capture what the bot sends to the server, tests follow this 5-step sequence:
 * <pre>
 * 1. server.resetBotIntentLatch()   — clear stale semaphore permits
 * 2. bot.setSomeValue(...)          — set command values on the bot
 * 3. goAsync(bot)                   — trigger bot.go() in a tracked thread
 * 4. server.continueBotIntent()     — release the MockedServer gate
 * 5. awaitBotIntent()               — block until intent is captured
 * </pre>
 * The {@link #executeCommandAndGetIntent} helper encapsulates steps 1, 4, 5.
 *
 * <h3>Why continueBotIntent() is Required</h3>
 * MockedServer's handler blocks on a semaphore BEFORE parsing the intent JSON.
 * If {@code continueBotIntent()} is never called, the handler blocks forever,
 * the bot thread blocks in {@code waitForNextTurn()}, and the test hangs.
 *
 * @see <a href="../../../../../../tests/TESTING-GUIDE.md">TESTING-GUIDE.md</a>
 */
@Timeout(value = 10, unit = TimeUnit.SECONDS)
abstract class AbstractBotTest {

    protected MockedServer server;
    private final List<Thread> trackedThreads = new ArrayList<>();

    protected static final BotInfo botInfo = BotInfo.builder()
            .setName("TestBot")
            .setVersion("1.0")
            .addAuthor("Author 1")
            .addAuthor("Author 2")
            .setDescription("Short description")
            .setHomepage("https://testbot.robocode.dev")
            .addCountryCode("gb")
            .addCountryCode("us")
            .addGameType("classic")
            .addGameType("melee")
            .addGameType("1v1")
            .setPlatform("JVM 19")
            .setProgrammingLang("Java 19")
            .setInitialPosition(InitialPosition.fromString("10, 20, 30"))
            .build();

    protected static class TestBot extends BaseBot {
        TestBot() {
            super(botInfo, MockedServer.getServerUrl());
        }
    }

    @BeforeEach
    void setUp() {
        server = new MockedServer();
        server.start();
    }

    @AfterEach
    void tearDown() {
        // Stop server first to trigger bot thread shutdown
        server.stop();

        // Wait for tracked threads to complete with timeout
        for (Thread thread : trackedThreads) {
            if (thread.isAlive()) {
                try {
                    thread.join(2000); // 2 second timeout per thread
                    if (thread.isAlive()) {
                        // Thread didn't stop cleanly - interrupt it
                        thread.interrupt();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        trackedThreads.clear();
    }

    /**
     * Create and start a test bot asynchronously.
     * The bot thread is automatically tracked for clean shutdown.
     *
     * @return the started bot instance
     */
    protected BaseBot start() {
        var bot = new TestBot();
        startAsync(bot);
        return bot;
    }

    /**
     * Start a bot asynchronously in a tracked thread.
     * The thread is registered for cleanup during teardown.
     *
     * @param bot the bot to start
     * @return the thread running the bot
     */
    protected Thread startAsync(BaseBot bot) {
        var thread = new Thread(bot::start);
        thread.setName("TestBot-" + System.currentTimeMillis());
        trackedThreads.add(thread);
        thread.start();
        return thread;
    }

    /**
     * Execute bot.go() asynchronously in a tracked thread.
     * The thread is registered for cleanup during teardown.
     *
     * @param bot the bot to run
     */
    protected void goAsync(BaseBot bot) {
        var thread = new Thread(bot::go);
        thread.setName("TestBot-go-" + System.currentTimeMillis());
        trackedThreads.add(thread);
        thread.start();
    }

    protected void goAsync(Runnable runnable) {
        var thread = new Thread(runnable);
        thread.setName("TestBot-action-" + System.currentTimeMillis());
        trackedThreads.add(thread);
        thread.start();
    }

    protected BaseBot startAndAwaitHandshake() {
        var bot = start();
        awaitBotHandshake();
        return bot;
    }

    protected BaseBot startAndAwaitTick() {
        var bot = start();
        awaitGameStarted(bot);
        awaitTick(bot);
        return bot;
    }

    protected BaseBot startAndAwaitGameStarted() {
        var bot = start();
        awaitGameStarted(bot);
        return bot;
    }

    /**
     * Start bot, wait for game started, and set gun heat to 0 for fire tests.
     * This convenience method prepares the bot to be able to fire immediately.
     *
     * @return the started bot with gun heat at 0
     */
    protected BaseBot startAndPrepareForFire() {
        var bot = start();
        awaitGameStarted(bot);
        // Set gun heat to 0 and energy to 100 so bot can fire immediately
        // Use setBotStateAndAwaitTick to actually send the state to the bot
        boolean tickSent = server.setBotStateAndAwaitTick(100.0, 0.0, null, null, null, null);
        assertThat(tickSent).as("setBotStateAndAwaitTick should send tick").isTrue();
        // Wait for bot to update its internal state by polling until energy matches
        boolean stateUpdated = awaitCondition(() -> bot.getEnergy() == 100.0 && bot.getGunHeat() == 0.0, 2000);
        assertThat(stateUpdated).as("Bot state should update to energy=100, gunHeat=0 (actual: energy=" + bot.getEnergy() + ", gunHeat=" + bot.getGunHeat() + ")").isTrue();
        return bot;
    }

    protected void awaitBotHandshake() {
        assertThat(server.awaitBotHandshake(5000)).isTrue();
    }

    protected void awaitGameStarted(BaseBot bot) {
        assertThat(server.awaitGameStarted(5000)).isTrue();

        long startMillis = System.currentTimeMillis();
        boolean noException = false;
        do {
            try {
                bot.getGameType();
                noException = true;
            } catch (BotException ex) {
                Thread.yield();
            }
        } while (!noException && System.currentTimeMillis() - startMillis < 1000);
    }

    protected void awaitTick(BaseBot bot) {
        assertThat(server.awaitTick(5000)).isTrue();

        long startMillis = System.currentTimeMillis();
        boolean noException = false;
        do {
            try {
                bot.getEnergy();
                noException = true;
            } catch (BotException ex) {
                Thread.yield();
            }
        } while (!noException && System.currentTimeMillis() - startMillis < 1000);
    }

    protected void awaitBotIntent() {
        assertThat(server.awaitBotIntent(5000)).isTrue();
    }

    /**
     * Execute a command and wait for the bot to send its intent to the server.
     * This is useful for testing non-blocking commands that immediately return.
     *
     * @param command the command to execute
     * @param <T> the return type of the command
     * @return the result of the command
     */
    protected <T> T executeCommand(java.util.function.Supplier<T> command) {
        server.resetBotIntentLatch();
        T result = command.get();
        server.continueBotIntent();
        awaitBotIntent();
        return result;
    }

    /**
     * Execute a blocking action and wait for the bot to send its intent to the server.
     * This is useful for testing blocking commands like go().
     *
     * @param action the action to execute
     */
    protected void executeBlocking(Runnable action) {
        server.resetBotIntentLatch();
        action.run();
        server.continueBotIntent();
        awaitBotIntent();
    }

    /**
     * Execute a command and capture both the result and the bot intent sent to the server.
     * This is useful for verifying that commands produce the expected intent values.
     *
     * @param command the command to execute
     * @param <T> the return type of the command
     * @return a CommandResult containing both the command result and captured intent
     */
    protected <T> CommandResult<T> executeCommandAndGetIntent(java.util.function.Supplier<T> command) {
        server.resetBotIntentLatch();
        T result = command.get();
        server.continueBotIntent();
        awaitBotIntent();
        return new CommandResult<>(result, server.getBotIntent());
    }

    /**
     * Wrapper class that holds both a command's return value and the captured bot intent.
     * Use this to verify that bot commands produce the correct intent values sent to the server.
     *
     * @param <T> the type of the command result
     */
    protected static class CommandResult<T> {
        private final T result;
        private final dev.robocode.tankroyale.schema.BotIntent intent;

        public CommandResult(T result, dev.robocode.tankroyale.schema.BotIntent intent) {
            this.result = result;
            this.intent = intent;
        }

        public T getResult() {
            return result;
        }

        public dev.robocode.tankroyale.schema.BotIntent getIntent() {
            return intent;
        }
    }

    protected static boolean exceptionContainsEnvVarName(BotException botException, String envVarName) {
        return botException.getMessage().toUpperCase().contains(envVarName);
    }

    protected boolean awaitCondition(BooleanSupplier condition, int milliSeconds) {
        long startTime = System.currentTimeMillis();
        do {
            try {
                if (condition.getAsBoolean()) {
                    return true;
                }
            } catch (BotException ignore) {
            }
            Thread.yield();
        } while (System.currentTimeMillis() - startTime < milliSeconds);
        return false;
    }
}
