package net.robocode2.model.events;

public final class BotDeathEvent implements Event {

	private final int botId;

	public BotDeathEvent(int botId) {
		this.botId = botId;
	}

	public int getBotId() {
		return botId;
	}
}