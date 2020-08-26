package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Condition;
import dev.robocode.tankroyale.botapi.IBot;

public class TurnCompleteCondition extends Condition {

    private final IBot bot;

    public TurnCompleteCondition(IBot bot) {
        this.bot = bot;
    }

    @Override
    public boolean test() {
        return bot.getTurnRemaining() == 0;
    }
}
