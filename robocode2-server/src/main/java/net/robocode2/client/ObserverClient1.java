package net.robocode2.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;

import net.robocode2.json_schema.ObserverHandshake;

public class ObserverClient1 extends WebSocketClient {

    public ObserverClient1(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public ObserverClient1(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("onOpen()");
        
        System.out.println("Sending ClientHandshake");
        ObserverHandshake oh = new ObserverHandshake();
        oh.setMessageType(ObserverHandshake.MessageType.OBSERVER_HANDSHAKE);
        oh.setName("Observer name");
        oh.setVersion("0.1");
        oh.setAuthor("Author name");
        
		String msg = new Gson().toJson(oh);

		send(msg);
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
        WebSocketClient client = new ObserverClient1(new URI("ws://localhost:50000"), new Draft_10());
        client.connect();
    }
}