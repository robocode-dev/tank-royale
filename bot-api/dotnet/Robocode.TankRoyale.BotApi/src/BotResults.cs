namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Represents individual bot results.
/// </summary>
public sealed class BotResults
{
    /// <summary>
    /// The rank/placement of the bot, where 1 means 1st place, 4 means 4th place etc.
    /// </summary>
    /// <value>The rank of the bot for this battle.</value>
    public int Rank { get; }

    /// <summary>
    /// The accumulated survival score. Every bot still alive score 50 points every time another
    /// bot is defeated.
    /// </summary>
    /// <value>The survival score.</value>
    public double Survival { get; }

    /// <summary>
    /// The last survivor score. The last bot alive scores 10 points or each bot that has been
    /// defeated.
    /// </summary>
    /// <value>The last survivor score.</value>
    public double LastSurvivorBonus { get; }

    /// <summary>
    /// The bullet damage score. A bot score 1 point for each point of damage they do to other
    /// bots.
    /// </summary>
    /// <value>The bullet damage score.</value>
    public double BulletDamage { get; }

    /// <summary>
    /// The bullet kill-bonus. When a bot kills another bot, it scores an additional 20% points of
    /// the total damage it did to that bot.
    /// </summary>
    /// <value>The bullet kill-bonus.</value>
    public double BulletKillBonus { get; }

    /// <summary>
    /// The ram damage score. Bots score 2 points for each point of damage inflicted by ramming an
    /// enemy bot. Ramming is the act deliberately driving forward (not backward) and hitting
    /// another bot.
    /// </summary>
    /// <value>The ram damage score.</value>
    public double RamDamage { get; }

    /// <summary>
    /// The ram kill-bonus. When a bot kills another bot due to ramming, it scores an additional
    /// 30% of the total ramming damage it did to that bot.
    /// </summary>
    /// <value>The ram kill-bonus.</value>
    public double RamKillBonus { get; }

    /// <summary>
    /// The total score is the sum of all scores and determines the ranking.
    /// </summary>
    /// <value>The total score.</value>
    public double TotalScore { get; }

    /// <summary>
    /// The number of 1st places for the bot.
    /// </summary>
    /// <value>The number of 1st places.</value>
    public double FirstPlaces { get; }

    /// <summary>
    /// The number of 2nd places for the bot.
    /// </summary>
    /// <value>The number of 2nd places.</value>
    public double SecondPlaces { get; }

    /// <summary>
    /// The number of 3rd places for the bot.
    /// </summary>
    /// <value>The number of 3rd places.</value>
    public double ThirdPlaces { get; }

    /// <summary>
    /// Initializes a new instance of the BotInfo class.
    /// </summary>
    /// <param name="rank">The rank/placement of the bot.</param>
    /// <param name="survival">The survival score.</param>
    /// <param name="lastSurvivorBonus">The last survivor score.</param>
    /// <param name="bulletDamage">The bullet damage score.</param>
    /// <param name="bulletKillBonus">The bullet kill bonus.</param>
    /// <param name="ramDamage">The ram damage score.</param>
    /// <param name="ramKillBonus">The ram kill bonus.</param>
    /// <param name="totalScore">The total score.</param>
    /// <param name="firstPlaces">The number of 1st places.</param>
    /// <param name="secondPlaces">The number of 2nd places.</param>
    /// <param name="thirdPlaces">The number of 3rd places.</param>
    public BotResults(int rank, double survival, double lastSurvivorBonus, double bulletDamage,
        double bulletKillBonus, double ramDamage, double ramKillBonus, double totalScore, double firstPlaces,
        double secondPlaces, double thirdPlaces)
    {
        Rank = rank;
        Survival = survival;
        LastSurvivorBonus = lastSurvivorBonus;
        BulletDamage = bulletDamage;
        BulletKillBonus = bulletKillBonus;
        RamDamage = ramDamage;
        RamKillBonus = ramKillBonus;
        TotalScore = totalScore;
        FirstPlaces = firstPlaces;
        SecondPlaces = secondPlaces;
        ThirdPlaces = thirdPlaces;
    }
}