package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.IBaseBot;
import dev.robocode.tankroyale.botapi.IBot;

import java.util.concurrent.Callable;

/**
 * The Condition class is used for testing if a specific condition is met. For example, program
 * execution can be blocked by using the {@link IBot#waitFor} method, which will wait until a
 * condition is met. A condition can also be used to trigger a custom event by adding a custom
 * event handler using the method {@link IBaseBot#addCustomEvent} that will trigger
 * {@link IBaseBot#onCustomEvent} when the condition is fulfilled.
 *
 * <p>Here is an example of how to use the condition:
 * <script src="../../../../../prism.js"></script>
 * <pre><code class="language-java">
 *  public class MyBot extends Bot {
 *    public void run() {
 *      while (isRunning()) {
 *        ...
 *        setTurnRight(90);
 *        waitFor(new TurnCompleteCondition(this));
 *        ...
 *      }
 *    }
 *
 *    public class TurnCompleteCondition extends Condition {
 *      private final Bot bot;
 *
 *      public TurnCompleteCondition(Bot bot) {
 *        this.bot = bot;
 *      }
 *
 *      public boolean test() {
 *        return bot.getTurnRemaining() == 0;
 *      }
 *    }
 *  }
 * </code></pre>
 *
 * <p>Here is another example using the same condition using a lambda expression instead of a (reusable) class:
 *
 * <pre><code class="language-java">
 *  public class MyBot extends Bot {
 *    public void run() {
 *      while (isRunning()) {
 *        ...
 *        setTurnRight(90);
 *        waitFor(new Condition(() -&gt; getTurnRemaining() == 0));
 *        ...
 *      }
 *    }
 * </code></pre>
 */
public class Condition {

    // Optional name of the condition.
    private final String name;

    // Optional Callable
    private final Callable<Boolean> callable;

    /**
     * Constructor for initializing a new instance of the Condition class.
     */
    public Condition() {
        this(null, null);
    }

    /**
     * Constructor for initializing a new instance of the Condition class.
     *
     * @param name is the name of the condition used for identifying a specific condition between
     *             multiple conditions with the {@link IBaseBot#onCustomEvent} event handler.
     */
    public Condition(String name) {
        this(name, null);
    }

    /**
     * Constructor for initializing a new instance of the Condition class.
     *
     * @param callable is a callable containing a method returning {@code true}, if some condition is met,
     *                 or {@code false} when the condition is not met.
     */
    public Condition(Callable<Boolean> callable) {
        this(null, callable);
    }

    /**
     * Constructor for initializing a new instance of the Condition class.
     *
     * @param name     is the name of the condition used for identifying a specific condition between
     *                 multiple conditions with the {@link IBaseBot#onCustomEvent} event handler.
     * @param callable is a callable containing a method returning {@code true}, if some condition is met,
     *                 or {@code false} when the condition is not met.
     */
    public Condition(String name, Callable<Boolean> callable) {
        this.callable = callable;
        this.name = name;
    }

    /**
     * Returns the name of this condition, if a name has been provided for it.
     *
     * @return The name of this condition or {@code null} if no name has been provided for it.
     * @see IBaseBot#onCustomEvent
     */
    public String getName() {
        return name;
    }

    /**
     * You can choose to override this method to let the game use it for testing your condition each turn.
     * Alternatively, you can use the one of the constructors that take a {@link Callable} instead.
     *
     * @return {@code true} if the condition is met; {@code false} otherwise.
     */
    public boolean test() {
        if (callable != null) {
            try {
                return callable.call();
            } catch (Exception ignore) {
                return false;
            }
        }
        return false;
    }
}
