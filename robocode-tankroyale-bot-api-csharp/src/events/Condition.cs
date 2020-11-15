namespace Robocode.TankRoyale.BotApi.Events
{
  /// <summary>
  /// The Condition class is used for testing if a specific condition is met. For example, program
  /// execution can be blocked by using the <see cref="IBot.WaitFor(Condition)"/> method, which will wait
  /// until a condition is met. A condition can also used to trigger a custom event by adding a custom
  /// event handler using the method <see cref="IBaseBot.AddCustomEvent(Condition)"/> that will trigger
  /// <see cref="IBaseBot.OnCustomEvent(Condition)"/> when the condition is fulfilled.
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
    /// <seealso cref="IBaseBot.OnCustomEvent(Condition)"/>
    public string Name { get; }

    /// <summary>
    /// Constructor for initializing a new instance of the Condition class. With this constructor the condition will be
    /// without a name.
    /// </summary>
    public Condition()
    {
      this.Name = null;
    }

    /// <summary>
    /// Constructor for initializing a new instance of the Condition class. With this constructor the condition will be
    /// given a name of your choice.
    /// </summary>
    /// <param name="name">Is the name of the condition used for identifying a specific condition between multiple
    /// conditions with the <see cref="IBaseBot.OnCustomEvent(Condition)"/> event handler.</param>
    public Condition(string name)
    {
      this.Name = name;
    }

    /// <summary>
    /// Overriding the this test method is the purpose of a Condition. The game will call your <c>test()</c> function,
    /// and take action if it returns <c>true</c>.
    /// </summary>
    public abstract bool Test();
  }
}