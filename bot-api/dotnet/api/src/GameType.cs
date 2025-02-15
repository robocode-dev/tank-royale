namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Predefined game types.
/// These game types are described <a href="https://robocode-dev.github.io/tank-royale/articles/game_types.html">here</a>.
/// </summary>
public static class GameType
{
    /// <summary>
    /// Classic (standard) battle with a minimum of 2 bots battling each other on an arena size of 800 x 600 units.
    /// </summary>
    public const string Classic = "classic";

    /// <summary>
    /// Melee battle with a minimum of 10 bots battling each other on an arena of 1000 x 1000 units.
    /// </summary>
    public const string Melee = "melee";

    /// <summary>
    /// One versus one (1-vs-1) battle between exactly two bots alone on an arena of 1000 x 1000 units.
    /// </summary>
    public const string OneVsOne = "1v1";
}