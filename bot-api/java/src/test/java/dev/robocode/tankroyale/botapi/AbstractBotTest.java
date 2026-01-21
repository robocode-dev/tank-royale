package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import test_utils.MockedServer;
import dev.robocode.tankroyale.schema.BotIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

abstract class AbstractBotTest {

    protected MockedServer server;
    protected final List<Thread> botThreads = new ArrayList<>();

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
        for (Thread thread : botThreads) {
            thread.interrupt();
            try {
                thread.join(100);
            } catch (InterruptedException ignored) {
            }
        }
        botThreads.clear();
        server.stop();
    }

    protected BaseBot start() {
        var bot = new TestBot();
        startAsync(bot);
        return bot;
    }

    protected Thread startAsync(BaseBot bot) {
        var thread = new Thread(() -> {
            try {
                bot.start();
            } catch (Exception e) {
                if (!(e.getCause() instanceof InterruptedException) && !(e instanceof BotException && e.getMessage().contains("Interrupted"))) {
                    // Only print if it's not a normal interruption
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        botThreads.add(thread);
        return thread;
    }

    protected void goAsync(BaseBot bot) {
        var thread = new Thread(() -> {
            try {
                bot.go();
            } catch (Exception e) {
                // Ignore interruption noise
            }
        });
        thread.start();
        botThreads.add(thread);
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
        if (!server.awaitBotIntent(5000)) { // Increased timeout for debugging
            throw new RuntimeException("Timeout waiting for BotIntent");
        }
    }

    protected BotIntent executeCommandAndGetIntent(BaseBot bot, Runnable command) {
        command.run();
        goAsync(bot);
        awaitBotIntent();
        var intent = server.getBotIntent();
        assertThat(intent).as("BotIntent should not be null after execution").isNotNull();
        return intent;
    }

    protected <T> T executeCommand(BaseBot bot, java.util.function.Supplier<T> command) {
        T result = command.get();
        goAsync(bot);
        awaitBotIntent();
        return result;
    }

    protected void executeCommand(BaseBot bot, Runnable command) {
        command.run();
        goAsync(bot);
        awaitBotIntent();
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
