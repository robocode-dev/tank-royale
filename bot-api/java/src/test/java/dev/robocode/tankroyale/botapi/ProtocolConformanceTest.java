package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test_utils.MockedServer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cross-platform protocol conformance tests (TR-API-TCK-007 through TR-API-TCK-017).
 *
 * <p>These tests verify that the Java Bot API correctly handles protocol messages
 * as defined in the Tank Royale cross-platform TCK.
 *
 * @see <a href="../../../../../../tests/TEST-REGISTRY.md">TEST-REGISTRY.md</a>
 */
@Tag("TCK")
class ProtocolConformanceTest extends AbstractBotTest {

    // -----------------------------------------------------------------------
    // TCK-007: BotHandshake contains correct sessionId, name, version, authors, isDroid
    // -----------------------------------------------------------------------

    @Test
    @Tag("TR-API-TCK-007")
    void tck007_botHandshake_containsCorrectFields() {
        startAsync(new BaseBot(botInfo, java.net.URI.create(MockedServer.SERVER_URL)) {});
        assertThat(server.awaitBotHandshake(3000)).isTrue();

        var handshake = server.getBotHandshake();
        assertThat(handshake.getSessionId()).isEqualTo(MockedServer.SESSION_ID);
        assertThat(handshake.getName()).isEqualTo("TestBot");
        assertThat(handshake.getVersion()).isEqualTo("1.0");
        assertThat(handshake.getAuthors()).containsExactlyInAnyOrder("Author 1", "Author 2");
        assertThat(handshake.getIsDroid()).isFalse();
    }

    // -----------------------------------------------------------------------
    // TCK-008: Bot sends BotReady after GameStarted
    // -----------------------------------------------------------------------

    @Test
    @Tag("TR-API-TCK-008")
    void tck008_botSendsBotReadyAfterGameStarted() {
        startAsync(new BaseBot(botInfo, java.net.URI.create(MockedServer.SERVER_URL)) {});
        assertThat(server.awaitBotReadyMessage(3000)).isTrue();
    }

    // -----------------------------------------------------------------------
    // TCK-009: onRoundStarted fires with roundNumber==1
    // -----------------------------------------------------------------------

    @Test
    @Tag("TR-API-TCK-009")
    void tck009_onRoundStarted_firesWithRoundNumber1() throws InterruptedException {
        var latch = new CountDownLatch(1);
        var roundNumber = new AtomicReference<Integer>();

        startAsync(new BaseBot(botInfo, java.net.URI.create(MockedServer.SERVER_URL)) {
            @Override
            public void onRoundStarted(RoundStartedEvent event) {
                roundNumber.set(event.getRoundNumber());
                latch.countDown();
            }
        });

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(roundNumber.get()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // TCK-010: onRoundEnded fires with roundNumber==1, turnNumber==5
    // -----------------------------------------------------------------------

    @Test
    @Tag("TR-API-TCK-010")
    void tck010_onRoundEnded_firesWithCorrectNumbers() throws InterruptedException {
        var latch = new CountDownLatch(1);
        var capturedRound = new AtomicReference<Integer>();
        var capturedTurn = new AtomicReference<Integer>();

        startAsync(new BaseBot(botInfo, java.net.URI.create(MockedServer.SERVER_URL)) {
            @Override
            public void onRoundEnded(RoundEndedEvent event) {
                capturedRound.set(event.getRoundNumber());
                capturedTurn.set(event.getTurnNumber());
                latch.countDown();
            }
        });

        assertThat(server.awaitBotReadyMessage(3000)).isTrue();
        server.sendRawText(buildRoundEndedJson(1, 5));

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(capturedRound.get()).isEqualTo(1);
        assertThat(capturedTurn.get()).isEqualTo(5);
    }

    // -----------------------------------------------------------------------
    // TCK-011: onGameEnded fires with numberOfRounds==10
    // -----------------------------------------------------------------------

    @Test
    @Tag("TR-API-TCK-011")
    void tck011_onGameEnded_firesWithNumberOfRounds10() throws InterruptedException {
        var latch = new CountDownLatch(1);
        var capturedRounds = new AtomicReference<Integer>();

        startAsync(new BaseBot(botInfo, java.net.URI.create(MockedServer.SERVER_URL)) {
            @Override
            public void onGameEnded(GameEndedEvent event) {
                capturedRounds.set(event.getNumberOfRounds());
                latch.countDown();
            }
        });

        assertThat(server.awaitBotReadyMessage(3000)).isTrue();
        server.sendRawText(buildGameEndedJson(10));

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(capturedRounds.get()).isEqualTo(10);
    }

    // -----------------------------------------------------------------------
    // TCK-012: onSkippedTurn fires with turnNumber==1
    // -----------------------------------------------------------------------

    @Test
    @Tag("TR-API-TCK-012")
    void tck012_onSkippedTurn_firesWithTurnNumber1() throws InterruptedException {
        var latch = new CountDownLatch(1);
        var capturedTurn = new AtomicReference<Integer>();

        var bot = new BaseBot(botInfo, java.net.URI.create(MockedServer.SERVER_URL)) {
            @Override
            public void onSkippedTurn(SkippedTurnEvent event) {
                capturedTurn.set(event.getTurnNumber());
                latch.countDown();
            }
        };
        startAsync(bot);
        awaitGameStarted(bot);
        awaitTick(bot);
        server.sendRawText("{\"type\":\"SkippedTurnEvent\",\"turnNumber\":1}");
        goAsync(bot);
        server.continueBotIntent();
        awaitBotIntent();

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(capturedTurn.get()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // TCK-013: Unknown server message type triggers onConnectionError
    // -----------------------------------------------------------------------

    @Test
    @Tag("TR-API-TCK-013")
    void tck013_unknownMessageType_triggersOnConnectionError() throws InterruptedException {
        var latch = new CountDownLatch(1);
        var capturedMessage = new AtomicReference<String>();

        startAsync(new BaseBot(botInfo, java.net.URI.create(MockedServer.SERVER_URL)) {
            @Override
            public void onConnectionError(ConnectionErrorEvent event) {
                capturedMessage.set(event.getError().getMessage());
                latch.countDown();
            }
        });

        assertThat(server.awaitBotReadyMessage(3000)).isTrue();
        server.sendRawText("{\"type\":\"UnknownMessageType\",\"data\":\"test\"}");

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(capturedMessage.get()).contains("Unsupported WebSocket message type");
    }

    // -----------------------------------------------------------------------
    // TCK-014: BotDeathEvent(victimId==myId) triggers onDeath (isCritical=true)
    // -----------------------------------------------------------------------

    @Test
    @Tag("TR-API-TCK-014")
    void tck014_botDeathEventSelf_triggersOnDeath() throws InterruptedException {
        var latch = new CountDownLatch(1);

        var bot = new BaseBot(botInfo, java.net.URI.create(MockedServer.SERVER_URL)) {
            @Override
            public void onDeath(DeathEvent event) {
                latch.countDown();
            }
        };
        startAsync(bot);
        awaitGameStarted(bot);
        awaitTick(bot);
        server.sendRawText(buildTickWithEvent(
                "{\"type\":\"BotDeathEvent\",\"turnNumber\":2,\"victimId\":" + MockedServer.MY_ID + "}"));
        awaitTurnNumber(bot, 2);
        goAsync(bot);
        server.continueBotIntent();
        awaitBotIntent();

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
    }

    // -----------------------------------------------------------------------
    // TCK-015: BotDeathEvent(victimId!=myId) triggers onBotDeath
    // -----------------------------------------------------------------------

    @Test
    @Tag("TR-API-TCK-015")
    void tck015_botDeathEventOther_triggersOnBotDeath() throws InterruptedException {
        var latch = new CountDownLatch(1);
        var capturedId = new AtomicReference<Integer>();

        var bot = new BaseBot(botInfo, java.net.URI.create(MockedServer.SERVER_URL)) {
            @Override
            public void onBotDeath(BotDeathEvent event) {
                capturedId.set(event.getVictimId());
                latch.countDown();
            }
        };
        startAsync(bot);
        awaitGameStarted(bot);
        awaitTick(bot);
        server.sendRawText(buildTickWithEvent(
                "{\"type\":\"BotDeathEvent\",\"turnNumber\":2,\"victimId\":99}"));
        awaitTurnNumber(bot, 2);
        goAsync(bot);
        server.continueBotIntent();
        awaitBotIntent();

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(capturedId.get()).isEqualTo(99);
    }

    // -----------------------------------------------------------------------
    // TCK-016: BulletHitBotEvent(victimId==myId) triggers onHitByBullet
    // -----------------------------------------------------------------------

    @Test
    @Tag("TR-API-TCK-016")
    void tck016_bulletHitBotEventSelf_triggersOnHitByBullet() throws InterruptedException {
        var latch = new CountDownLatch(1);

        var bot = new BaseBot(botInfo, java.net.URI.create(MockedServer.SERVER_URL)) {
            @Override
            public void onHitByBullet(HitByBulletEvent event) {
                latch.countDown();
            }
        };
        startAsync(bot);
        awaitGameStarted(bot);
        awaitTick(bot);
        server.sendRawText(buildTickWithEvent(buildBulletHitBotEventJson(MockedServer.MY_ID)));
        awaitTurnNumber(bot, 2);
        goAsync(bot);
        server.continueBotIntent();
        awaitBotIntent();

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
    }

    // -----------------------------------------------------------------------
    // TCK-017: BulletHitBotEvent(victimId!=myId) triggers onBulletHit
    // -----------------------------------------------------------------------

    @Test
    @Tag("TR-API-TCK-017")
    void tck017_bulletHitBotEventOther_triggersOnBulletHit() throws InterruptedException {
        var latch = new CountDownLatch(1);

        var bot = new BaseBot(botInfo, java.net.URI.create(MockedServer.SERVER_URL)) {
            @Override
            public void onBulletHit(BulletHitBotEvent event) {
                latch.countDown();
            }
        };
        startAsync(bot);
        awaitGameStarted(bot);
        awaitTick(bot);
        server.sendRawText(buildTickWithEvent(buildBulletHitBotEventJson(99)));
        awaitTurnNumber(bot, 2);
        goAsync(bot);
        server.continueBotIntent();
        awaitBotIntent();

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
    }

    // -----------------------------------------------------------------------
    // JSON builders
    // -----------------------------------------------------------------------

    private static String buildTickWithEvent(String eventJson) {
        return "{\"type\":\"TickEventForBot\",\"roundNumber\":1,\"turnNumber\":2,"
                + "\"botState\":{\"isDroid\":false,\"energy\":100.0,\"x\":100.0,\"y\":100.0,"
                + "\"direction\":0.0,\"gunDirection\":0.0,\"radarDirection\":0.0,\"radarSweep\":0.0,"
                + "\"speed\":0.0,\"turnRate\":0.0,\"gunTurnRate\":0.0,\"radarTurnRate\":0.0,"
                + "\"gunHeat\":0.0,\"enemyCount\":0,\"isDebuggingEnabled\":false},"
                + "\"bulletStates\":[],"
                + "\"events\":[" + eventJson + "]}";
    }

    private static String buildRoundEndedJson(int roundNumber, int turnNumber) {
        return "{\"type\":\"RoundEndedEventForBot\","
                + "\"roundNumber\":" + roundNumber + ","
                + "\"turnNumber\":" + turnNumber + ","
                + "\"results\":{\"rank\":1,\"survival\":0,\"lastSurvivorBonus\":0,"
                + "\"bulletDamage\":0,\"bulletKillBonus\":0,\"ramDamage\":0,\"ramKillBonus\":0,"
                + "\"totalScore\":0,\"firstPlaces\":0,\"secondPlaces\":0,\"thirdPlaces\":0}}";
    }

    private static String buildGameEndedJson(int numberOfRounds) {
        return "{\"type\":\"GameEndedEventForBot\","
                + "\"numberOfRounds\":" + numberOfRounds + ","
                + "\"results\":{\"rank\":1,\"survival\":0,\"lastSurvivorBonus\":0,"
                + "\"bulletDamage\":0,\"bulletKillBonus\":0,\"ramDamage\":0,\"ramKillBonus\":0,"
                + "\"totalScore\":0,\"firstPlaces\":0,\"secondPlaces\":0,\"thirdPlaces\":0}}";
    }

    private static String buildBulletHitBotEventJson(int victimId) {
        return "{\"type\":\"BulletHitBotEvent\",\"turnNumber\":2,\"victimId\":" + victimId + ","
                + "\"bullet\":{\"bulletId\":1,\"ownerId\":2,\"power\":1.0,\"x\":50.0,\"y\":50.0,\"direction\":90.0,\"color\":null},"
                + "\"damage\":4.0,\"energy\":96.0}";
    }
}
