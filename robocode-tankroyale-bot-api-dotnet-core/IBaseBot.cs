using System.Collections.Generic;

namespace Robocode.TankRoyale
{
  public interface IBaseBot
  {
    /// <summary>
    /// Main method for start running the bot.
    /// </summary>
    void Start();

    /// <summary>
    /// Commits the current actions for the current turn. This method must be called in order to send
    /// the bot actions to the server, and MUST before the turn timeout occurs. The turn timeout is
    /// started when the GameStartedEvent and TickEvent occurs. If Go() is called too late,
    /// SkippedTurnEvents will occur. Actions are set by calling the setter methods prior to calling
    /// the Go() method: SetTurnRate(), SetGunTurnRate(), SetRadarTurnRate(), SetTargetSpeed(), and
    /// SetFire().
    /// </summary>
    void Go();

    /// <summary>
    /// Property containing the unique id of this bot in the battle. Available when game has started.
    /// </summary>
    int MyId { get; }

    /// <summary>
    /// Property containing the game variant, e.g. "Tank Royale" for Robocode Tank Royale.
    /// </summary>
    string Variant { get; }

    /// <summary>
    /// Property containing the game version, e.g. "1.0.0"
    /// </summary>
    string Version { get; }

    /// <summary>
    /// Property containing the game type, e.g. "melee".
    ///
    /// Available when game has started.
    /// </summary>
    string GameType { get; }

    /// <summary>
    /// Property containing the width of the arena measured in pixels.
    ///
    /// Available when game has started.
    /// </summary>
    int ArenaWidth { get; }

    /// <summary>
    /// Property containing the height of the arena measured in pixels.
    ///
    /// Available when game has started.
    /// </summary>
    int ArenaHeight { get; }

    /// <summary>
    /// Property containing the number of rounds in a battle.
    ///
    /// Available when game has started.
    /// </summary>
    int NumberOfRounds { get; }

    /// <summary>
    /// Property containing the gun cooling rate. The gun needs to cool down to a gun heat of zero
    /// before the gun is able to fire. The gun cooling rate determines how fast the gun cools down.
    /// That is, the gun cooling rate is subtracted from the gun heat each turn until the gun heat
    /// reaches zero.
    ///
    /// Available when game has started.
    /// </summary>
    /// <seealso cref="GunHeat"/>
    double GunCoolingRate { get; }

    /// <summary>
    /// Property containing the maximum number of inactive turns allowed, where a bot does not take any
    /// action before it is zapped by the game.
    ///
    /// Available when game has started.
    /// </summary>
    int MaxInactivityTurns { get; }

    /// <summary>
    /// Property containing turn timeout in microseconds (1 / 1,000,000 second). The turn timeout is
    /// important as the bot need to take action by calling Go() before the turn timeout occurs. As
    /// soon as the TickEvent is triggered, i.e. when OnTick() is called, you need to call Go() to take
    /// action before the turn timeout occurs. Otherwise your bot will receive SkippedTurnEvent(s).
    ///
    /// Available when game has started.
    /// </summary>
    /// <seealso cref="TimeLeft"/>
    /// <seealso cref="Go"/>
    int TurnTimeout { get; }

    /// <summary>
    /// Property containing the number of microseconds left for this round before the bot will skip the
    /// turn. Make sure to call Go() before the time runs out.
    /// </summary>
    /// <seealso cref="TurnTimeout"/>
    /// <seealso cref="Go"/>
    int TimeLeft { get; }

    /// <summary>
    /// Property containing the current round number.
    /// </summary>
    int RoundNumber { get; }

    /// <summary>
    /// Property containing the current turn number.
    /// </summary>
    int TurnNumber { get; }

    /// <summary>
    /// Property containing the current energy level. When positive, the bot is alive and active. When
    /// 0, the bot is alive, but disabled, meaning that it will not be able to move. If negative, the
    /// bot has been defeated.
    /// </summary>
    double Energy { get; }

    /// <summary>
    /// Property indicating if the bot is disabled, i.e. when the energy is zero. When the bot is
    /// disabled, it it is not able to take any action like movement, turning and firing.
    /// </summary>
    bool Disabled { get; }

    /// <summary>
    /// Property containing the X coordinate of the center of the bot.
    /// </summary>
    double X { get; }

    /// <summary>
    /// Property containing the Y coordinate of the center of the bot.
    /// </summary>
    double Y { get; }

    /// <summary>
    /// Property containing the driving direction of the body in degrees.
    /// </summary>
    double Direction { get; }

    /// <summary>
    /// Property containing the gun direction of the body in degrees.
    /// </summary>
    double GunDirection { get; }

    /// <summary>
    /// Property containing the radar direction of the body in degrees.
    /// </summary>
    double RadarDirection { get; }

    /// <summary>
    /// Property containing the speed measured in pixels per turn. If the speed is positive, the bot
    /// moves forward. If negative, the bot moves backwards. A zero speed means that the bot is not
    /// moving from its current position.
    /// </summary>
    double Speed { get; }

    /// <summary>
    /// Property containing the gun heat. The gun gets heated when it is fired, and will first be able
    /// to fire again, when the gun has cooled down, meaning that the gun heat must be zero.
    /// 
    /// When the gun is fired the gun heat is set to 1 + (firepower / 5). The gun is cooled down by
    /// the gun cooling rate.
    /// </summary>
    /// <seealso cref="GunCoolingRate"/>
    double GunHeat { get; }

    /// <summary>
    /// Property containing the current bullet states.
    /// </summary>
    ICollection<BulletState> BulletStates { get; }

    /// <summary>
    /// Property containing the game events received for the current turn.
    /// </summary>
    ICollection<Event> Events { get; }

    /// <summary>
    /// Sets the turn rate of the body in degrees per turn (can be positive and negative). The turn
    /// rate is added to the current turn direction of the body. But it is also added to the current
    /// direction of the gun and radar. This is because the gun is mounted on the body, and hence turns
    /// with the body. The radar is mounted on the gun, and hence moves with the gun. By subtracting
    /// the turn rate of the body from the turn rate of the gun and radar, you can compensate for the
    /// turn rate of the body. But be aware that the turn limits for the gun and radar cannot be
    /// exceeded.
    /// 
    /// The turn rate is truncated to MAX_TURN_RATE if the turn rate exceeds this value.
    /// 
    /// If this property is set multiple times, the last value set before Go() counts.
    /// </summary>
    double TurnRate { get; set; }

    /// <summary>
    /// Sets the new turn rate of the gun in degrees per turn (can be positive and negative). The turn
    /// rate is added to the current turn direction of the gun. But it is also added to the current
    /// direction of the radar. This is because the radar is mounted on the gun, and hence moves with
    /// the gun. You can compensate for this by subtracting the turn rate of the gun and body from the
    /// turn rate of the radar. And you can compensate the turn rate of the body on the gun by
    /// subtracting the turn rate of the body from the turn rate of the gun. But be aware that the turn
    /// limits for the radar (and also body and gun) cannot be exceeded.
    ///
    /// The gun turn rate is truncated to MAX_GUN_TURN_RATE if the gun turn rate exceeds
    /// this value.
    ///
    /// If this property is set multiple times, the last value set before Go() counts.
    /// </summary>
    double GunTurnRate { get; set; }

    /// <summary>
    /// Sets the new turn rate of the radar in degrees per turn (can be positive and negative). The
    /// turn rate is added to the current turn direction of the radar. Note that beside the turn rate
    /// of the radar, the turn rates of the body and gun is also added to the radar direction, as the
    /// radar moves with the gun, which is mounted on the gun that moves with the body. You can
    /// compensate for this by subtracting the turn rate of the body and gun from the turn rate of the
    /// radar. But be aware that the turn limits for the radar (and also body and gun) cannot be
    /// exceeded.
    ///
    /// The radar turn rate is truncated to MAX_RADAR_TURN_RATE if the radar turn rate
    /// exceeds this value.
    ///
    /// If this property is set multiple times, the last value set before Go() counts.
    /// </summary>
    double RadarTurnRate { get; set; }

    /// <summary>
    /// Sets the new target speed for the bot in units per turn. The target speed is the speed you want
    /// to achieve eventually, which could take one to several turns to achieve depending on the
    /// current speed. For example, if the bot is moving forward with max speed, and then must change
    /// to move backwards at full speed, the bot will need to first decelerate/brake its positive speed
    /// (moving forward). When passing a speed of zero, it will then need to accelerate backwards to
    /// achieve max negative speed.
    ///
    /// Note that acceleration is 1 pixel/turn and deceleration/braking is faster than acceleration
    /// as it is -2 pixel/turn. Deceleration is negative as it is added to the speed and hence needs to
    /// be negative.
    ///
    /// The target speed is truncated to MAX_SPEED if the target speed exceeds this value.
    ///
    /// If this property is set multiple times, the last value set before go() counts.
    /// </summary>
    double TargetSpeed { get; set; }

    /// <summary>
    /// Sets the gun to fire in the direction as the gun is pointing with the specified firepower.
    ///
    /// Firepower is the amount of energy spend on firing the gun. You cannot spend more energy
    /// that available from the bot. The amount of energy loss is equal to the firepower.
    /// 
    /// The bullet power must be > MIN_FIREPOWER and the gun heat zero before the gun is able to fire.
    /// 
    /// If the bullet hits an opponent bot, you will gain energy from the bullet hit. When hitting
    /// another bot, your bot will be rewarded and retrieve an energy boost of 3x firepower.
    ///
    /// The gun will only fire when the firepower is at MIN_FIREPOWER or higher. If the
    /// firepower is more than MAX_FIREPOWER, the power will be truncated to the max
    /// firepower.
    ///
    /// Whenever the gun is fired, the gun is heated an needs to cool down before it is able to fire
    /// again. The gun heat must be zero before the gun is able to fire (see <see cref="GunHeat"/>). The
    /// gun heat generated by firing the gun is 1 + (firepower / 5). Hence, the more firepower used the
    /// longer it takes to cool down the gun. The gun cooling rate can be read by calling
    /// <see cref="GunCoolingRate"/>.
    ///
    /// The amount of energy used for firing the gun is subtracted from the bots total energy. The
    /// amount of damage dealt by a bullet hitting another bot is 4x firepower, and if the firepower is
    /// greater than 1 it will do an additional 2 x (firepower - 1) damage.
    ///
    /// Note that the gun will automatically keep firing at any turn when the gun heat reaches zero.
    /// It is possible disable the gun firing by setting the firepower on this property to zero.
    ///
    /// The firepower is truncated to 0 and MAX_FIREPOWER if the firepower exceeds these values.
    ///
    /// If this property is set multiple times, the last value set before Go() counts.
    /// </summary>
    /// <seealso cref="OnBulletFired"/>
    /// <seealso cref="GunHeat"/>
    /// <seealso cref="GunCoolingRate"/>
    double Fire { set; }

    /// <summary>
    /// Sets the gun to adjust for the bot's turn when setting the gun turn rate. So the gun behaves
    /// like it is turning independent of the bot's turn.
    ///
    /// Ok, so this needs some explanation: The gun is mounted on the bot's body. So, normally, if
    /// the bot turns 90 degrees to the right, then the gun will turn with it as it is mounted on top
    /// of the bot's body. To compensate for this, you can adjust the gun for the body turn.
    /// When this is set, the gun will turn independent from the bot's turn.
    ///
    /// Note: This property is additive until you reach the maximum the gun can turn MAX_GUN_TURN_RATE.
    /// The "adjust" is added to the amount, you set for turning the bot by the turn rate, then capped
    /// by the physics of the game.
    ///
    /// Note: The gun compensating this way does count as "turning the gun".
    /// </summary>
    /// <seealso cref="AdjustRadarForGunTurn"/>
    bool AdjustGunForBodyTurn { get; set; }

    /// <summary>
    /// Sets the radar to adjust for the gun's turn when setting the radar turn rate. So the radar
    /// behaves like it is turning independent of the gun's turn.
    ///
    /// Ok, so this needs some explanation: The radar is mounted on the gun. So, normally, if the
    /// gun turns 90 degrees to the right, then the radar will turn with it as it is mounted on top of
    /// the gun. To compensate for this, you can adjust the radar for the gun turn. When this
    /// is set, the radar will turn independent from the gun's turn.
    ///
    /// Note: This property is additive until you reach the maximum the radar can turn
    /// MAX_RADAR_TURN_RATE. The "adjust" is added to the amount, you set for turning the gun by
    /// the gun turn rate, then capped by the physics of the game.
    ///
    /// Note: The radar compensating this way does count as "turning the radar".
    /// </summary>
    /// <seealso cref="AdjustGunForBodyTurn"/>
    bool AdjustRadarForGunTurn { get; set; }

  }
}