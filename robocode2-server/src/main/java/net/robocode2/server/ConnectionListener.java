package net.robocode2.server;

import net.robocode2.json_schema.BotHandshake;
import net.robocode2.json_schema.ObserverHandshake;

public interface ConnectionListener {

	public void onBotJoined(BotHandshake botHandshake);

	public void onBotLeft(BotHandshake botHandshake);

	public void onObserverJoined(ObserverHandshake observerHandshake);

	public void onObserverLeft(ObserverHandshake observerHandshake);
}
