using Robocode.TankRoyale.BotApi;

namespace Robocode.TankRoyale.Sample.Bots
{
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
}