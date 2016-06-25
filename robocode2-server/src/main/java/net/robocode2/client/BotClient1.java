package net.robocode2.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;

import net.robocode2.json_schema.BotHandshake;

public class BotClient1 extends WebSocketClient {

    public BotClient1(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public BotClient1(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("onOpen()");
        
        System.out.println("Sending ClientHandshake");
        BotHandshake bh = new BotHandshake();
        bh.setMessageType(BotHandshake.MessageType.BOT_HANDSHAKE);
        bh.setName("Bot name");
        bh.setVersion("0.1");
        bh.setAuthor("Author name");
        bh.setCountryCode("DK");
        bh.setGameTypes(Arrays.asList("melee", "1v1"));
        bh.setProgrammingLanguage("Java");
        
		String msg = new Gson().toJson(bh);
		
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
        WebSocketClient client = new BotClient1(new URI("ws://localhost:50000"), new Draft_10());
        client.connect();
    }
}