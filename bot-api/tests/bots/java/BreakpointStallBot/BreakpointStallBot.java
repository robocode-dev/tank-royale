import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

/**
 * A minimal bot that simulates a JVM completely frozen at a debugger breakpoint.
 *
 * It connects to the server, completes the bot handshake, sends BotReady when the game starts,
 * but then:
 *   - Never sends any BotIntent messages (no turn responses)
 *   - Does NOT respond to WebSocket ping frames with a pong
 *
 * This mirrors the real-world failure mode: when a developer's JVM is suspended by a debugger,
 * ALL threads (including the Java HTTP client's selector thread) are frozen, so the bot can
 * neither send intents nor reply to pings.
 *
 * Used by the slow integration test to verify that the server's connection-lost detection is
 * disabled for bots in breakpoint pause (issue #206).
 */
public class BreakpointStallBot {

    public static void main(String[] args) throws Exception {
        String serverUrl = System.getenv("SERVER_URL");
        if (serverUrl == null || serverUrl.isBlank()) serverUrl = "ws://localhost:7654";

        CountDownLatch closeLatch = new CountDownLatch(1);

        WebSocket.Listener listener = new WebSocket.Listener() {
            private final StringBuilder buf = new StringBuilder();

            @Override
            public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                buf.append(data);
                if (last) {
                    handleMessage(ws, buf.toString());
                    buf.setLength(0);
                }
                return WebSocket.Listener.super.onText(ws, data, last);
            }

            /**
             * Deliberately does NOT send a pong in response to the server's ping.
             * This simulates a JVM suspended by a debugger: the selector thread is frozen,
             * so no pong can be sent. We still call request(1) so the connection is not
             * stalled at the Java layer — only the pong is withheld.
             */
            @Override
            public CompletionStage<?> onPing(WebSocket ws, ByteBuffer message) {
                ws.request(1);
                return null;
            }

            @Override
            public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
                closeLatch.countDown();
                return null;
            }

            @Override
            public void onError(WebSocket ws, Throwable error) {
                closeLatch.countDown();
            }
        };

        HttpClient.newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(URI.create(serverUrl), listener)
            .join();

        closeLatch.await();
    }

    private static void handleMessage(WebSocket ws, String json) {
        try {
            JsonObject msg = JsonParser.parseString(json).getAsJsonObject();
            String type = msg.get("type").getAsString();
            switch (type) {
                case "ServerHandshake": {
                    String sessionId = msg.get("sessionId").getAsString();
                    String handshake =
                        "{\"type\":\"BotHandshake\"" +
                        ",\"sessionId\":\"" + sessionId + "\"" +
                        ",\"name\":\"BreakpointStallBot\"" +
                        ",\"version\":\"1.0\"" +
                        ",\"authors\":[\"Test\"]" +
                        ",\"gameTypes\":[\"1v1\",\"classic\",\"melee\"]" +
                        "}";
                    ws.sendText(handshake, true);
                    break;
                }
                case "GameStartedEventForBot":
                    // Signal readiness so the game can start, then go silent.
                    ws.sendText("{\"type\":\"BotReady\"}", true);
                    break;
                // All other messages (ticks, round/game events) are intentionally ignored.
                // No BotIntent is ever sent — the server will pause in breakpoint mode.
            }
        } catch (Exception ignored) {
            // Ignore parse errors; the bot stays connected regardless
        }
    }
}
