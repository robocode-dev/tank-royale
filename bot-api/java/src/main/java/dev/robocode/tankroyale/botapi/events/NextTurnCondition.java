package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.IBaseBot;
import dev.robocode.tankroyale.botapi.IBot;

/**
 * Prebuilt condition that can be used for waiting for the next turn.
 * See {@link Condition} and {@link IBot#waitFor} for more information.
 *
 * <p>Here is an example of how to use this condition:
 * <script src="../../../../../prism.js"></script>
 * <pre><code class="language-java">
 *  public class MyBot extends Bot {
 *    public void run() {
 *      while (isRunning()) {
 *        ...
 *        setFire(1.0);
 *        ...
 *        waitFor(new NextTurnCondition(this)); // wait for the next turn before continuing
 *      }
 *    }
 * }
 * </code></pre>
 */
@SuppressWarnings("unused")
public final class NextTurnCondition extends Condition {

    private final IBaseBot baseBot;
    private final int creationTurnNumber;

    /**
     * Constructor for initializing a new instance of the NextTurnCondition class.
     *
     * @param baseBot is your bot instance, typically {@code this} instance, used for determining
     *                the current turn of the battle with the {@link #test} method.
     */
    public NextTurnCondition(IBaseBot baseBot) {
        super("NextTurnCondition");

        this.baseBot = baseBot;
        this.creationTurnNumber = baseBot.getTurnNumber();
    }

    /**
     * This method tests if the turn number has changed since we created this condition.
     *
     * @return {@code true} if the current turn number is greater than the initial turn number,
     * when this condition was created; {@code false} otherwise.
     */
    @Override
    public boolean test() {
        return baseBot.getTurnNumber() > creationTurnNumber;
    }
}
