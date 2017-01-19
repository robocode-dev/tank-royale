import static jsweet.dom.Globals.console;
import static jsweet.dom.Globals.window;

import java.util.Map;
import java.util.Map.Entry;

import json_schema.ObserverHandshake;
import jsweet.dom.Event;
import jsweet.dom.WebSocket;

public class ObserverClient1 {

	public static void main(String[] args) {
		window.onload = e -> {
			return new ObserverClient1();
		};
	}

	private WebSocket ws;

	public ObserverClient1() {
		ws = new jsweet.dom.WebSocket("ws://localhost:50000");

		ws.onopen = e -> {
			return onOpen(e);
		};
		ws.onclose = e -> {
			return onClose(e);
		};
		ws.onmessage = e -> {
			return onMessage(e);
		};
		ws.onerror = e -> {
			return onError(e);
		};
	}

	private Void onOpen(Event e) {
		console.info("onOpen: " + e.toString());

		ObserverHandshake handshake = new ObserverHandshake();
		handshake.setName("Observer name");
		handshake.setVersion("0.1");
		handshake.setAuthor("Author name");

		ws.send(handshake.toJsonString());

		return null;
	}

	private Void onClose(Event e) {
		console.info("onClose: " + e.toString());
		return null;
	}

	private Void onMessage(Event e) {
		console.info("onMessage: " + e.toString());
		return null;
	}

	private Void onError(Event e) {
		console.error("onError: " + e.toString());
		return null;
	}

	private static jsweet.lang.Object toJsObject(Map<String, String> map) {
		jsweet.lang.Object jsObject = new jsweet.lang.Object();

		// Put the keys and values from the map into the object
		for (Entry<String, String> keyVal : map.entrySet()) {
			jsObject.$set(keyVal.getKey(), keyVal.getValue());
		}
		return jsObject;
	}
}
