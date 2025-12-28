package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.ConnectionErrorEvent;
import dev.robocode.tankroyale.botapi.events.DisconnectedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("BOT")
@Tag("TR-API-BOT-004")
class ErrorHandlingTest extends AbstractBotTest {

    @Test
    @DisplayName("TR-API-BOT-004 Error handling: server disconnect during handshake")
    void test_TR_API_BOT_004_server_disconnect_during_handshake() {
        AtomicBoolean disconnectedCalled = new AtomicBoolean(false);
        AtomicBoolean connectionErrorCalled = new AtomicBoolean(false);

        var bot = new TestBot() {
            @Override
            public void onDisconnected(DisconnectedEvent e) {
                disconnectedCalled.set(true);
            }

            @Override
            public void onConnectionError(ConnectionErrorEvent e) {
                connectionErrorCalled.set(true);
            }
        };

        Thread thread = new Thread(bot::start);
        thread.start();

        assertThat(server.awaitConnection(1000)).isTrue();

        server.closeConnections();

        awaitCondition(() -> disconnectedCalled.get() || connectionErrorCalled.get(), 1000);

        assertThat(disconnectedCalled.get() || connectionErrorCalled.get()).isTrue();
    }

    @Test
    @DisplayName("TR-API-BOT-004 Error handling: server disconnect during game")
    void test_TR_API_BOT_004_server_disconnect_during_game() {
        AtomicBoolean disconnectedCalled = new AtomicBoolean(false);
        AtomicBoolean connectionErrorCalled = new AtomicBoolean(false);

        var bot = new TestBot() {
            @Override
            public void onDisconnected(DisconnectedEvent e) {
                disconnectedCalled.set(true);
            }

            @Override
            public void onConnectionError(ConnectionErrorEvent e) {
                connectionErrorCalled.set(true);
            }
        };

        Thread thread = new Thread(bot::start);
        thread.start();

        awaitGameStarted(bot);

        server.closeConnections();

        awaitCondition(() -> disconnectedCalled.get() || connectionErrorCalled.get(), 1000);

        assertThat(disconnectedCalled.get() || connectionErrorCalled.get()).isTrue();

        assertThat(awaitCondition(() -> !thread.isAlive(), 2000)).isTrue();
    }

    @Test
    @DisplayName("TR-API-BOT-004 Error handling: protocol error (invalid JSON)")
    void test_TR_API_BOT_004_protocol_error_invalid_json() {
        AtomicBoolean connectionErrorCalled = new AtomicBoolean(false);

        var bot = new TestBot() {
            @Override
            public void onConnectionError(ConnectionErrorEvent e) {
                connectionErrorCalled.set(true);
            }
        };

        Thread thread = new Thread(bot::start);
        thread.start();

        awaitGameStarted(bot);

        server.sendRawText("invalid json {");

        awaitCondition(connectionErrorCalled::get, 1000);

        assertThat(connectionErrorCalled.get()).isTrue();
    }

    @Test
    @DisplayName("TR-API-BOT-004 Error handling: protocol error (unsupported message type)")
    void test_TR_API_BOT_004_protocol_error_unsupported_type() {
        AtomicBoolean connectionErrorCalled = new AtomicBoolean(false);

        var bot = new TestBot() {
            @Override
            public void onConnectionError(ConnectionErrorEvent e) {
                connectionErrorCalled.set(true);
            }
        };

        Thread thread = new Thread(bot::start);
        thread.start();

        awaitGameStarted(bot);

        server.sendRawText("{\"type\": \"UNKNOWN_TYPE\"}");

        awaitCondition(connectionErrorCalled::get, 1000);

        assertThat(connectionErrorCalled.get()).isTrue();
    }
}
