package test_utils;

import dev.robocode.tankroyale.botapi.Bot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TestBotBuilder.
 */
class TestBotBuilderTest {

    private static final int CALLBACK_TIMEOUT_MS = 5_000;

    private MockedServer server;

    @BeforeEach
    void setUp() {
        server = new MockedServer();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Tag("Unit")
    @Test
    @DisplayName("TestBotBuilder creates bot with default passive behavior")
    void testDefaultPassiveBehavior() {
        Bot bot = TestBotBuilder.create()
                .withBehavior(TestBotBuilder.BotBehavior.PASSIVE)
                .build();

        assertThat(bot).isNotNull();
    }

    @Tag("Unit")
    @Test
    @DisplayName("TestBotBuilder creates bot with custom name")
    void testCustomName() {
        Bot bot = TestBotBuilder.create()
                .withName("CustomBot")
                .withVersion("2.0")
                .withAuthors("Author1", "Author2")
                .build();

        assertThat(bot).isNotNull();
    }

    @Tag("Unit")
    @Test
    @DisplayName("TestBotBuilder creates bot with aggressive behavior")
    void testAggressiveBehavior() {
        Bot bot = TestBotBuilder.create()
                .withBehavior(TestBotBuilder.BotBehavior.AGGRESSIVE)
                .build();

        assertThat(bot).isNotNull();
    }

    @Tag("Unit")
    @Test
    @DisplayName("TestBotBuilder creates bot with scanning behavior")
    void testScanningBehavior() {
        Bot bot = TestBotBuilder.create()
                .withBehavior(TestBotBuilder.BotBehavior.SCANNING)
                .build();

        assertThat(bot).isNotNull();
    }

    @Tag("Unit")
    @Test
    @DisplayName("TestBotBuilder onTick callback is invoked")
    void testOnTickCallback() throws InterruptedException {
        CountDownLatch tickCalled = new CountDownLatch(1);

        Bot bot = TestBotBuilder.create()
                .onTick(e -> tickCalled.countDown())
                .build();

        // Start bot in separate thread
        Thread botThread = new Thread(bot::start);
        botThread.start();

        try {
            // Wait for bot to be ready and receive tick
            assertThat(server.awaitBotReady(2000)).isTrue();

            assertThat(tickCalled.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS)).isTrue();
        } finally {
            botThread.interrupt();
            botThread.join(1000);
        }
    }

    @Tag("Unit")
    @Test
    @DisplayName("TestBotBuilder onRun callback is invoked")
    void testOnRunCallback() throws InterruptedException {
        CountDownLatch runCalled = new CountDownLatch(1);

        Bot bot = TestBotBuilder.create()
                .onRun(runCalled::countDown)
                .build();

        // Start bot in separate thread
        Thread botThread = new Thread(bot::start);
        botThread.start();

        try {
            // Wait for bot to be ready
            assertThat(server.awaitBotReady(2000)).isTrue();

            assertThat(runCalled.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS)).isTrue();
        } finally {
            botThread.interrupt();
            botThread.join(1000);
        }
    }

    @Tag("Unit")
    @Test
    @DisplayName("TestBotBuilder multiple callbacks can be chained")
    void testCallbackChaining() {
        AtomicInteger callbackCount = new AtomicInteger(0);

        Bot bot = TestBotBuilder.create()
                .withName("ChainedBot")
                .withBehavior(TestBotBuilder.BotBehavior.CUSTOM)
                .onTick(e -> callbackCount.incrementAndGet())
                .onScannedBot(e -> callbackCount.incrementAndGet())
                .onHitBot(e -> callbackCount.incrementAndGet())
                .onHitWall(e -> callbackCount.incrementAndGet())
                .onDeath(e -> callbackCount.incrementAndGet())
                .build();

        assertThat(bot).isNotNull();
    }

    @Tag("Unit")
    @Test
    @DisplayName("TestBotBuilder custom behavior relies on callbacks only")
    void testCustomBehavior() throws InterruptedException {
        CountDownLatch customTickHandled = new CountDownLatch(1);

        Bot bot = TestBotBuilder.create()
                .withBehavior(TestBotBuilder.BotBehavior.CUSTOM)
                .onTick(e -> customTickHandled.countDown())
                .build();

        // Start bot in separate thread
        Thread botThread = new Thread(bot::start);
        botThread.start();

        try {
            // Wait for bot to be ready and receive tick
            assertThat(server.awaitBotReady(2000)).isTrue();

            assertThat(customTickHandled.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS)).isTrue();
        } finally {
            botThread.interrupt();
            botThread.join(1000);
        }
    }

    @Tag("Unit")
    @Test
    @DisplayName("TestBotBuilder can build multiple bots from same builder")
    void testMultipleBotsFromSameBuilder() {
        TestBotBuilder builder = TestBotBuilder.create()
                .withName("ReusableBot")
                .withBehavior(TestBotBuilder.BotBehavior.PASSIVE);

        Bot bot1 = builder.build();
        Bot bot2 = builder.build();

        assertThat(bot1).isNotNull();
        assertThat(bot2).isNotNull();
        assertThat(bot1).isNotSameAs(bot2);
    }
}
