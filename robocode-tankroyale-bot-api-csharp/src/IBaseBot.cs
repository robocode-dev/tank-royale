using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Interface containing the core API for a bot.
  /// </summary>
  public interface IBaseBot
  {
    /// <summary>
    /// The bounding circle of a bot is a circle going from the center of the bot with a radius so
    /// that the circle covers most of the bot. The bounding circle is used for determining when a
    /// bot is hit by a bullet.
    ///
    /// A bot gets hit by a bullet when the bullet gets inside the bounding circle, i.e. the
    /// distance between the bullet and the center of the bounding circle is less than the radius
    /// of the bounding circle.
    /// </summary>
    /// <value>The radius of the bounding circle of the bot, which is a constant of 18 units.</value>
    int BoundingCircleRadius => 18;

    /// <summary>
    /// The radar is used for scanning the battlefield for opponent bots. The shape of the scan
    /// beam of the radar is a circle arc ("pizza slice") starting from the center of the bot.
    /// Opponent bots that get inside the scan arc will be detected by the radar.
    ///
    /// The radius of the arc is a constant of 1200 units. This means that that the radar will not
    /// be able to detect bots that are more than 1200 units away from the bot.
    ///
    /// The radar needs to be turned (left or right) to scan opponent bots. So make sure the radar
    /// is always turned. The more the radar is turned, the larger the area of the scan arc
    /// becomes, and the bigger the chance is that the radar detects an opponent. If the radar is
    /// not turning, the scan arc becomes a thin line, unable to scan and detect anything.
    /// </summary>
    /// <value>The radius of the radar's scan beam, which is a constant of 1200 units.</value>
    int ScanRadius => 1200;

    /// <summary>
    /// This is the max. possible turn rate of the bot. Note that the speed of the bot has a direct
    /// impact on the turn rate. The faster the speed the less turn rate.
    ///
    /// The formula for the max. possible turn rate at a given speed is:
    /// MaxTurnRate - 0.75 x abs(speed).
    /// Hence, the turn rate is at max. 10 degrees/turn when the speed is zero, and down to only 4
    /// degrees per turn when the bot is at max speed (which is 8 units per turn).
    /// </summary>
    /// <value>The maximum possible driving turn rate, which is max. 10 degrees per turn.</value>
    int MaxTurnRate => 10;

    /// <summary>
    /// The maximum gun turn rate, which is a constant of 20 degrees per turn.
    /// </summary>
    int MaxGunTurnRate => 20;

    /// <summary>
    /// The maximum radar turn rate, which is a constant of 45 degrees per turn.
    /// </summary>
    int MaxRadarTurnRate => 45;

    /// <summary>
    /// The maximum absolute speed, which is 8 units per turn.
    /// </summary>
    int MaxSpeed => 8;

    /// <summary>
    /// The maximum forward speed, which is 8 units per turn.
    /// </summary>
    int MaxForwardSpeed => 8;

    /// <summary>
    /// The maximum backward speed, which is -8 units per turn.
    /// </summary>
    int MaxBackwardSpeed => -8;

    /// <summary>
    /// The gun will not fire with a power that is less than the minimum firepower, which is 0.1.
    /// </summary>
    /// <value>The minimum firepower, which is 0.1.</value>
    double MinFirepower => 0.1;

    /// <summary>
    /// The gun will fire up to this power only if the firepower is set to a higher value.
    /// </summary>
    /// <value>The maximum firepower, which is 3.</value>
    double MaxFirepower => 3;

    /// <summary>
    /// The minimum bullet speed is the slowest possible speed that a bullet can travel and is
    /// defined by the maximum firepower. Min. bullet speed = 20 - 3 x max. firepower, i.e.
    /// 20 - 3 x 3 = 11. The more power, the slower the bullet speed will be.
    /// </summary>
    /// <value>The minimum bullet speed is 11 units per turn.</value>
    double MinBulletSpeed => 20 - 3 * MaxFirepower;

    /// <summary>
    /// The maximum bullet speed is the fastest possible speed that a bullet can travel and is
    /// defined by the minimum firepower. Max. bullet speed = 20 - 3 x min. firepower, i.e.
    /// 20 - 3 x 0.1 = 19.7. The lesser power, the faster the bullet speed will be.
    /// </summary>
    /// <value>The maximum bullet speed is 19.7 units per turn.</value>
    double MaxBulletSpeed => 20 - 3 * MinFirepower;

    /// <summary>
    /// Acceleration is the increase in speed per turn, which adds 1 unit to the speed per turn
    /// when the bot is increasing its speed moving forward.
    /// </summary>
    /// <value>The acceleration is 1 additional unit per turn.</value>
    int Acceleration => 1;

    /// <summary>
    /// Deceleration is the decrease in speed per turn, which subtracts 2 units to the speed
    /// per turn when the bot is decreasing its speed moving backward. This means that a bot
    /// is faster at braking than accelerating forward.
    /// </summary>
    /// <value>The deceleration is 2 units less per turn.</value>
    int Deceleration => -2;

    /// <summary>
    /// The method used to start running the bot. You should call this method from the main
    /// method or similar.
    /// </summary>
    /// <example>
    /// This sample shows how to call the <see cref="Start"/> method.
    /// <code>
    /// static void Main(string[] args)
    /// {
    ///     // create myBot
    ///     ...
    ///     myBot.Start();
    /// } 
    /// </code>
    /// </example>
    void Start();

    /// <summary>
    /// Commits the current commands (actions), which finalizes the current turn for the bot.
    /// 
    /// This method must be called once per turn to send the bot actions to the server and must be
    /// called before the turn timeout occurs. A turn timer is started when the <see
    /// cref="GameStartedEvent"/> and <see cref="TickEvent"/> occurs. If the <see cref="Go"/>
    /// method is called too late,a turn timeout will occur and the <see cref="SkippedTurnEvent"/>
    /// will occur, which means that the bot has skipped all actions for the last turn. In this
    /// case, the server will continue executing the last actions received. This could be fatal for
    /// the bot due to loss of control over the bot. So make sure that <see cref="Go"/> is called
    /// before the turn ends.
    /// 
    /// The commands executed when <see cref="Go"/> is called are set by setting these properties
    /// prior to calling the <see cref="Go"/> method: <see cref="TurnRate"/>, <see
    /// cref="GunTurnRate"/>, <see cref="RadarTurnRate"/>, <see cref="TargetSpeed"/>, and <see
    /// cref="SetFire"/>.
    /// </summary>
    /// <seealso cref="TurnTimeout"/>
    void Go();

    /// <summary>
    /// Unique id of this bot, which is available when the game has started.
    /// </summary>
    /// <value>The unique id of this bot.</value>
    int MyId { get; }

    /// <summary>
    /// The game variant, which is "Tank Royale".
    /// </summary>
    /// <value>The game variant of Robocode.</value>
    string Variant { get; }

    /// <summary>
    /// Game version, e.g. "1.0.0".
    /// </summary>
    /// <value>The game version.</value>
    string Version { get; }

    /// <summary>
    /// Game type, e.g. "melee" or "1v1".
    ///
    /// First available when the game has started.
    /// </summary>
    /// <value>The game type</value>
    string GameType { get; }

    /// <summary>
    /// Width of the arena measured in unit.
    ///
    /// First available when the game has started.
    /// </summary>
    /// <value>The arena width measured in units</value>
    int ArenaWidth { get; }

    /// <summary>
    /// Height of the arena measured in unit.
    ///
    /// First available when the game has started.
    /// </summary>
    /// <value>The arena height measured in units</value>
    int ArenaHeight { get; }

    /// <summary>
    /// The number of rounds in a battle.
    ///
    /// First available when the game has started.
    /// </summary>
    /// <value>The number of rounds in a battle.</value>
    int NumberOfRounds { get; }

    /// <summary>
    /// Gun cooling rate. The gun needs to cool down to a gun heat of zero before the gun can fire.
    /// The gun cooling rate determines how fast the gun cools down. That is, the gun cooling rate
    /// is subtracted from the gun heat each turn until the gun heat reaches zero.
    ///
    /// First available when the game has started.
    /// </summary>
    /// <value>The gun cooling rate</value>
    /// <seealso cref="GunHeat"/>
    double GunCoolingRate { get; }

    /// <summary>
    /// The maximum number of inactive turns allowed the bot will become zapped by the game for
    /// being inactive. Inactive means that the bot has taken no action in several turns in a row.
    ///
    /// First available when the game has started.
    /// </summary>
    /// <value>The maximum number of allowed inactive turns.</value>
    int? MaxInactivityTurns { get; }

    /// <summary>
    /// Turn timeout in microseconds (1 / 1,000,000 second). The turn timeout is important as the
    /// bot needs to take action by calling <see cref="Go"/> before the turn timeout occurs. As
    /// soon as the <see cref="TickEvent"/> is triggered, i.e. when <see cref="OnTick"/> is called,
    /// you need to call <see cref="Go"/> to take action before the turn timeout occurs. Otherwise,
    /// your bot will skip a turn and receive a <see cref="OnSkippedTurn"/> for each turn where
    /// <see cref="Go"/> is called too late.
    ///
    /// First available when the game has started.
    /// </summary>
    /// <value>The turn timeout in microseconds.</value>
    /// <seealso cref="TimeLeft"/>
    /// <seealso cref="Go"/>
    int TurnTimeout { get; }

    /// <summary>
    /// The number of microseconds left of this turn before the bot will skip the turn. Make sure
    /// to call <see cref="Go"/> before the time runs out.
    /// </summary>
    /// <value>The amount of time left in microseconds.</value>
    /// <seealso cref="TurnTimeout"/>
    /// <seealso cref="Go"/>
    int TimeLeft { get; }

    /// <summary>
    /// Current round number.
    /// </summary>
    /// <value>The current round number.</value>
    int RoundNumber { get; }

    /// <summary>
    /// Current turn number.
    /// </summary>
    /// <value>The current turn number.</value>
    int TurnNumber { get; }

    /// <summary>
    /// Number of enemies left in the current round.
    /// </summary>
    /// <value></value>
    int EnemyCount { get; }

    /// <summary>
    /// Current energy level. When the energy level is positive, the bot is alive and active. When
    /// the energy level is 0, the bot is still alive but disabled. If the bot becomes disabled it
    /// will not be able to move or take any action. If negative, the bot has been defeated.
    /// </summary>
    /// <value>The current energy level.</value>
    double Energy { get; }

    /// <summary>
    /// Specifies if the bot is disabled, i.e., when the energy is zero. When the bot is disabled,
    /// it is not able to take any action like movement, turning, and firing.
    /// </summary>
    /// <value><em>true</em> if the bot is disabled; <em>false</em> otherwise</value>
    bool IsDisabled { get; }

    /// <summary>
    /// Current X coordinate of the center of the bot.
    /// </summary>
    /// <value>Current X coordinate of the bot.</value>
    double X { get; }

    /// <summary>
    /// Current Y coordinate of the center of the bot.
    /// </summary>
    /// <value>Current Y coordinate of the bot.</value>
    double Y { get; }

    /// <summary>
    /// Current driving direction of the bot in degrees.
    /// </summary>
    /// <value>The current driving direction of the bot.</value>
    double Direction { get; }

    /// <summary>
    /// Current direction of the gun in degrees.
    /// </summary>
    /// <value>The current gun direction of the bot.</value>
    double GunDirection { get; }

    /// <summary>
    /// Current direction of the radar in degrees.
    /// </summary>
    /// <value>The current radar direction of the bot.</value>
    double RadarDirection { get; }

    /// <summary>
    /// The current speed measured in units per turn. If the speed is positive, the bot moves
    /// forward. If negative, the bot moves backward. Zero speed means that the bot is not moving
    /// from its current position.
    /// </summary>
    /// <value>The current speed.</value>
    double Speed { get; }

    /// <summary>
    /// Current gun heat. When the is fired it gets heated and will not be able to fire before it
    /// has been cooled down. The gun is cooled down when the gun heat is zero.
    ///
    /// When the gun has fired the gun heat is set to 1 + (firepower / 5) and will be cooled down
    /// by the gun cooling rate.
    /// </summary>
    /// <value>The current gun heat.</value>
    /// <seealso cref="GunCoolingRate"/>
    double GunHeat { get; }

    /// <summary>
    /// Current bullet states. Keeps track of all the bullets fired by the bot, which are still
    /// active on the arena.
    /// </summary>
    /// <value>The current bullet states.</value>
    IEnumerable<BulletState> BulletStates { get; }

    /// <summary>
    /// Game events received for the current turn. Note that all event handlers are automatically
    /// being called when each of these events occurs.
    /// </summary>
    /// <value>The game events received for the current turn.</value>
    IEnumerable<Event> Events { get; }

    /// <summary>
    /// Set or get the turn rate of the bot, which can be positive and negative. The turn rate is
    /// measured in degrees per turn. The turn rate is added to the current direction of the bot.
    /// But it is also added to the current direction of the gun and radar. This is because the gun
    /// is mounted on the body, and hence turns with the body. The radar is mounted on the gun and
    /// hence moves with the gun. You can compensate for the turn rate of the bot by subtracting
    /// the turn rate of the bot from the turn rate of the gun and radar. But be aware that the
    /// turn limits defined for the gun and radar cannot be exceeded.
    ///
    /// The turn rate is truncated to <see cref="MaxTurnRate"/> if the turn rate exceeds this
    /// value.
    /// 
    /// If this property is set multiple times, the last value set before <see cref="Go"/> counts.
    /// </summary>
    /// <value>The turn rate of the bot</value>
    double TurnRate { get; set; }

    /// <summary>
    /// Set or get the turn rate of the gun, which can be positive and negative. The gun turn rate
    /// is measured in degrees per turn. The turn rate is added to the current turn direction of
    /// the gun. But it is also added to the current direction of the radar. This is because the
    /// radar is mounted on the gun, and hence moves with the gun. You can compensate for the turn
    /// rate of the gun by subtracting the turn rate of the gun from the turn rate of the radar.
    /// But be aware that the turn limits defined for the radar cannot be exceeded.
    ///
    /// The gun turn rate is truncated to <see cref="MaxGunTurnRate"/> if the gun turn rate exceeds
    /// this value.
    ///
    /// If this property is set multiple times, the last value set before <see cref="Go"/> counts.
    /// </summary>
    /// <value>The turn rate of the gun.</value>
    double GunTurnRate { get; set; }

    /// <summary>
    /// Set or get the turn rate of the radar, which can be positive and negative. The radar turn
    /// rate is measured in degrees per turn. The turn rate is added to the current direction of
    /// the radar. Note that besides the turn rate of the radar, the turn rates of the bot and gun
    /// are also added to the radar direction, as the radar moves with the gun, which is mounted on
    /// the gun that moves with the body. You can compensate for the turn rate of the gun by
    /// subtracting the turn rate of the bot and gun from the turn rate of the radar. But be aware
    /// that the turn limits defined for the radar cannot be exceeded.
    ///
    /// The radar turn rate is truncated to <see cref="MaxRadarTurnRate"/> if the radar turn rate
    /// exceeds this value.
    ///
    /// If this property is set multiple times, the last value set before <see cref="Go"/> counts.
    /// </summary>
    /// <value>The turn rate of the radar.</value>
    double RadarTurnRate { get; set; }

    /// <summary>
    /// Set or get the target speed for the bot in units per turn. The target speed is the speed
    /// you want to achieve eventually, which could take one to several turns depending on the
    /// current speed. For example, if the bot is moving forward with max speed, and then must
    /// change to move backward at full speed, the bot will have to first decelerate/brake its
    /// positive speed (moving forward). When passing speed of zero, it will then have to
    /// accelerate back to achieve max negative speed.
    ///
    /// Note that acceleration is 1 unit per turn and deceleration/braking is faster than
    /// acceleration as it is -2 unit per turn. Deceleration is negative as it is added to the
    /// speed and hence needs to be negative speed down.
    ///
    /// The target speed is truncated to <see cref="MaxSpeed"/> if the target speed exceeds this value.
    ///
    /// If this property is set multiple times, the last value set before <see cref="Go"/> counts.
    /// </summary>
    /// <value>The target speed.</value>
    double TargetSpeed { get; set; }

    /// <summary>
    /// Sets the gun to fire in the direction that the gun is pointing with the specified
    /// firepower.
    ///
    /// Firepower is the amount of energy your bot will spend on firing the gun. This means that
    /// the bot will lose power on firing the gun where the energy loss is equal to the firepower.
    /// You cannot spend more energy than available from your bot. 
    ///
    /// The bullet power must be greater than <see cref="MinFirepower"/> and the gun heat zero
    /// before the gun can fire.
    ///
    /// If the bullet hits an opponent bot, you will gain energy from the bullet hit.When hitting
    /// another bot, your bot will be rewarded and retrieve an energy boost of 3x firepower.
    ///
    /// The gun will only fire when the firepower is at <see cref="MinFirepower"/> or higher. If
    /// the firepower is more than<see cref="MaxFirepower"/> the power will be truncated to the
    /// max firepower.
    ///
    /// Whenever the gun is fired, the gun is heated and needs to cool down before it can fire
    /// again. The gun heat must be zero before the gun is able to fire (see <see cref="GunHeat"/>.
    /// The gun heat generated by firing the gun is 1 + (firepower / 5). Hence, the more firepower
    /// used the longer it takes to cool down the gun. The gun cooling rate can be read by calling
    /// <see cref="GunCoolingRate"/>.
    ///
    /// The amount of energy used for firing the gun is subtracted from the bots' total energy. The
    /// amount of damage dealt by a bullet hitting another bot is 4x firepower, and if the
    /// firepower is greater than 1 it will do an additional 2 x (firepower - 1) damage.
    ///
    /// Note that the gun will automatically keep firing at any turn when the gun heat reaches zero.
    /// It is possible to disable the gun firing by setting the firepower to zero.
    ///
    /// The firepower is truncated to 0 and <see cref="MaxFirepower"/> if the firepower exceeds
    /// this value.
    ///
    /// If this property is set multiple times, the last value set before <see cref="Go"/> counts.
    /// </summary>
    /// <param name="firepower">The new firepower.</param>
    /// <returns><c>true</c> if the cannon can fire, i.e. if there no gun heat; <c>false</c>
    /// otherwise.</returns>
    /// <seealso cref="OnBulletFired"/>
    /// <seealso cref="GunHeat"/>
    /// <seealso cref="GunCoolingRate"/>
    bool SetFire(double firepower);

    /// <summary>
    /// Sets the gun to adjust for the bot's turn when setting the gun turn rate. So the gun
    /// behaves like it is turning independent of the bot's turn.
    ///
    /// Ok, so this needs some explanation: The gun is mounted on the bot's body. So, normally, if
    /// the bot turns 90 degrees to the right, then the gun will turn with it as it is mounted on
    /// top of the bot's body. To compensate for this, you can adjust the gun for the bot's turn.
    /// When this is set, the gun will turn independent from the bot's turn.
    ///
    /// Note: This property is additive until you reach the maximum the gun can turn
    /// <see cref="MaxGunTurnRate"/>. The "adjust" is added to the amount, you set for turning the
    /// bot by the turn rate, then capped by the physics of the game.
    ///
    /// Note: The gun compensating this way does count as "turning the gun".
    /// </summary>
    /// <value><em>true</em> if the gun is set to adjust for the bot's turn; <em>false</em>
    /// otherwise.</value>
    /// <seealso cref="DoAdjustRadarForGunTurn"/>
    bool DoAdjustGunForBodyTurn { get; set; }

    /// <summary>
    /// Sets the radar to adjust for the gun's turn when setting the radar turn rate. So the radar
    /// behaves like it is turning independent of the gun's turn.
    ///
    /// /// Ok, so this needs some explanation: The radar is mounted on the gun. So, normally, if the
    /// gun turns 90 degrees to the right, then the radar will turn with it as it is mounted on top
    /// of the gun. To compensate for this, you can adjust the radar for the gun turn. When this is
    /// set, the radar will turn independent from the gun's turn.
    ///
    /// Note: This property is additive until you reach the maximum the radar can turn
    /// <see cref="MaxRadarTurnRate"/>. The "adjust" is added to the amount, you set for turning
    /// the gun by the gun turn rate, then capped by the physics of the game.
    ///
    /// Note: The radar compensating this way does count as "turning the radar".
    /// </summary>
    /// <value><em>true</em> if the radar is set to adjust for the bot's turn; <em>false</em>
    /// otherwise.</value>
    /// <seealso cref="DoAdjustGunForBodyTurn"/>
    bool DoAdjustRadarForGunTurn { get; set; }

    /// <summary>
    /// Returns the RGB color code of the body. The color code is an integer in hexadecimal format
    /// using bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
    /// 
    /// See <a href="https://www.rapidtables.com/web/color/RGB_Color.html">
    /// https://www.rapidtables.com/web/color/RGB_Color.html</a>
    /// </summary>
    /// <returns>The color code of the body or <c>null</c> if the bot uses the default
    /// color code.</returns>
    int? GetBodyColor();

    /// <summary>
    /// Sets the color of the body. Colors can be changed each turn.
    /// 
    /// Note that currently only the number format using the number sign (#) is supported.
    /// 
    /// See <a href="https://en.wikipedia.org/wiki/Web_colors">
    /// https://en.wikipedia.org/wiki/Web_colors</a>
    /// </summary>
    /// <example>
    /// <code>
    /// SetBodyColor("#09C");
    /// SetBodyColor("#0099CC"); // same color as above
    /// </code>
    /// </example>
    /// <param name="bodyColor">Is the new body color of the bot. Currently hexadecimal number
    /// format is being used.</param>
    void SetBodyColor(string bodyColor);

    /// <summary>
    /// Returns the RGB color code of the gun turret. The color code is an integer in hexadecimal format
    /// using bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
    /// 
    /// See <a href="https://www.rapidtables.com/web/color/RGB_Color.html">
    /// https://www.rapidtables.com/web/color/RGB_Color.html</a>
    /// </summary>
    /// <returns>The color code of the gun turret or <c>null</c> if the bot uses the default
    /// color code.</returns>
    int? GetTurretColor();

    /// <summary>
    /// Sets the color of the gun turret. Colors can be changed each turn.
    /// 
    /// Note that currently only the number format using the number sign (#) is supported.
    /// 
    /// See <a href="https://en.wikipedia.org/wiki/Web_colors">
    /// https://en.wikipedia.org/wiki/Web_colors</a>
    /// </summary>
    /// <example>
    /// <code>
    /// SetTurretColor("#09C");
    /// SetTurretColor("#0099CC"); // same color as above
    /// </code>
    /// </example>
    /// <param name="turretColor">Is the new gun turret color of the bot. Currently hexadecimal number
    /// format is being used.</param>
    void SetTurretColor(string turretColor);

    /// <summary>
    /// Returns the RGB color code of the radar. The color code is an integer in hexadecimal format
    /// using bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
    /// 
    /// See <a href="https://www.rapidtables.com/web/color/RGB_Color.html">
    /// https://www.rapidtables.com/web/color/RGB_Color.html</a>
    /// </summary>
    /// <returns>The color code of the radar or <c>null</c> if the bot uses the default
    /// color code.</returns>
    int? GetRadarColor();

    /// <summary>
    /// Sets the color of the radar. Colors can be changed each turn.
    /// 
    /// Note that currently only the number format using the number sign (#) is supported.
    /// 
    /// See <a href="https://en.wikipedia.org/wiki/Web_colors">
    /// https://en.wikipedia.org/wiki/Web_colors</a>
    /// </summary>
    /// <example>
    /// <code>
    /// SetRadarColor("#09C");
    /// SetRadarColor("#0099CC"); // same color as above
    /// </code>
    /// </example>
    /// <param name="radarColor">Is the new radar color of the bot. Currently hexadecimal number
    /// format is being used.</param>
    void SetRadarColor(string radarColor);

    /// <summary>
    /// Returns the RGB color code of the bullets when fired. The color code is an integer in hexadecimal format
    /// using bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
    /// 
    /// See <a href="https://www.rapidtables.com/web/color/RGB_Color.html">
    /// https://www.rapidtables.com/web/color/RGB_Color.html</a>
    /// </summary>
    /// <returns>The color code of the bullets or <c>null</c> if the bot uses the default
    /// color code.</returns>
    int? GetBulletColor();

    /// <summary>
    /// Sets the color of the bullet when fired. Colors can be changed each turn.
    /// 
    /// Note that currently only the number format using the number sign (#) is supported.
    /// 
    /// See <a href="https://en.wikipedia.org/wiki/Web_colors">
    /// https://en.wikipedia.org/wiki/Web_colors</a>
    /// </summary>
    /// <example>
    /// <code>
    /// SetBulletColor("#09C");
    /// SetBulletColor("#0099CC"); // same color as above
    /// </code>
    /// </example>
    /// <param name="bulletColor">Is the new bullets color of the bot. Currently hexadecimal number
    /// format is being used.</param>
    void SetBulletColor(string bulletColor);

    /// <summary>
    /// Returns the RGB color code of the scan arc. The color code is an integer in hexadecimal format
    /// using bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
    /// 
    /// See <a href="https://www.rapidtables.com/web/color/RGB_Color.html">
    /// https://www.rapidtables.com/web/color/RGB_Color.html</a>
    /// </summary>
    /// <returns>The color code of the scan arc or <c>null</c> if the bot uses the default
    /// color code.</returns>
    int? GetScanColor();

    /// <summary>
    /// Sets the color of the scan arc. Colors can be changed each turn.
    /// 
    /// Note that currently only the number format using the number sign (#) is supported.
    /// 
    /// See <a href="https://en.wikipedia.org/wiki/Web_colors">
    /// https://en.wikipedia.org/wiki/Web_colors</a>
    /// </summary>
    /// <example>
    /// <code>
    /// SetScanColor("#09C");
    /// SetScanColor("#0099CC"); // same color as above
    /// </code>
    /// </example>
    /// <param name="scanColor">Is the new scan arc color of the bot. Currently hexadecimal number
    /// format is being used.</param>
    void SetScanColor(string scanColor);

    /// <summary>
    /// Returns the RGB color code of the tracks. The color code is an integer in hexadecimal format
    /// using bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
    /// 
    /// See <a href="https://www.rapidtables.com/web/color/RGB_Color.html">
    /// https://www.rapidtables.com/web/color/RGB_Color.html</a>
    /// </summary>
    /// <returns>The color code of the tracks or <c>null</c> if the bot uses the default
    /// color code.</returns>
    int? GetTracksColor();

    /// <summary>
    /// Sets the color of the tracks. Colors can be changed each turn.
    /// 
    /// Note that currently only the number format using the number sign (#) is supported.
    /// 
    /// See <a href="https://en.wikipedia.org/wiki/Web_colors">
    /// https://en.wikipedia.org/wiki/Web_colors</a>
    /// </summary>
    /// <example>
    /// <code>
    /// SetTracksColor("#09C");
    /// SetTracksColor("#0099CC"); // same color as above
    /// </code>
    /// </example>
    /// <param name="tracksColor">Is the new tracks color of the bot. Currently hexadecimal number
    /// format is being used.</param>
    void SetTracksColor(string tracksColor);

    /// <summary>
    /// The event handler triggered when connected to the server.
    /// </summary>
    /// <param name="connectedEvent">Event details from the game.</param>
    void OnConnected(ConnectedEvent connectedEvent);

    /// <summary>
    /// The event handler triggered when disconnected from the server.
    /// </summary>
    /// <param name="disconnectedEvent">Event details from the game.</param>
    void OnDisconnected(DisconnectedEvent disconnectedEvent);

    /// <summary>
    /// The event handler triggered when a connection error occurs.
    /// </summary>
    /// <param name="connectionErrorEvent">Event details from the game.</param>
    void OnConnectionError(ConnectionErrorEvent connectionErrorEvent);

    /// <summary>
    /// The event handler triggered when the game has started.
    /// </summary>
    /// <param name="gameStatedEvent">Event details from the game.</param>
    void OnGameStarted(GameStartedEvent gameStatedEvent);

    /// <summary>
    /// The event handler triggered when the game has ended.
    /// </summary>
    /// <param name="gameEndedEvent">Event details from the game.</param>
    void OnGameEnded(GameEndedEvent gameEndedEvent);

    /// <summary>
    /// The event handler triggered when a game tick event occurs, i.e., when a new turn in a round
    /// has started. When this handler is triggered, your bot must figure out the next action to
    /// take and call <see cref="Go"/> when it needs to commit the action to the server.
    /// </summary>
    /// <param name="tickEvent">Event details from the game.</param>
    void OnTick(TickEvent tickEvent);

    /// <summary>
    /// The event handler triggered when another bot has died.
    /// </summary>
    /// <param name="botDeathEvent">Event details from the game.</param>
    void OnBotDeath(BotDeathEvent botDeathEvent);

    /// <summary>
    /// The event handler triggered when this bot has died.
    /// </summary>
    /// <param name="botDeathEvent">Event details from the game.</param>
    void OnDeath(BotDeathEvent botDeathEvent);

    /// <summary>
    /// The event handler triggered when the bot has collided with another bot.
    /// </summary>
    /// <param name="botHitBotEvent">Event details from the game.</param>
    void OnHitBot(BotHitBotEvent botHitBotEvent);

    /// <summary>
    /// The event handler triggered when the bot has hit a wall.
    /// </summary>
    /// <param name="botHitWallEvent">Event details from the game.</param>
    void OnHitWall(BotHitWallEvent botHitWallEvent);

    /// <summary>
    /// The event handler triggered when the bot has fired a bullet.
    /// </summary>
    /// <param name="bulletFiredEvent">Event details from the game.</param>
    void OnBulletFired(BulletFiredEvent bulletFiredEvent);

    /// <summary>
    /// The event handler triggered when the bot has been hit by a bullet.
    /// </summary>
    /// <param name="bulletHitBotEvent">Event details from the game.</param>
    void OnHitByBullet(BulletHitBotEvent bulletHitBotEvent);

    /// <summary>
    /// The event handler triggered when the bot has hit another bot with a bullet.
    /// </summary>
    /// <param name="bulletHitBotEvent">Event details from the game.</param>
    void OnBulletHit(BulletHitBotEvent bulletHitBotEvent);

    /// <summary>
    /// The event handler triggered when a bullet fired from the bot has collided with another bullet.
    /// </summary>
    /// <param name="bulletHitBulletEvent">Event details from the game.</param>
    void OnBulletHitBullet(BulletHitBulletEvent bulletHitBulletEvent);

    /// <summary>
    /// The event handler triggered when a bullet has hit a wall.
    /// </summary>
    /// <param name="bulletHitWallEvent">Event details from the game.</param>
    void OnBulletHitWall(BulletHitWallEvent bulletHitWallEvent);

    /// <summary>
    /// The event handler triggered when the bot has scanned another bot.
    /// </summary>
    /// <param name="scannedBotEvent">Event details from the game.</param>
    void OnScannedBot(ScannedBotEvent scannedBotEvent);

    /// <summary>
    /// The event handler triggered when the bot has skipped a turn. This event occurs if the bot
    /// did not take any action in a specific turn. That is, <see cref="Go"/> was not called before
    /// the turn timeout occurred for the turn. If the bot does not take action for multiple turns
    /// in a row, it will receive a <see cref="SkippedTurnEvent"/> for each turn where it did not
    /// take action. When the bot is skipping a turn, the server did not receive the message from
    /// the bot, and the server will use the newest received instructions for target speed, turn
    /// rates, firing, etc.
    /// </summary>
    /// <param name="skippedTurnEvent">Event details from the game.</param>
    void OnSkippedTurn(SkippedTurnEvent skippedTurnEvent);

    /// <summary>
    /// The event handler triggered when the bot has won a round.
    /// </summary>
    /// <param name="wonRoundEvent">Event details from the game.</param>
    void OnWonRound(WonRoundEvent wonRoundEvent);

    /// <summary>
    /// The event handler triggered when a condition has been met. Use the <see cref="Condition.Name"/> of the
    /// condition if you need to differ between multiple conditions being met.
    /// </summary>
    /// <param name="condition">Is the condition that has been met.</param>
    void OnCondition(Condition condition);

    /// <summary>
    /// Calculates the maximum turn rate for a specific speed.
    /// </summary>
    /// <param name="speed">Is the speed.</param>
    /// <returns>The maximum turn rate determined by the given speed.</returns>
    double CalcMaxTurnRate(double speed);

    /// <summary>
    /// Calculates the bullet speed given a fire power.
    /// </summary>
    /// <param name="firepower">Is the firepower.</param>
    /// <returns>The bullet speed determined by the given firepower.</returns>
    double CalcBulletSpeed(double firepower);

    /// <summary>
    /// Calculates gun heat after having fired the gun.
    /// </summary>
    /// <param name="firepower">Is the firepower used when firing the gun.</param>
    /// <returns>The gun heat produced when firing the gun with the given firepower.</returns>
    double CalcGunHeat(double firepower);

    /// <summary>
    /// Calculates the bearing (delta angle) between the input direction and the direction of this bot.
    /// </summary>
    /// <code>
    /// bearing = CalcBearing(direction) = NormalizeRelativeDegrees(direction - this.Direction)
    /// </code>
    /// <param name="direction">Is the input direction to calculate the bearing from.</param>
    /// <returns>A bearing (delta angle) between the input direction and the direction of this bot.
    /// The bearing is a normalized angle in the range [-180,180[</returns>
    /// <seealso cref="Direction"/>
    /// <seealso cref="NormalizeRelativeDegrees"/>
    double CalcBearing(double direction);

    /// <summary>
    /// Calculates the bearing (delta angle) between the input direction and the direction of the gun.
    /// </summary>
    /// <code>
    /// bearing = CalcGunBearing(direction) = NormalizeRelativeDegrees(direction - this.GunDirection)
    /// </code>
    /// <param name="direction">Is the input direction to calculate the bearing from.</param>
    /// <returns>A bearing (delta angle) between the input direction and the direction of the gun.
    /// The bearing is a normalized angle in the range [-180,180[</returns>
    /// <seealso cref="GunDirection"/>
    /// <seealso cref="NormalizeRelativeDegrees"/>
    double CalcGunBearing(double direction);

    /// <summary>
    /// Calculates the bearing (delta angle) between the input direction and the direction of the radar.
    /// </summary>
    /// <code>
    /// bearing = CalcRadarBearing(direction) = NormalizeRelativeDegrees(direction - this.RadarDirection)
    /// </code>
    /// <param name="direction">Is the input direction to calculate the bearing from.</param>
    /// <returns>A bearing (delta angle) between the input direction and the direction of the radar.
    /// The bearing is a normalized angle in the range [-180,180[</returns>
    /// <seealso cref="RadarDirection"/>
    /// <seealso cref="NormalizeRelativeDegrees"/>
    double CalcRadarBearing(double direction);

    /// <summary>
    /// Calculates the direction (angle) from the bot�s coordinates to a point x,y.
    /// </summary>
    /// <param name="x">Is the x coordinate of the point.</param>
    /// <param name="y">Is the y coordinate of the point.</param>
    /// <returns>The direction to the point x,y in the range [0,360[</returns>
    double DirectionTo(double x, double y);

    /// <summary>
    /// Calculates the bearing (delta angle) between the bot's coordinates and direction and the
    /// direction to the point x,y.
    /// </summary>
    /// <param name="x">Is the x coordinate of the point.</param>
    /// <param name="y">Is the y coordinate of the point.</param>
    /// <returns>The bearing to the point x,y in the range [-180,180[</returns>
    double BearingTo(double x, double y);

    /// <summary>
    /// Calculates the distance from the bot's coordinates to a point x,y.
    /// </summary>
    /// <param name="x">Is the x coordinate of the point.</param>
    /// <param name="y">Is the y coordinate of the point.</param>
    /// <returns>The distance to the point x,y.</returns>
    double DistanceTo(double x, double y);

    /// <summary>
    /// Normalizes an angle to an absolute angle into the range [0,360[
    /// </summary>
    /// <param name="angle">Is the angle to normalize.</param>
    /// <returns>The normalized absolute angle.</returns>
    double NormalizeAbsoluteDegrees(double angle);

    /// <summary>
    /// Normalizes an angle to an relative angle into the range [-180,180[
    /// </summary>
    /// <param name="angle">Is the angle to normalize.</param>
    /// <returns>The normalized relative angle.</returns>
    double NormalizeRelativeDegrees(double angle);
  }
}