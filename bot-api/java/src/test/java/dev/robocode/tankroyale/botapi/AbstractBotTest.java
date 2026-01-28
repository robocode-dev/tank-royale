package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import test_utils.MockedServer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Abstract base class for bot API tests.
 * Provides common test infrastructure including MockedServer lifecycle management,
 * bot thread tracking, and command execution utilities.
 *
 * <p>This class handles:
 * <ul>
 *   <li>MockedServer setup and teardown</li>
 *   <li>Bot thread lifecycle and clean shutdown</li>
 *   <li>Synchronous command execution with intent capture</li>
 *   <li>Await helpers for synchronization</li>
 * </ul>
 */
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

    protected BaseBot startAndAwaitHandshake() {
        var bot = start();
        awaitBotHandshake();
        return bot;
    }

    protected BaseBot startAndAwaitTick() {
        var bot = start();
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
        // Set gun heat to 0 so bot can fire immediately
        server.setInitialBotState(null, 0.0, null, null, null, null);
        return bot;
    }

    protected void awaitBotHandshake() {
        assertThat(server.awaitBotHandshake(1000)).isTrue();
    }

    protected void awaitGameStarted(BaseBot bot) {
        assertThat(server.awaitGameStarted(1000)).isTrue();

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
        assertThat(server.awaitTick(1000)).isTrue();

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
        assertThat(server.awaitBotIntent(1000)).isTrue();
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
