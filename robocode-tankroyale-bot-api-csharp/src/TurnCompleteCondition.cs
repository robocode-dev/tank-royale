using Robocode.TankRoyale.BotApi;

public class TurnCompleteCondition : Condition
{
  private readonly Bot bot;

  public TurnCompleteCondition(Bot bot)
  {
    this.bot = bot;
  }

  public override bool Test()
  {
    return bot.TurnRemaining == 0;
  }
}
