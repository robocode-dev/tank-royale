namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// The Condition class is used for stopping program execution temporarily and wait for a specific condition to be
  /// met before continuing the program execution. Program execution will be stopped by the
  /// <see cref="IBot.WaitFor(Condition)"/> method, which will wait until the condition is met. When the
  /// condition is fulfilled, the <see cref="IBaseBot.OnCondition(Condition)"/> event handler will be triggered.
  /// </summary>
  /// <example>
  /// <code>
  /// public class MyBot : Bot
  /// {
  ///   public void Run()
  ///   {
  ///     while (IsRunning)
  ///     {
  ///       ...
  ///       SetTurnRight(90);
  ///       WaitFor(new TurnCompleteCondition(this));
  ///       ...
  ///     }
  ///   }
  ///
  ///   public class TurnCompleteCondition : Condition
  ///   {
  ///     private readonly Bot bot;
  ///
  ///     public TurnCompleteCondition(Bot bot)
  ///     {
  ///       this.bot = bot;
  ///     }
  ///
  ///     public override bool Test()
  ///     {
  ///       return bot.TurnRemaining == 0;
  ///     }
  ///   }
  /// }
  /// </code>
  /// </example>
  public abstract class Condition
  {
    /// <summary>
    /// Returns the name of this condition, if a name has been provided for it.
    /// </summary>
    /// <value>The name of this condition or <c>null</c> if no name has been provided for it.</value>
    /// <seealso cref="IBaseBot.OnCondition(Condition)"/>
    public string name { get; }

    /// <summary>
    /// Constructor for initializing a new instance of the Condition class. With this constructor the condition will be
    /// without a name.
    /// </summary>
    public Condition()
    {
      this.name = null;
    }

    /// <summary>
    /// Constructor for initializing a new instance of the Condition class. With this constructor the condition will be
    /// given a name of your choice.
    /// </summary>
    /// <param name="name">Is the name of the condition used for identifying a specific condition between multiple
    /// conditions with the <see cref="IBaseBot.OnCondition(Condition)"/> event handler.</param>
    public Condition(string name)
    {
      this.name = name;
    }

    /// <summary>
    /// Overriding the this test method is the purpose of a Condition. The game will call your <c>test()</c> function,
    /// and take action if it returns <c>true</c>.
    /// </summary>
    public abstract bool Test();
  }
}