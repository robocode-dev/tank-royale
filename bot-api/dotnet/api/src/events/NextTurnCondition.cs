namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Prebuilt condition that can be used for waiting for the next turn.
/// See <see cref="Condition"/> and <see cref="IBot.WaitFor(Condition)"/> for more information.
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
///       SetFire(1.0);
///       ...
///       WaitFor(new TurnCompleteCondition(this)); // wait for the next turn before continuing
///     }
///   }
/// }
/// </code>
/// </example>
public sealed class NextTurnCondition : Condition
{
    private readonly IBaseBot baseBot;
    private readonly int creationTurnNumber;

    /// <summary>
    /// Constructor for initializing a new instance of the NextTurnCondition class.
    /// </summary>
    /// <param name="baseBot">baseBot is your bot instance, typically <c>this</c> instance, used
    /// for determining the current turn of the battle with the <see cref="Test"/> method</param>
    public NextTurnCondition(IBaseBot baseBot) : base("NextTurnCondition")
    {
        this.baseBot = baseBot;
        creationTurnNumber = baseBot.TurnNumber;
    }

    /// <summary>
    /// This method tests if the turn number has changed since we created this condition.
    /// </summary>
    /// <returns><c>true</c> if the current turn number is greater than the initial turn number,
    /// when this condition was created; <code>false</code> otherwise.</returns>
    public override bool Test()
    {
        return baseBot.TurnNumber > creationTurnNumber;
    }
}