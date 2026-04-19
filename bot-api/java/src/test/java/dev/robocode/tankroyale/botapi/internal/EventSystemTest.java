package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import dev.robocode.tankroyale.botapi.graphics.IGraphics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("EVT")
class EventSystemTest {

    static class BaseBotStub implements IBaseBot {
        final List<BotEvent> firedEvents = new ArrayList<>();

        @Override public void start() {}
        @Override public void go() {}
        @Override public int getMyId() { return 0; }
        @Override public String getVariant() { return ""; }
        @Override public String getVersion() { return ""; }
        @Override public String getGameType() { return ""; }
        @Override public int getArenaWidth() { return 0; }
        @Override public int getArenaHeight() { return 0; }
        @Override public int getNumberOfRounds() { return 0; }
        @Override public double getGunCoolingRate() { return 0; }
        @Override public int getMaxInactivityTurns() { return 0; }
        @Override public int getTurnTimeout() { return 0; }
        @Override public int getTimeLeft() { return 0; }
        @Override public int getRoundNumber() { return 0; }
        @Override public int getTurnNumber() { return 0; }
        @Override public int getEnemyCount() { return 0; }
        @Override public double getEnergy() { return 0; }
        @Override public boolean isDisabled() { return false; }
        @Override public double getX() { return 0; }
        @Override public double getY() { return 0; }
        @Override public double getDirection() { return 0; }
        @Override public double getGunDirection() { return 0; }
        @Override public double getRadarDirection() { return 0; }
        @Override public double getSpeed() { return 0; }
        @Override public double getGunHeat() { return 0; }
        @Override public Collection<BulletState> getBulletStates() { return Collections.emptyList(); }
        @Override public List<BotEvent> getEvents() { return Collections.emptyList(); }
        @Override public void clearEvents() {}
        @Override public double getTurnRate() { return 0; }
        @Override public void setTurnRate(double turnRate) {}
        @Override public double getMaxTurnRate() { return 0; }
        @Override public void setMaxTurnRate(double maxTurnRate) {}
        @Override public double getGunTurnRate() { return 0; }
        @Override public void setGunTurnRate(double gunTurnRate) {}
        @Override public double getMaxGunTurnRate() { return 0; }
        @Override public void setMaxGunTurnRate(double maxGunTurnRate) {}
        @Override public double getRadarTurnRate() { return 0; }
        @Override public void setRadarTurnRate(double radarTurnRate) {}
        @Override public double getMaxRadarTurnRate() { return 0; }
        @Override public void setMaxRadarTurnRate(double maxRadarTurnRate) {}
        @Override public double getTargetSpeed() { return 0; }
        @Override public void setTargetSpeed(double targetSpeed) {}
        @Override public double getMaxSpeed() { return 0; }
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
        @Override public IGraphics getGraphics() { return null; }
        @Override public double calcMaxTurnRate(double speed) { return 0; }
        @Override public double calcBulletSpeed(double firepower) { return 0; }
        @Override public double calcGunHeat(double firepower) { return 0; }
        @Override public int getEventPriority(Class<BotEvent> eventClass) { return 0; }
        @Override public void setEventPriority(Class<BotEvent> eventClass, int priority) {}

        @Override public void onConnected(ConnectedEvent e) {}
        @Override public void onDisconnected(DisconnectedEvent e) {}
        @Override public void onConnectionError(ConnectionErrorEvent e) {}
        @Override public void onGameStarted(GameStartedEvent e) {}
        @Override public void onGameEnded(GameEndedEvent e) {}
        @Override public void onRoundStarted(RoundStartedEvent e) {}
        @Override public void onRoundEnded(RoundEndedEvent e) {}
        @Override public void onTick(TickEvent e) { firedEvents.add(e); }
        @Override public void onBotDeath(BotDeathEvent e) { firedEvents.add(e); }
        @Override public void onDeath(DeathEvent e) { firedEvents.add(e); }
        @Override public void onHitBot(HitBotEvent e) { firedEvents.add(e); }
        @Override public void onHitWall(HitWallEvent e) { firedEvents.add(e); }
        @Override public void onBulletFired(BulletFiredEvent e) { firedEvents.add(e); }
        @Override public void onHitByBullet(HitByBulletEvent e) { firedEvents.add(e); }
        @Override public void onBulletHit(BulletHitBotEvent e) { firedEvents.add(e); }
        @Override public void onBulletHitBullet(BulletHitBulletEvent e) { firedEvents.add(e); }
        @Override public void onBulletHitWall(BulletHitWallEvent e) { firedEvents.add(e); }
        @Override public void onScannedBot(ScannedBotEvent e) { firedEvents.add(e); }
        @Override public void onWonRound(WonRoundEvent e) { firedEvents.add(e); }
        @Override public void onCustomEvent(CustomEvent e) { firedEvents.add(e); }
        @Override public void onTeamMessage(TeamMessageEvent e) { firedEvents.add(e); }
    }

    private BaseBotStub botStub;
    private BaseBotInternals internals;
    private EventQueue queue;

    @BeforeEach
    void setUp() {
        botStub = new BaseBotStub();
        BotInfo botInfo = new BotInfo("dummy", "1.0", List.of("dummy"), null, null, null, null, null, null, null);
        internals = new BaseBotInternals(botStub, botInfo, URI.create("ws://localhost:7654"), null);
        queue = new EventQueue(internals, internals.getBotEventHandlers());
    }

    @Test
    @Tag("TR-API-EVT-001")
    void test_TR_API_EVT_001_event_constructors() {
        // TickEvent
        TickEvent te = new TickEvent(1, 2, null, Collections.emptyList(), Collections.emptyList());
        assertEquals(1, te.getTurnNumber());
        assertEquals(2, te.getRoundNumber());

        // ScannedBotEvent
        ScannedBotEvent sbe = new ScannedBotEvent(3, 1, 2, 80.0, 100.0, 200.0, 45.0, 5.0);
        assertEquals(3, sbe.getTurnNumber());
        assertEquals(1, sbe.getScannedByBotId());
        assertEquals(2, sbe.getScannedBotId());
        assertEquals(80.0, sbe.getEnergy());
        assertEquals(100.0, sbe.getX());
        assertEquals(200.0, sbe.getY());
        assertEquals(45.0, sbe.getDirection());
        assertEquals(5.0, sbe.getSpeed());

        // HitBotEvent
        HitBotEvent hbe = new HitBotEvent(4, 5, 90.0, 10.0, 20.0, true);
        assertEquals(4, hbe.getTurnNumber());
        assertEquals(5, hbe.getVictimId());
        assertEquals(90.0, hbe.getEnergy());
        assertEquals(10.0, hbe.getX());
        assertEquals(20.0, hbe.getY());
        assertTrue(hbe.isRammed());

        // HitByBulletEvent
        BulletState bullet = new BulletState(1, 1, 3.0, 100.0, 200.0, 45.0, null);
        HitByBulletEvent hbbe = new HitByBulletEvent(6, bullet, 5.0, 95.0);
        assertEquals(6, hbbe.getTurnNumber());
        assertEquals(bullet, hbbe.getBullet());
        assertEquals(5.0, hbbe.getDamage());
        assertEquals(95.0, hbbe.getEnergy());

        // HitWallEvent
        HitWallEvent hwe = new HitWallEvent(7);
        assertEquals(7, hwe.getTurnNumber());

        // BulletFiredEvent
        BulletFiredEvent bfe = new BulletFiredEvent(8, bullet);
        assertEquals(8, bfe.getTurnNumber());
        assertEquals(bullet, bfe.getBullet());

        // BulletHitBotEvent
        BulletHitBotEvent bhbe = new BulletHitBotEvent(9, 10, bullet, 5.0, 90.0);
        assertEquals(9, bhbe.getTurnNumber());
        assertEquals(bullet, bhbe.getBullet());
        assertEquals(10, bhbe.getVictimId());
        assertEquals(5.0, bhbe.getDamage());
        assertEquals(90.0, bhbe.getEnergy());

        // BulletHitBulletEvent
        BulletState otherBullet = new BulletState(2, 2, 3.0, 150.0, 250.0, 90.0, null);
        BulletHitBulletEvent bhbue = new BulletHitBulletEvent(10, bullet, otherBullet);
        assertEquals(10, bhbue.getTurnNumber());
        assertEquals(bullet, bhbue.getBullet());
        assertEquals(otherBullet, bhbue.getHitBullet());

        // BulletHitWallEvent
        BulletHitWallEvent bhwe = new BulletHitWallEvent(11, bullet);
        assertEquals(11, bhwe.getTurnNumber());
        assertEquals(bullet, bhwe.getBullet());

        // BotDeathEvent
        BotDeathEvent bde = new BotDeathEvent(12, 13);
        assertEquals(12, bde.getTurnNumber());
        assertEquals(13, bde.getVictimId());

        // DeathEvent
        DeathEvent de = new DeathEvent(14);
        assertEquals(14, de.getTurnNumber());

        // SkippedTurnEvent
        SkippedTurnEvent ste = new SkippedTurnEvent(15);
        assertEquals(15, ste.getTurnNumber());

        // WonRoundEvent
        WonRoundEvent wre = new WonRoundEvent(16);
        assertEquals(16, wre.getTurnNumber());

        // TeamMessageEvent
        TeamMessageEvent tme = new TeamMessageEvent(17, "hello", 18);
        assertEquals(17, tme.getTurnNumber());
        assertEquals("hello", tme.getMessage());
        assertEquals(18, tme.getSenderId());

        // CustomEvent
        Condition condition = new Condition("test") {
            @Override public boolean test() { return true; }
        };
        CustomEvent ce = new CustomEvent(19, condition);
        assertEquals(19, ce.getTurnNumber());
        assertEquals(condition, ce.getCondition());
    }

    @Test
    @Tag("TR-API-EVT-008")
    void test_TR_API_EVT_008_condition_test_callable() {
        Condition c1 = new Condition("true") {
            @Override public boolean test() { return true; }
        };
        assertTrue(c1.test());

        Condition c2 = new Condition("false") {
            @Override public boolean test() { return false; }
        };
        assertFalse(c2.test());
    }

    @Test
    @Tag("TR-API-EVT-009")
    void test_TR_API_EVT_009_custom_event_firing() {
        Condition condTrue = new Condition("true") {
            @Override public boolean test() { return true; }
        };
        Condition condFalse = new Condition("false") {
            @Override public boolean test() { return false; }
        };

        internals.addCondition(condTrue);
        internals.addCondition(condFalse);
        
        TickEvent tick = new TickEvent(5, 1, null, Collections.emptyList(), Collections.emptyList());
        internals.setTickEvent(tick);

        queue.dispatchEvents(5);

        assertThat(botStub.firedEvents).hasSize(1);
        assertThat(botStub.firedEvents.get(0)).isInstanceOf(CustomEvent.class);
        assertEquals(condTrue, ((CustomEvent) botStub.firedEvents.get(0)).getCondition());
    }
}
