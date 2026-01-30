package test_utils;

import dev.robocode.tankroyale.botapi.BaseBot;
import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.InitialPosition;
import dev.robocode.tankroyale.botapi.events.*;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Builder/factory for creating configurable test bots.
 * <p>
 * This builder allows tests to create bots with specific behaviors without
 * creating separate bot classes for each test scenario. It supports:
 * <ul>
 *   <li>Predefined behavior profiles (passive, aggressive, scanning)</li>
 *   <li>Custom event handler callbacks for targeted testing</li>
 *   <li>Fluent API for easy configuration</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * var bot = TestBotBuilder.create()
 *     .withBehavior(BotBehavior.PASSIVE)
 *     .onTick(e -> bot.setTurnRate(5))
 *     .build(serverUrl);
 * }</pre>
 */
public class TestBotBuilder {

    /**
     * Predefined bot behavior profiles.
     */
    public enum BotBehavior {
        /**
         * Does nothing - just calls go() each tick.
         */
        PASSIVE,

        /**
         * Moves forward and fires continuously.
         */
        AGGRESSIVE,

        /**
         * Continuously rotates radar to scan for enemies.
         */
        SCANNING,

        /**
         * Custom behavior defined by callbacks only.
         */
        CUSTOM
    }

    // Bot info configuration
    private String name = "TestBot";
    private String version = "1.0";
    private String[] authors = {"Test Author"};
    private String description = "Test bot for unit tests";
    private String platform = "JVM";
    private String programmingLang = "Java";

    // Behavior configuration
    private BotBehavior behavior = BotBehavior.PASSIVE;

    // Event callbacks
    private Consumer<TickEvent> onTickCallback;
    private Consumer<ScannedBotEvent> onScannedBotCallback;
    private Consumer<HitBotEvent> onHitBotCallback;
    private Consumer<HitWallEvent> onHitWallCallback;
    private Consumer<BulletFiredEvent> onBulletFiredCallback;
    private Consumer<HitByBulletEvent> onHitByBulletCallback;
    private Consumer<BulletHitBotEvent> onBulletHitBotCallback;
    private Consumer<DeathEvent> onDeathCallback;
    private Consumer<RoundStartedEvent> onRoundStartedCallback;
    private Consumer<RoundEndedEvent> onRoundEndedCallback;
    private Consumer<GameStartedEvent> onGameStartedCallback;
    private Consumer<GameEndedEvent> onGameEndedCallback;
    private Runnable onRunCallback;

    private TestBotBuilder() {
    }

    /**
     * Create a new TestBotBuilder instance.
     *
     * @return a new builder
     */
    public static TestBotBuilder create() {
        return new TestBotBuilder();
    }

    /**
     * Set the bot name.
     *
     * @param name the bot name
     * @return this builder
     */
    public TestBotBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the bot version.
     *
     * @param version the bot version
     * @return this builder
     */
    public TestBotBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Set the bot authors.
     *
     * @param authors the bot authors
     * @return this builder
     */
    public TestBotBuilder withAuthors(String... authors) {
        this.authors = authors;
        return this;
    }

    /**
     * Set the bot behavior profile.
     *
     * @param behavior the predefined behavior
     * @return this builder
     */
    public TestBotBuilder withBehavior(BotBehavior behavior) {
        this.behavior = behavior;
        return this;
    }

    /**
     * Set a callback for the onTick event.
     * This callback is invoked AFTER any behavior-specific tick handling.
     *
     * @param callback the tick event callback
     * @return this builder
     */
    public TestBotBuilder onTick(Consumer<TickEvent> callback) {
        this.onTickCallback = callback;
        return this;
    }

    /**
     * Set a callback for the onScannedBot event.
     *
     * @param callback the scanned bot event callback
     * @return this builder
     */
    public TestBotBuilder onScannedBot(Consumer<ScannedBotEvent> callback) {
        this.onScannedBotCallback = callback;
        return this;
    }

    /**
     * Set a callback for the onHitBot event.
     *
     * @param callback the hit bot event callback
     * @return this builder
     */
    public TestBotBuilder onHitBot(Consumer<HitBotEvent> callback) {
        this.onHitBotCallback = callback;
        return this;
    }

    /**
     * Set a callback for the onHitWall event.
     *
     * @param callback the hit wall event callback
     * @return this builder
     */
    public TestBotBuilder onHitWall(Consumer<HitWallEvent> callback) {
        this.onHitWallCallback = callback;
        return this;
    }

    /**
     * Set a callback for the onBulletFired event.
     *
     * @param callback the bullet fired event callback
     * @return this builder
     */
    public TestBotBuilder onBulletFired(Consumer<BulletFiredEvent> callback) {
        this.onBulletFiredCallback = callback;
        return this;
    }

    /**
     * Set a callback for the onHitByBullet event.
     *
     * @param callback the hit by bullet event callback
     * @return this builder
     */
    public TestBotBuilder onHitByBullet(Consumer<HitByBulletEvent> callback) {
        this.onHitByBulletCallback = callback;
        return this;
    }

    /**
     * Set a callback for the onBulletHitBot event.
     *
     * @param callback the bullet hit bot event callback
     * @return this builder
     */
    public TestBotBuilder onBulletHitBot(Consumer<BulletHitBotEvent> callback) {
        this.onBulletHitBotCallback = callback;
        return this;
    }

    /**
     * Set a callback for the onDeath event.
     *
     * @param callback the death event callback
     * @return this builder
     */
    public TestBotBuilder onDeath(Consumer<DeathEvent> callback) {
        this.onDeathCallback = callback;
        return this;
    }

    /**
     * Set a callback for the onRoundStarted event.
     *
     * @param callback the round started event callback
     * @return this builder
     */
    public TestBotBuilder onRoundStarted(Consumer<RoundStartedEvent> callback) {
        this.onRoundStartedCallback = callback;
        return this;
    }

    /**
     * Set a callback for the onRoundEnded event.
     *
     * @param callback the round ended event callback
     * @return this builder
     */
    public TestBotBuilder onRoundEnded(Consumer<RoundEndedEvent> callback) {
        this.onRoundEndedCallback = callback;
        return this;
    }

    /**
     * Set a callback for the onGameStarted event.
     *
     * @param callback the game started event callback
     * @return this builder
     */
    public TestBotBuilder onGameStarted(Consumer<GameStartedEvent> callback) {
        this.onGameStartedCallback = callback;
        return this;
    }

    /**
     * Set a callback for the onGameEnded event.
     *
     * @param callback the game ended event callback
     * @return this builder
     */
    public TestBotBuilder onGameEnded(Consumer<GameEndedEvent> callback) {
        this.onGameEndedCallback = callback;
        return this;
    }

    /**
     * Set a callback for the run() method.
     * This is called once when the bot starts running.
     *
     * @param callback the run callback
     * @return this builder
     */
    public TestBotBuilder onRun(Runnable callback) {
        this.onRunCallback = callback;
        return this;
    }

    /**
     * Build the test bot with the configured settings.
     *
     * @param serverUrl the server URL to connect to
     * @return the configured test bot
     */
    public Bot build(URI serverUrl) {
        BotInfo.IBuilder botInfoBuilder = BotInfo.builder()
                .setName(name)
                .setVersion(version);

        for (String author : authors) {
            botInfoBuilder.addAuthor(author);
        }

        botInfoBuilder
                .setDescription(description)
                .setPlatform(platform)
                .setProgrammingLang(programmingLang);

        BotInfo botInfo = botInfoBuilder.build();

        return new ConfigurableBot(botInfo, serverUrl, this);
    }

    /**
     * Build the test bot using the default MockedServer URL.
     *
     * @return the configured test bot
     */
    public Bot build() {
        return build(MockedServer.getServerUrl());
    }

    /**
     * Internal bot class that delegates to callbacks.
     */
    private static class ConfigurableBot extends Bot {
        private final TestBotBuilder config;

        ConfigurableBot(BotInfo botInfo, URI serverUrl, TestBotBuilder config) {
            super(botInfo, serverUrl);
            this.config = config;
        }

        @Override
        public void run() {
            // Execute custom run callback if provided
            if (config.onRunCallback != null) {
                config.onRunCallback.run();
            }

            // Main loop based on behavior
            while (isRunning()) {
                switch (config.behavior) {
                    case PASSIVE:
                        // Do nothing, just go
                        break;
                    case AGGRESSIVE:
                        setForward(100);
                        setFire(1);
                        break;
                    case SCANNING:
                        setRadarTurnRate(45);
                        break;
                    case CUSTOM:
                        // Custom behavior is handled entirely by callbacks
                        break;
                }
                go();
            }
        }

        @Override
        public void onTick(TickEvent tickEvent) {
            // Apply behavior-specific tick actions
            switch (config.behavior) {
                case AGGRESSIVE:
                    // Continue firing if gun is cool
                    if (getGunHeat() == 0) {
                        setFire(1);
                    }
                    break;
                case SCANNING:
                    // Keep radar spinning
                    setRadarTurnRate(45);
                    break;
                default:
                    break;
            }

            // Invoke custom callback
            if (config.onTickCallback != null) {
                config.onTickCallback.accept(tickEvent);
            }
        }

        @Override
        public void onScannedBot(ScannedBotEvent scannedBotEvent) {
            // Behavior-specific actions
            if (config.behavior == BotBehavior.AGGRESSIVE) {
                // Turn towards scanned bot and fire
                double bearing = directionTo(scannedBotEvent.getX(), scannedBotEvent.getY());
                setTurnRate(bearing - getDirection());
                setFire(3);
            }

            if (config.onScannedBotCallback != null) {
                config.onScannedBotCallback.accept(scannedBotEvent);
            }
        }

        @Override
        public void onHitBot(HitBotEvent botHitBotEvent) {
            if (config.onHitBotCallback != null) {
                config.onHitBotCallback.accept(botHitBotEvent);
            }
        }

        @Override
        public void onHitWall(HitWallEvent botHitWallEvent) {
            // Behavior-specific actions
            if (config.behavior == BotBehavior.AGGRESSIVE) {
                // Reverse direction when hitting wall
                setForward(-100);
            }

            if (config.onHitWallCallback != null) {
                config.onHitWallCallback.accept(botHitWallEvent);
            }
        }

        @Override
        public void onBulletFired(BulletFiredEvent bulletFiredEvent) {
            if (config.onBulletFiredCallback != null) {
                config.onBulletFiredCallback.accept(bulletFiredEvent);
            }
        }

        @Override
        public void onHitByBullet(HitByBulletEvent hitByBulletEvent) {
            if (config.onHitByBulletCallback != null) {
                config.onHitByBulletCallback.accept(hitByBulletEvent);
            }
        }

        @Override
        public void onBulletHit(BulletHitBotEvent bulletHitBotEvent) {
            if (config.onBulletHitBotCallback != null) {
                config.onBulletHitBotCallback.accept(bulletHitBotEvent);
            }
        }

        @Override
        public void onDeath(DeathEvent deathEvent) {
            if (config.onDeathCallback != null) {
                config.onDeathCallback.accept(deathEvent);
            }
        }

        @Override
        public void onRoundStarted(RoundStartedEvent roundStartedEvent) {
            if (config.onRoundStartedCallback != null) {
                config.onRoundStartedCallback.accept(roundStartedEvent);
            }
        }

        @Override
        public void onRoundEnded(RoundEndedEvent roundEndedEvent) {
            if (config.onRoundEndedCallback != null) {
                config.onRoundEndedCallback.accept(roundEndedEvent);
            }
        }

        @Override
        public void onGameStarted(GameStartedEvent gameStartedEvent) {
            if (config.onGameStartedCallback != null) {
                config.onGameStartedCallback.accept(gameStartedEvent);
            }
        }

        @Override
        public void onGameEnded(GameEndedEvent gameEndedEvent) {
            if (config.onGameEndedCallback != null) {
                config.onGameEndedCallback.accept(gameEndedEvent);
            }
        }
    }
}
