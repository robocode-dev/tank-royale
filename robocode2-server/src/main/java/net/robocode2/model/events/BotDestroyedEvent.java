package net.robocode2.model.events;

public final class BotDestroyedEvent implements Event {

	private final int botId;

	public BotDestroyedEvent(int botId) {
		this.botId = botId;
	}

	public double getBotId() {
		return botId;
	}
}