package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.Bot;

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
