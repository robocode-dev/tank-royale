package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.IBaseBot;
import dev.robocode.tankroyale.botapi.IBot;

/**
 * The Condition class is used for testing if a specific condition is met. For example, program
 * execution can be blocked by using the {@link IBot#waitFor(Condition)} method, which will wait
 * until a condition is met. A condition can also used to trigger a custom event by adding a custom
 * event handler using the method {@link IBaseBot#addCustomEvent(Condition)} that will trigger
 * {@link IBaseBot#onCustomEvent(CustomEvent)} when the condition is fulfilled.
 *
 * <p>Here is an example of how to use the condition:
 *
 * <pre>
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
 * </pre>
 */
public abstract class Condition {
  private final String name;

  /**
   * Constructor for initializing a new instance of the Condition class. With this constructor the
   * condition will be without a name.
   */
  public Condition() {
    this.name = null;
  }

  /**
   * Constructor for initializing a new instance of the Condition class. With this constructor the
   * condition will be given a name of your choice.
   *
   * @param name is the name of the condition used for identifying a specific condition between
   *     multiple conditions with the {@link IBaseBot#onCustomEvent(CustomEvent)} event handler.
   */
  public Condition(String name) {
    this.name = name;
  }

  /**
   * Returns the name of this condition, if a name has been provided for it.
   *
   * @return The name of this condition or {@code null} if no name has been provided for it.
   * @see IBaseBot#onCustomEvent(CustomEvent) 
   */
  public String getName() {
    return name;
  }

  /**
   * Overriding this test method is the purpose of a Condition. The game will call your test()
   * function, and take action if it returns {@code true}.
   *
   * @return {@code true} if the condition is met; {@code false} otherwise.
   */
  public abstract boolean test();
}
