namespace Robocode.TankRoyale
{
  /// <summary>
  /// Predefined game types.
  /// </summary>
  public sealed class GameType
  {
    // Hide constuctor.
    private GameType() { }

    /// <summary>Melee battle with every bot against every other bot.</summary>
    public const string Melee = "melee";

    /// <summary>One versus one (1-vs-1) battle.</summary>
    public const string OneVsOne = "1v1";
  }
}