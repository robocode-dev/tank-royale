package dev.robocode.tankroyale.bot;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.Condition;
import dev.robocode.tankroyale.botapi.IBot;

public class TurnCompleteCondition extends Condition {

    private final Bot bot;

    public TurnCompleteCondition(Bot bot) {
        this.bot = bot;
    }

    @Override
    public boolean test() {
        return bot.getTurnRemaining() == 0;
    }
}
