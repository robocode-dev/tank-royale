using System;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// The Condition class is used for testing if a specific condition is met. For example, program
/// execution can be blocked by using the <see cref="IBot.WaitFor(Condition)"/> method, which will wait
/// until a condition is met. A condition can also used to trigger a custom event by adding a custom
/// event handler using the method <see cref="IBaseBot.AddCustomEvent(Condition)"/> that will trigger
/// <see cref="IBaseBot.OnCustomEvent(CustomEvent)"/> when the condition is fulfilled.
/// </summary>
///
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
///
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
///       WaitFor(new Condition(() => TurnRemaining == 0));
///       ...
///     }
///   }
/// }
/// </code>
/// </example>
public class Condition
{
    /// <summary>
    /// Returns the name of this condition, if a name has been provided for it.
    /// </summary>
    /// <value>The name of this condition or <c>null</c> if no name has been provided for it.</value>
    /// <seealso cref="IBaseBot.OnCustomEvent(CustomEvent)"/>
    public string Name { get; }

    private readonly Func<bool> testFunc;

    /// <summary>
    /// Constructor for initializing a new instance of the Condition class.
    /// </summary>
    public Condition()
    {
        Name = null;
        testFunc = null;
    }

    /// <summary>
    /// Constructor for initializing a new instance of the Condition class.
    /// </summary>
    /// <param name="name">Is the name of the condition used for identifying a specific condition between multiple
    /// conditions with the <see cref="IBaseBot.OnCustomEvent(CustomEvent)"/> event handler.</param>
    public Condition(string name)
    {
        Name = name;
        testFunc = null;
    }

    /// <summary>
    /// Constructor for initializing a new instance of the Condition class.
    /// </summary>
    /// <param name="testFunc">Is a function/delegate containing a method returning <c>true</c>, if some condition is met,
    /// or <c>false</c> when the condition is not met.</param>
    public Condition(Func<bool> testFunc)
    {
        Name = null;
        this.testFunc = testFunc;
    }

    /// <summary>
    /// Constructor for initializing a new instance of the Condition class.
    /// </summary>
    /// <param name="name">Is the name of the condition used for identifying a specific condition between multiple
    /// conditions with the <see cref="IBaseBot.OnCustomEvent(CustomEvent)"/> event handler.</param>
    /// <param name="testFunc">Is a function/delegate containing a method returning <c>true</c>, if some condition is met,
    /// or <c>false</c> when the condition is not met.</param>
    public Condition(string name, Func<bool> testFunc)
    {
        Name = name;
        this.testFunc = testFunc;
    }

    /// <summary>
    /// You can choose to override this method to let the game use it for testing your condition each turn.
    /// Alternatively, you can use the one of the constructors that take a <see cref="Func{TResult}"/> instead.
    /// </summary>
    /// <return><c>true</c> if the condition is met; <c>false</c> otherwise.</return>
    public virtual bool Test()
    {
        if (testFunc == null)
            return false;
        try
        {
            return testFunc.Invoke();
        }
        catch (Exception)
        {
            return false;
        }
    }
}