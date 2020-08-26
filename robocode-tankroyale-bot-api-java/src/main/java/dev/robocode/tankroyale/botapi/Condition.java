package dev.robocode.tankroyale.botapi;

/**
 * The Condition class is used for stopping program execution temporarily and wait for a specific
 * condition to be met before continuing the program execution. Program execution will be stopped by
 * the {@link Bot#waitFor(Condition)} method, which will wait until the condition is met. When the
 * condition is fulfilled, the {@link Bot#onCondition(Condition)} event handler will be triggered.
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
   *     multiple conditions with the {@link Bot#onCondition(Condition)} event handler.
   */
  public Condition(String name) {
    this.name = name;
  }

  /**
   * Returns the name of this condition, if a name has been provided for it.
   *
   * @return The name of this condition or {@code null} if no name has been provided for it.
   * @see Bot#onCondition(Condition)
   */
  public String getName() {
    return name;
  }

  /**
   * Overriding this test method is the purpose of a Condition. The game will call your test()
   * function, and take action if it returns {@code true}.
   */
  public abstract boolean test();
}
