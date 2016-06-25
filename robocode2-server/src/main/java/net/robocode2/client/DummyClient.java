package net.robocode2.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

public class DummyClient extends WebSocketClient {

    public DummyClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public DummyClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("onOpen()");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("onClose(), code: " + code + ", reason: " + reason);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("onMessage(): " + message);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("onError():" + ex);
    }

    public static void main(String[] args) throws URISyntaxException {      
        WebSocketClient client = new DummyClient(new URI("ws://localhost:50000"), new Draft_10());
        client.connect();
    }
}