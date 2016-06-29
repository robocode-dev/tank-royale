package net.robocode2.server;

import net.robocode2.json_schema.BotHandshake;
import net.robocode2.json_schema.ObserverHandshake;

public interface ConnectionListener {

	void onBotJoined(BotHandshake botHandshake);

	void onObserverJoined(ObserverHandshake observerHandshake);

	void onBotLeft(BotHandshake botHandshake);

	void onObserverLeft(ObserverHandshake observerHandshake);
}
