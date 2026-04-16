package dev.robocode.tankroyale.botapi.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.robocode.tankroyale.botapi.IBaseBot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.Constants;
import dev.robocode.tankroyale.botapi.graphics.Color;
import dev.robocode.tankroyale.schema.BotIntent;
import dev.robocode.tankroyale.botapi.events.Condition;
import dev.robocode.tankroyale.botapi.events.BotEvent;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SharedTestRunner {

    private static final Path SHARED_TESTS_DIR = Paths.get("../tests/shared");

    static class TestSuite {
        String suite;
        String description;
        List<TestCase> tests;
    }

    static class TestCase {
        String id;
        String description;
        String type;
        String method;
        Map<String, Object> setup;
        List<Object> args;
        Map<String, Object> expected;

        @Override
        public String toString() {
            return id + ": " + description;
        }
    }

    @TestFactory
    Stream<DynamicTest> runSharedTests() throws IOException {
        if (!Files.exists(SHARED_TESTS_DIR)) {
            // Fallback for different execution environments
            Path altPath = Paths.get("bot-api/tests/shared");
            if (Files.exists(altPath)) {
                return createTestsFromDir(altPath);
            }
            throw new RuntimeException("Shared tests directory not found at " + SHARED_TESTS_DIR.toAbsolutePath());
        }
        return createTestsFromDir(SHARED_TESTS_DIR);
    }

    private Stream<DynamicTest> createTestsFromDir(Path dir) throws IOException {
        return Files.list(dir)
                .filter(path -> path.toString().endsWith(".json") && !path.toString().endsWith("schema.json"))
                .flatMap(this::createTestsFromFile);
    }

    private Stream<DynamicTest> createTestsFromFile(Path file) {
        try (FileReader reader = new FileReader(file.toFile())) {
            TestSuite suite = new Gson().fromJson(reader, TestSuite.class);
            return suite.tests.stream().map(testCase ->
                DynamicTest.dynamicTest(suite.suite + " | " + testCase.toString(), () -> executeTest(testCase))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeTest(TestCase testCase) {
        // Mocking IBaseBot to provide state for BaseBotInternals
        TestBot mockBot = new TestBot();
        BaseBotInternals internals = new BaseBotInternals(mockBot, null, null, null);

        // Setup state
        if (testCase.setup != null) {
            if (testCase.setup.containsKey("energy")) {
                mockBot.setEnergy(((Number) testCase.setup.get("energy")).doubleValue());
            }
            if (testCase.setup.containsKey("gunHeat")) {
                mockBot.setGunHeat(((Number) testCase.setup.get("gunHeat")).doubleValue());
            }
            if (testCase.setup.containsKey("maxSpeed")) {
                internals.setMaxSpeed(((Number) testCase.setup.get("maxSpeed")).doubleValue());
            }
            if (testCase.setup.containsKey("maxTurnRate")) {
                internals.setMaxTurnRate(((Number) testCase.setup.get("maxTurnRate")).doubleValue());
            }
            if (testCase.setup.containsKey("maxGunTurnRate")) {
                internals.setMaxGunTurnRate(((Number) testCase.setup.get("maxGunTurnRate")).doubleValue());
            }
            if (testCase.setup.containsKey("maxRadarTurnRate")) {
                internals.setMaxRadarTurnRate(((Number) testCase.setup.get("maxRadarTurnRate")).doubleValue());
            }
        }

        Object[] lastActionValue = new Object[1];

        Runnable action = () -> {
            Object[] args = (testCase.args != null) ? testCase.args.toArray() : new Object[0];
            for (int i = 0; i < args.length; i++) {
                args[i] = parseArg(args[i]);
            }

            switch (testCase.method) {
                case "setFire":
                    lastActionValue[0] = internals.setFire((Double) args[0]);
                    break;
                case "setTurnRate":
                    internals.setTurnRate((Double) args[0]);
                    break;
                case "setGunTurnRate":
                    internals.setGunTurnRate((Double) args[0]);
                    break;
                case "setRadarTurnRate":
                    internals.setRadarTurnRate((Double) args[0]);
                    break;
                case "setTargetSpeed":
                    internals.setTargetSpeed((Double) args[0]);
                    break;
                case "setMaxSpeed":
                    internals.setMaxSpeed((Double) args[0]);
                    break;
                case "setMaxTurnRate":
                    internals.setMaxTurnRate((Double) args[0]);
                    break;
                case "getNewTargetSpeed":
                    lastActionValue[0] = IntentValidator.getNewTargetSpeed((Double) args[0], (Double) args[1], (Double) args[2]);
                    break;
                case "getDistanceTraveledUntilStop":
                    lastActionValue[0] = IntentValidator.getDistanceTraveledUntilStop((Double) args[0], (Double) args[1]);
                    break;
                case "BotInfo":
                    lastActionValue[0] = new BotInfo(
                            (String) args[0],
                            (String) args[1],
                            (List<String>) args[2],
                            args.length > 3 ? (String) args[3] : null,
                            args.length > 4 ? (String) args[4] : null,
                            args.length > 5 ? (List<String>) args[5] : null,
                            args.length > 6 ? (Collection<String>) args[6] : null,
                            args.length > 7 ? (String) args[7] : null,
                            args.length > 8 ? (String) args[8] : null,
                            null
                    );
                    break;
                case "fromRgb":
                    lastActionValue[0] = Color.fromRgb(((Number) args[0]).intValue(), ((Number) args[1]).intValue(), ((Number) args[2]).intValue());
                    break;
                case "fromRgba":
                    lastActionValue[0] = Color.fromRgba(((Number) args[0]).intValue(), ((Number) args[1]).intValue(), ((Number) args[2]).intValue(), ((Number) args[3]).intValue());
                    break;
                case "colorToHex":
                    lastActionValue[0] = IntentValidator.colorToHex((Color) args[0]);
                    break;
                case "getColorConstant":
                    lastActionValue[0] = getStaticField(Color.class, (String) args[0]);
                    break;
                case "getConstant":
                    lastActionValue[0] = getStaticField(Constants.class, (String) args[0]);
                    break;
                default:
                    throw new UnsupportedOperationException("Method not implemented in runner: " + testCase.method);
            }
        };

        if (testCase.expected.containsKey("throws")) {
            String expectedException = (String) testCase.expected.get("throws");
            if ("IllegalArgumentException".equals(expectedException)) {
                assertThrows(IllegalArgumentException.class, action::run);
            } else {
                throw new UnsupportedOperationException("Exception type not implemented in runner: " + expectedException);
            }
        } else {
            action.run();

            if (testCase.expected.containsKey("returns")) {
                Object expected = parseArg(testCase.expected.get("returns"));
                if (expected instanceof Number && lastActionValue[0] instanceof Number) {
                    assertThat(((Number) lastActionValue[0]).doubleValue()).isEqualTo(((Number) expected).doubleValue());
                } else if (expected instanceof String && lastActionValue[0] instanceof String && ((String) expected).startsWith("#")) {
                    assertThat((String) lastActionValue[0]).isEqualToIgnoringCase((String) expected);
                } else {
                    assertThat(lastActionValue[0]).isEqualTo(expected);
                }
            }

            // Verify expected state in botIntent or lastActionValue object fields
            BotIntent intent = internals.getBotIntent();
            for (Map.Entry<String, Object> entry : testCase.expected.entrySet()) {
                String key = entry.getKey();
                if (key.equals("returns") || key.equals("throws")) continue;

                Object expectedValue = entry.getValue();
                Object actualValue = getActualValue(key, lastActionValue[0], intent, internals);

                if (expectedValue instanceof Number && actualValue instanceof Number) {
                    assertThat(((Number) actualValue).doubleValue()).isEqualTo(((Number) expectedValue).doubleValue());
                } else if (expectedValue instanceof Collection && actualValue instanceof Collection) {
                    assertThat((Collection) actualValue).containsExactlyElementsOf((Collection) expectedValue);
                } else {
                    assertThat(actualValue).isEqualTo(expectedValue);
                }
            }
        }
    }

    private Object getActualValue(String key, Object lastActionValue, BotIntent intent, BaseBotInternals internals) {
        switch (key) {
            case "firepower": return intent.getFirepower() == null ? 0.0 : intent.getFirepower();
            case "turnRate": return intent.getTurnRate();
            case "gunTurnRate": return intent.getGunTurnRate();
            case "radarTurnRate": return intent.getRadarTurnRate();
            case "targetSpeed": return intent.getTargetSpeed();
            case "maxSpeed": return internals.getMaxSpeed();
            case "maxTurnRate": return internals.getMaxTurnRate();
        }
        // Fallback: try to get property from lastActionValue (e.g., BotInfo or Color fields)
        if (lastActionValue != null) {
            try {
                if (lastActionValue instanceof Color) {
                    Color c = (Color) lastActionValue;
                    switch (key) {
                        case "r": return c.getR();
                        case "g": return c.getG();
                        case "b": return c.getB();
                        case "a": return c.getA();
                    }
                }
                if (lastActionValue instanceof BotInfo) {
                    BotInfo info = (BotInfo) lastActionValue;
                    switch (key) {
                        case "name": return info.getName();
                        case "version": return info.getVersion();
                        case "authors": return info.getAuthors();
                        case "countryCodes": return info.getCountryCodes();
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    private Object parseArg(Object arg) {
        if ("NaN".equals(arg)) return Double.NaN;
        if ("Infinity".equals(arg)) return Double.POSITIVE_INFINITY;
        if ("-Infinity".equals(arg)) return Double.NEGATIVE_INFINITY;
        if (arg instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) arg;
            if (map.containsKey("r") && map.containsKey("g") && map.containsKey("b")) {
                int r = ((Number) map.get("r")).intValue();
                int g = ((Number) map.get("g")).intValue();
                int b = ((Number) map.get("b")).intValue();
                int a = map.containsKey("a") ? ((Number) map.get("a")).intValue() : 255;
                return Color.fromRgba(r, g, b, a);
            }
        }
        return arg;
    }

    private Object getStaticField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            return field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class TestBot implements IBaseBot {
        private double energy = 100.0;
        private double gunHeat = 0.0;

        void setEnergy(double energy) { this.energy = energy; }
        void setGunHeat(double gunHeat) { this.gunHeat = gunHeat; }

        @Override public double getEnergy() { return energy; }
        @Override public double getGunHeat() { return gunHeat; }
        
        // Dummy implementations for the rest
        @Override public void start() {}
        @Override public void go() {}
        @Override public int getMyId() { return 1; }
        @Override public String getVariant() { return ""; }
        @Override public String getVersion() { return ""; }
        @Override public String getGameType() { return ""; }
        @Override public int getArenaWidth() { return 800; }
        @Override public int getArenaHeight() { return 600; }
        @Override public int getNumberOfRounds() { return 1; }
        @Override public double getGunCoolingRate() { return 0.1; }
        @Override public int getMaxInactivityTurns() { return 450; }
        @Override public int getTurnTimeout() { return 30000; }
        @Override public int getTimeLeft() { return 0; }
        @Override public int getRoundNumber() { return 1; }
        @Override public int getTurnNumber() { return 1; }
        @Override public int getEnemyCount() { return 0; }
        @Override public boolean isDisabled() { return false; }
        @Override public double getX() { return 0; }
        @Override public double getY() { return 0; }
        @Override public double getDirection() { return 0; }
        @Override public double getGunDirection() { return 0; }
        @Override public double getRadarDirection() { return 0; }
        @Override public double getSpeed() { return 0; }
        @Override public Collection<dev.robocode.tankroyale.botapi.BulletState> getBulletStates() { return Collections.emptyList(); }
        @Override public List<dev.robocode.tankroyale.botapi.events.BotEvent> getEvents() { return Collections.emptyList(); }
        @Override public void clearEvents() {}
        @Override public double getTurnRate() { return 0; }
        @Override public void setTurnRate(double turnRate) {}
        @Override public double getMaxTurnRate() { return 10; }
        @Override public void setMaxTurnRate(double maxTurnRate) {}
        @Override public double getGunTurnRate() { return 0; }
        @Override public void setGunTurnRate(double gunTurnRate) {}
        @Override public double getMaxGunTurnRate() { return 20; }
        @Override public void setMaxGunTurnRate(double maxGunTurnRate) {}
        @Override public double getRadarTurnRate() { return 0; }
        @Override public void setRadarTurnRate(double gunRadarTurnRate) {}
        @Override public double getMaxRadarTurnRate() { return 45; }
        @Override public void setMaxRadarTurnRate(double maxRadarTurnRate) {}
        @Override public double getTargetSpeed() { return 0; }
        @Override public void setTargetSpeed(double targetSpeed) {}
        @Override public double getMaxSpeed() { return 8; }
        @Override public void setMaxSpeed(double maxSpeed) {}
        @Override public boolean setFire(double firepower) { return false; }
        @Override public double getFirepower() { return 0; }
        @Override public void setRescan() {}
        @Override public void setFireAssist(boolean enable) {}
        @Override public void setInterruptible(boolean interruptible) {}
        @Override public void setAdjustGunForBodyTurn(boolean adjust) {}
        @Override public boolean isAdjustGunForBodyTurn() { return false; }
        @Override public void setAdjustRadarForBodyTurn(boolean adjust) {}
        @Override public boolean isAdjustRadarForBodyTurn() { return false; }
        @Override public void setAdjustRadarForGunTurn(boolean adjust) {}
        @Override public boolean isAdjustRadarForGunTurn() { return false; }
        @Override public boolean addCustomEvent(Condition condition) { return false; }
        @Override public boolean removeCustomEvent(Condition condition) { return false; }
        @Override public void setEventPriority(Class<BotEvent> eventClass, int priority) {}
        @Override public int getEventPriority(Class<BotEvent> eventClass) { return 0; }
        @Override public double calcMaxTurnRate(double speed) { return 0; }
        @Override public double calcBulletSpeed(double firepower) { return 0; }
        @Override public double calcGunHeat(double firepower) { return 0; }
        @Override public void setStop() {}
        @Override public void setStop(boolean overwrite) {}
        @Override public void setResume() {}
        @Override public Set<Integer> getTeammateIds() { return Collections.emptySet(); }
        @Override public boolean isTeammate(int botId) { return false; }
        @Override public void broadcastTeamMessage(Object message) {}
        @Override public void sendTeamMessage(int teammateId, Object message) {}
        @Override public boolean isStopped() { return false; }
        @Override public Color getBodyColor() { return null; }
        @Override public void setBodyColor(Color color) {}
        @Override public Color getTurretColor() { return null; }
        @Override public void setTurretColor(Color color) {}
        @Override public Color getRadarColor() { return null; }
        @Override public void setRadarColor(Color color) {}
        @Override public Color getBulletColor() { return null; }
        @Override public void setBulletColor(Color color) {}
        @Override public Color getScanColor() { return null; }
        @Override public void setScanColor(Color color) {}
        @Override public Color getTracksColor() { return null; }
        @Override public void setTracksColor(Color color) {}
        @Override public Color getGunColor() { return null; }
        @Override public void setGunColor(Color color) {}
        @Override public boolean isDebuggingEnabled() { return false; }
        @Override public dev.robocode.tankroyale.botapi.graphics.IGraphics getGraphics() { return null; }
        @Override public void onConnected(dev.robocode.tankroyale.botapi.events.ConnectedEvent connectedEvent) {}
        @Override public void onDisconnected(dev.robocode.tankroyale.botapi.events.DisconnectedEvent disconnectedEvent) {}
        @Override public void onConnectionError(dev.robocode.tankroyale.botapi.events.ConnectionErrorEvent connectionErrorEvent) {}
        @Override public void onGameStarted(dev.robocode.tankroyale.botapi.events.GameStartedEvent gameStatedEvent) {}
        @Override public void onGameEnded(dev.robocode.tankroyale.botapi.events.GameEndedEvent gameEndedEvent) {}
        @Override public void onRoundStarted(dev.robocode.tankroyale.botapi.events.RoundStartedEvent roundStartedEvent) {}
        @Override public void onRoundEnded(dev.robocode.tankroyale.botapi.events.RoundEndedEvent roundEndedEvent) {}
        @Override public void onTick(dev.robocode.tankroyale.botapi.events.TickEvent tickEvent) {}
        @Override public void onBotDeath(dev.robocode.tankroyale.botapi.events.BotDeathEvent botDeathEvent) {}
        @Override public void onDeath(dev.robocode.tankroyale.botapi.events.DeathEvent deathEvent) {}
        @Override public void onHitBot(dev.robocode.tankroyale.botapi.events.HitBotEvent botHitBotEvent) {}
        @Override public void onHitWall(dev.robocode.tankroyale.botapi.events.HitWallEvent botHitWallEvent) {}
        @Override public void onBulletFired(dev.robocode.tankroyale.botapi.events.BulletFiredEvent bulletFiredEvent) {}
        @Override public void onHitByBullet(dev.robocode.tankroyale.botapi.events.HitByBulletEvent hitByBulletEvent) {}
        @Override public void onBulletHit(dev.robocode.tankroyale.botapi.events.BulletHitBotEvent bulletHitBotEvent) {}
        @Override public void onBulletHitBullet(dev.robocode.tankroyale.botapi.events.BulletHitBulletEvent bulletHitBulletEvent) {}
        @Override public void onBulletHitWall(dev.robocode.tankroyale.botapi.events.BulletHitWallEvent bulletHitWallEvent) {}
        @Override public void onScannedBot(dev.robocode.tankroyale.botapi.events.ScannedBotEvent scannedBotEvent) {}
    }
}
