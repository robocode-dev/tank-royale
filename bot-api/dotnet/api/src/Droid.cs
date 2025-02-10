namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Droid interface to turn your bot into a droid bot, which is used as a specialized team bot.
/// A droid has 20 additional energy points (120 in total from the start), but has no scanner!
///
/// Because the droid has no scanner, it is 100% dependent on other team members to perform the scanning on its behalf
/// and communicate crucial information to the bot, e.g., the coordinates of target enemies.
///
/// A team of droids plus at least one non-droid team robot can have an edge over another team without droids (and the
/// same number of robots) due to the additional 20 energy points.
/// </summary>
public interface Droid
{
}