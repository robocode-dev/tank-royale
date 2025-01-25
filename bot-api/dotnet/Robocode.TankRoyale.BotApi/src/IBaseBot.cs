using System;
using System.Collections.Generic;
using System.Drawing;
using Robocode.TankRoyale.BotApi.Events;
using SvgNet.Interfaces;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Interface containing the core API for a bot.
/// </summary>
public interface IBaseBot
{
    /// <summary>
    /// The maximum size of a team message, which is 32 KB (32.786 bytes).
    /// </summary>
    const int TeamMessageMaxSize = 32768; // bytes

    /// <summary>
    /// The maximum number of team messages that can be sent per turn, which is 10 messages.
    /// </summary>
    const int MaxNumberOfTeamMessagesPerTurn = 10;

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
    /// <value><c>true</c> if the bot is disabled; <c>false</c> otherwise</value>
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
    /// an ordered list containing all events currently in the bot's event queue. You might, for example, call
    /// this while processing another event.
    /// </summary>
    /// <value>an ordered list containing all events currently in the bot's event queue.</value>
    /// <see cref="ClearEvents"/>
    IList<BotEvent> Events { get; }

    /// <summary>
    /// Clears out any pending events in the bot's event queue immediately.
    /// </summary>
    /// <see cref="Events"/>
    void ClearEvents();

    /// <summary>
    /// Set or get the turn rate of the bot, which can be positive and negative. The turn rate is
    /// measured in degrees per turn. The turn rate is added to the current direction of the bot.
    /// But it is also added to the current direction of the gun and radar. This is because the gun
    /// is mounted on the body, and hence turns with the body. The radar is mounted on the gun and
    /// hence moves with the gun. You can compensate for the turn rate of the bot by subtracting
    /// the turn rate of the bot from the turn rate of the gun and radar. But be aware that the
    /// turn limits defined for the gun and radar cannot be exceeded.
    ///
    /// The turn rate is truncated to <see cref="Constants.MaxTurnRate"/> if the turn rate exceeds this
    /// value.
    /// 
    /// If this property is set multiple times, the last value set before <see cref="Go"/> counts.
    /// </summary>
    /// <value>The turn rate of the bot</value>
    /// <seealso cref="MaxTurnRate"/>
    double TurnRate { get; set; }

    /// <summary>
    /// Sets or gets the maximum turn rate which applies to turn the bot to the left or right. The maximum
    /// turn rate must be an absolute value from 0 to <see cref="Constants.MaxTurnRate"/>, both values are
    /// included. If the input turn rate is negative, the max turn rate will be cut to zero. If the
    /// input turn rate is above <see cref="Constants.MaxTurnRate"/>, the max turn rate will be set to
    /// <see cref="Constants.MaxTurnRate"/>.
    ///
    /// If for example the max turn rate is set to 5, then the bot will be able to turn left or
    /// right with a turn rate down to -5 degrees per turn when turning right, and up to 5 degrees per turn
    /// when turning left.
    ///
    /// This method will first be executed when <see cref="Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="Go"/> is executed,
    /// counts.
    /// </summary>
    /// <value>The maximum turn rate of the bot.</value>
    /// <seealso cref="TurnRate"/>
    double MaxTurnRate { get; set; }

    /// <summary>
    /// Set or get the turn rate of the gun, which can be positive and negative. The gun turn rate
    /// is measured in degrees per turn. The turn rate is added to the current turn direction of
    /// the gun. But it is also added to the current direction of the radar. This is because the
    /// radar is mounted on the gun, and hence moves with the gun. You can compensate for the turn
    /// rate of the gun by subtracting the turn rate of the gun from the turn rate of the radar.
    /// But be aware that the turn limits defined for the radar cannot be exceeded.
    ///
    /// The gun turn rate is truncated to <see cref="Constants.MaxGunTurnRate"/> if the gun turn rate exceeds
    /// this value.
    ///
    /// If this property is set multiple times, the last value set before <see cref="Go"/> counts.
    /// </summary>
    /// <value>The turn rate of the gun.</value>
    /// <seealso cref="MaxGunTurnRate"/>
    double GunTurnRate { get; set; }

    /// <summary>
    /// Sets or gets the maximum turn rate which applies to turn the gun to the left or right. The maximum turn
    /// rate must be an absolute value from 0 to <see cref="Constants.MaxGunTurnRate"/>, both values are
    /// included. If the input turn rate is negative, the max turn rate will be cut to zero. If the
    /// input turn rate is above <see cref="Constants.MaxGunTurnRate"/>, the max turn rate will be set to
    /// <see cref="Constants.MaxGunTurnRate"/>.
    ///
    /// If for example the max gun turn rate is set to 5, then the gun will be able to turn left or
    /// right with a turn rate down to -5 degrees per turn when turning right and up to 5 degrees per
    /// turn when turning left.
    ///
    /// This method will first be executed when <see cref="Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="Go"/> is executed,
    /// counts.
    /// </summary>
    /// <value>The maximum turn rate of the gun.</value>
    /// <seealso cref="GunTurnRate"/>
    double MaxGunTurnRate { get; set; }

    /// <summary>
    /// Set or get the turn rate of the radar, which can be positive and negative. The radar turn
    /// rate is measured in degrees per turn. The turn rate is added to the current direction of
    /// the radar. Note that besides the turn rate of the radar, the turn rates of the bot and gun
    /// are also added to the radar direction, as the radar moves with the gun, which is mounted on
    /// the gun that moves with the body. You can compensate for the turn rate of the gun by
    /// subtracting the turn rate of the bot and gun from the turn rate of the radar. But be aware
    /// that the turn limits defined for the radar cannot be exceeded.
    ///
    /// The radar turn rate is truncated to <see cref="Constants.MaxRadarTurnRate"/> if the radar turn rate
    /// exceeds this value.
    ///
    /// If this property is set multiple times, the last value set before <see cref="Go"/> counts.
    /// </summary>
    /// <value>The turn rate of the radar.</value>
    /// <seealso cref="MaxRadarTurnRate"/>
    double RadarTurnRate { get; set; }

    /// <summary>
    /// Sets or gets the maximum turn rate which applies to turn the radar to the left or right. The maximum
    /// turn rate must be an absolute value from 0 to MaxRadarTurnRate, both values are
    /// included. If the input turn rate is negative, the max turn rate will be cut to zero. If the
    /// input turn rate is above MaxRadarTurnRate, the max turn rate will be set to MaxRadarTurnRate.
    ///
    /// If for example the max radar turn rate is set to 5, then the radar will be able to turn left
    /// or right with a turn rate down to -5 degrees per turn when turning right and up to 5 degrees per turn
    /// when turning left.
    ///
    /// This method will first be executed when <see cref="Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="Go"/> is executed,
    /// counts.
    /// </summary>
    /// <value>The maximum turn rate of the radar.</value>
    /// <seealso cref="RadarTurnRate"/>
    double MaxRadarTurnRate { get; set; }

    /// <summary>
    /// Set or get the target speed for the bot in units per turn. The target speed is the speed
    /// you want to achieve eventually, which could take one to several turns depending on the
    /// current speed. For example, if the bot is moving forward with max speed, and then must
    /// change to move backward at full speed, the bot will have to first decelerate/brake its
    /// positive speed (moving forward). When passing speed of zero, it will then have to
    /// accelerate back to achieve max negative speed.
    ///
    /// <note>
    /// Acceleration is 1 unit per turn and deceleration/braking is faster than acceleration as it
    /// is -2 unit per turn. Deceleration is negative as it is added to the speed and hence needs
    /// to be negative when slowing down.
    /// </note>
    /// <note>
    /// The target speed is truncated to <see cref="Constants.MaxSpeed"/> if the target speed exceeds this value.
    /// </note>
    ///
    /// If this property is set multiple times, the last value set before <see cref="Go"/> counts.
    /// </summary>
    /// <value>The target speed.</value>
    /// <see cref="MaxSpeed"/>
    double TargetSpeed { get; set; }

    /// <summary>
    /// Sets or gets the maximum speed which applies when moving forward and backward. The maximum speed must
    /// be an absolute value from 0 to MaxSpeed, both values are included. If the input
    /// speed is negative, the max speed will be cut to zero. If the input speed is above
    /// MaxSpeed, the max speed will be set to MaxSpeed.
    ///
    /// If for example the maximum speed is set to 5, then the bot will be able to move backwards
    /// with a speed down to -5 units per turn and up to 5 units per turn when moving forward.
    ///
    /// This method will first be executed when <see cref="Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="Go"/> is executed,
    /// counts.
    /// </summary>
    /// <value>The maximum speed.</value>
    /// <see cref="TargetSpeed"/>
    double MaxSpeed { get; set; }

    /// <summary>
    /// Sets the gun to fire in the direction that the gun is pointing with the specified
    /// firepower.
    ///
    /// Firepower is the amount of energy your bot will spend on firing the gun. This means that
    /// the bot will lose power on firing the gun where the energy loss is equal to the firepower.
    /// You cannot spend more energy than available from your bot. 
    ///
    /// The bullet power must be greater than <see cref="Constants.MinFirepower"/> and the gun heat zero
    /// before the gun can fire.
    ///
    /// If the bullet hits an opponent bot, you will gain energy from the bullet hit.When hitting
    /// another bot, your bot will be rewarded and retrieve an energy boost of 3x firepower.
    ///
    /// The gun will only fire when the firepower is at <see cref="Constants.MinFirepower"/> or higher. If
    /// the firepower is more than<see cref="Constants.MaxFirepower"/> the power will be truncated to the
    /// max firepower.
    ///
    /// Whenever the gun is fired, the gun is heated and needs to cool down before it can fire
    /// again. The gun heat must be zero before the gun is able to fire (see <see cref="GunHeat"/>.
    /// The gun heat generated by firing the gun is 1 + (firepower / 5). Hence, the more firepower
    /// used the longer it takes to cool down the gun. The gun cooling rate can be read by calling
    /// <see cref="GunCoolingRate"/>.
    ///
    /// The amount of energy used for firing the gun is subtracted from the bot´s total energy. The
    /// amount of damage dealt by a bullet hitting another bot is 4x firepower, and if the
    /// firepower is greater than 1 it will do an additional 2 x (firepower - 1) damage.
    ///
    /// <note>
    /// The gun will automatically keep firing at any turn as soon as the gun heat reaches zero.
    /// It is possible to disable the gun firing by setting the firepower to zero.
    /// </note>
    ///
    /// The firepower is truncated to 0 and <see cref="Constants.MaxFirepower"/> if the firepower exceeds
    /// this value.
    ///
    /// If this property is set multiple times, the last value set before <see cref="Go"/> counts.
    /// </summary>
    /// <param name="firepower">The new firepower.</param>
    /// <returns><c>true</c> if the cannon can fire, i.e. if there is no gun heat; <c>false</c>
    /// otherwise.</returns>
    /// <seealso cref="OnBulletFired"/>
    /// <seealso cref="Firepower"/>
    /// <seealso cref="GunHeat"/>
    /// <seealso cref="GunCoolingRate"/>
    bool SetFire(double firepower);

    /// <summary>
    /// The firepower.
    /// </summary>
    /// <returns>The firepower</returns>
    /// <see cref="SetFire"/>
    double Firepower { get; }

    /// <summary>
    /// Sets the bot to rescan with the radar. This method is useful if the radar has not turned, and
    /// hence will not automatically scan bots. The last radar direction and sweep angle will be used
    /// for scanning for bots.
    /// </summary>
    void SetRescan();

    /// <summary>
    /// Enables or disables fire assistance explicitly. Fire assistance is useful for bots with limited
    /// aiming capabilities as it will help the bot by firing directly at a scanned bot when the gun is fired,
    /// which is a very simple aiming strategy.
    /// 
    /// When fire assistance is enabled the gun will fire directly towards the center of the scanned bot
    /// when all these conditions are met:
    /// <ul>
    ///     <li>The gun is fired <see cref="SetFire"/> and <see cref="IBot.Fire"/></li>
    ///     <li>The radar is scanning a bot <em>when</em> firing the gun (<see cref="OnScannedBot"/>,
    ///         <see cref="SetRescan"/>, <see cref="IBot.Rescan"/>)</li>
    ///     <li>The gun and radar are pointing in the exact the same direction. You can set
    ///         <c>AdjustRadarForGunTurn=false</c> to align the gun and radar and make sure not to
    ///         turn the radar beside the gun.</li>
    /// </ul>
    /// The fire assistance feature is provided for backwards compatibility with the original Robocode,
    /// where robots that are not an <c>AdvancedRobot</c> got fire assistance per default as the gun and
    /// radar cannot be moved independently of each other. (The <c>AdvancedRobot</c> allows the body, gun,
    /// and radar to move independent of each other).
    /// </summary>
    /// <param name="enable">Enables fire assistance when set to <c>true</c>, and disable fire assistance
    /// otherwise.</param>
    void SetFireAssist(bool enable);

    /// <summary>
    /// Set this property during an event handler to control continuing or restarting the event handler,
    /// when a new event occurs again for the same event handler while processing an earlier event.
    /// </summary>
    /// <example>
    /// Example:
    /// <code>
    /// public void OnScannedBot(ScannedBotEvent e)
    /// {
    ///     Fire(1);
    ///     <b>Interruptible = true;</b>
    ///     Forward(100); // When a new bot is scanned while moving forward this handler will restart
    ///                   // from the top as this event handler has been set to be interruptible
    ///                   // right after firing. Without <c>Interruptible = true</c>, new scan events
    ///                   // would not be triggered while moving forward.
    ///     // We'll only get here if we do not see a robot during the move.
    ///     Console.WriteLine("No bots were scanned");
    /// }
    /// </code></example>
    /// <value><c>true</c> if the event handler should be interrupted and hence restart when a new
    /// event of the same event type occurs again; <c>false</c> otherwise where the event handler
    /// will continue processing.</value>
    bool Interruptible { set; }

    /// <summary>
    /// Sets the gun to adjust for the bot´s turn when setting the gun turn rate. So the gun
    /// behaves like it is turning independent of the bot´s turn.
    ///
    /// Ok, so this needs some explanation: The gun is mounted on the bot´s body. So, normally, if
    /// the bot turns 90 degrees to the right, then the gun will turn with it as it is mounted on
    /// top of the bot´s body. To compensate for this, you can adjust the gun for the body turning.
    /// When this is set, the gun will turn independent of the body turning.
    ///
    /// <note>
    /// This property is additive until you reach the maximum the gun can turn <see
    /// cref="Constants.MaxGunTurnRate"/>. The "adjust" is added to the amount, you set for turning the bot
    /// by the turn rate, then capped by the physics of the game.
    ///
    /// The gun compensating this way does count as "turning the gun".
    /// </note>
    /// </summary>
    /// <value><c>true</c> if the gun is set to adjust for the body turning; <c>false</c>
    /// otherwise (default).</value>
    /// <seealso cref="AdjustRadarForBodyTurn"/>
    /// <seealso cref="AdjustRadarForGunTurn"/>
    bool AdjustGunForBodyTurn { get; set; }

    /// <summary>
    /// Sets the radar to adjust for the body's turn when setting the radar turn rate. So the radar
    /// behaves like it is turning independent of the body's turn.
    ///
    /// Ok, so this needs some explanation: The radar is mounted on the gun, and the gun is mounted on the bot´s body.
    /// So, normally, if the bot turns 90 degrees to the right, the gun turns, as does the radar. Hence, if the bot
    /// turns 90 degrees to the right, then the gun and radar will turn with it as the radar is mounted on top of the
    /// gun. To compensate for this, you can adjust the radar for the body turn. When this is set, the radar will turn
    /// independent of the body's turn.
    ///
    /// <note>
    /// This property is additive until you reach the maximum the radar can turn <see
    /// cref="Constants.MaxRadarTurnRate"/>. The "adjust" is added to the amount, you set for turning the body
    /// by the body turn rate, then capped by the physics of the game.
    ///
    /// The radar compensating this way does count as "turning the radar".
    /// </note>
    /// </summary>
    /// <value><c>true</c> if the radar is set to adjust for the body turning; <c>false</c>
    /// otherwise (default).</value>
    /// <seealso cref="AdjustGunForBodyTurn"/>
    /// <seealso cref="AdjustRadarForGunTurn"/>
    bool AdjustRadarForBodyTurn { get; set; }

    /// <summary>
    /// Sets the radar to adjust for the gun's turn when setting the radar turn rate. So the radar
    /// behaves like it is turning independent of the gun's turn.
    ///
    /// Ok, so this needs some explanation: The radar is mounted on the gun. So, normally, if the
    /// gun turns 90 degrees to the right, then the radar will turn with it as it is mounted on top
    /// of the gun. To compensate for this, you can adjust the radar for the gun turn. When this is
    /// set, the radar will turn independent of the gun turning.
    ///
    /// <note>
    /// This property is additive until you reach the maximum the radar can turn <see
    /// cref="Constants.MaxRadarTurnRate"/>. The "adjust" is added to the amount, you set for turning the gun
    /// by the gun turn rate, then capped by the physics of the game.
    ///
    /// When the radar compensates this way it counts as "turning the radar", even when it is not
    /// explicitly turned by calling a method for turning the radar.
    /// </note>
    /// 
    /// When the radar compensates this way it counts as "turning the radar", even when it is not
    /// explicitly turned by calling a method for turning the radar.
    /// <note>
    /// This method automatically disables fire assistance when set to <c>true</c>, and automatically
    /// enables fire assistance when set to <c>false</c>. This is <em>not</em> the case for <see
    /// cref="AdjustGunForBodyTurn"/> and <see cref="AdjustRadarForBodyTurn"/>.
    /// Read more about fire assistance with the <see cref="SetFireAssist"/> method.
    /// </note>
    /// </summary>
    /// <seealso cref="AdjustGunForBodyTurn"/>
    /// <seealso cref="AdjustRadarForBodyTurn"/>
    bool AdjustRadarForGunTurn { get; set; }

    /// <summary>
    /// Adds a event handler that will be automatically triggered <see cref="OnCustomEvent(CustomEvent)"/>
    /// when the <see cref="Condition.Test()"/> returns true.
    /// </summary>
    /// <param name="condition">Is the condition that must be met to trigger the custom event.</param>
    /// <return><c>true</c> if the condition was not added already; <c>false</c> if the condition was already added.</return>
    /// <seealso cref="RemoveCustomEvent"/>
    bool AddCustomEvent(Condition condition);

    /// <summary>
    /// Removes triggering an custom event handler for a specific condition that was previously added
    /// with <see cref="AddCustomEvent(Condition)"/>.
    /// </summary>
    /// <param name="condition">is the condition that was previously added with <see cref="AddCustomEvent(Condition)"/></param>
    /// <return><c>true</c> if the condition was found; <c>false</c> if the condition was not found.</return>
    /// <seealso cref="AddCustomEvent(Condition)"/>
    bool RemoveCustomEvent(Condition condition);

    /// <summary>
    /// Set the bot to stop all movement including turning the gun and radar. The remaining movement is
    /// saved for a call to <see cref="SetResume"/>. This method has no effect, if it has already been
    /// called.
    ///
    /// This method will first be executed when <see cref="IBaseBot.Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="IBaseBot.Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="IBaseBot.Go"/>.
    /// </summary>
    /// <seealso cref="IBot.Stop()"/>
    /// <seealso cref="SetResume"/>
    /// <seealso cref="IBot.Resume"/>
    void SetStop();

    /// <summary>
    /// Set the bot to stop all movement including turning the gun and radar. The remaining movement is
    /// saved for a call to <see cref="SetResume"/>.
    ///
    /// This method will first be executed when <see cref="IBaseBot.Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="IBaseBot.Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="IBaseBot.Go"/>.
    /// </summary>
    /// <param name="overwrite">overwrite is set to <c>true</c> if the movement saved by a previous call
    /// to this method or <see cref="SetStop()"/> must be overridden with the current movement.
    /// When set to <c>false</c> this method is identical to <see cref="SetStop()"/>.</param>
    /// <seealso cref="IBot.Stop()"/>
    /// <seealso cref="SetResume"/>
    /// <seealso cref="IBot.Resume"/>
    void SetStop(bool overwrite);

    /// <summary>
    /// Sets the bot to scan (again) with the radar. This method is useful if the radar has not been
    /// turning and thereby will not be able to automatically scan bots. This method is useful when the
    /// bot movement has stopped, e.g. when <see cref="IBot.Stop()"/> has been called. The last radar direction
    /// and sweep angle will be used for rescanning for bots.
    ///
    /// This method will first be executed when <see cref="IBaseBot.Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="IBaseBot.Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="IBaseBot.Go"/>.
    /// </summary>
    /// <seealso cref="SetStop()"/>
    /// <seealso cref="SetStop(bool)"/>
    /// <seealso cref="IBot.Stop()"/>
    /// <seealso cref="IBot.Stop(bool)"/>
    /// <seealso cref="IBot.Resume"/>
    void SetResume();

    /// <summary>
    /// Checks if the movement has been stopped.
    /// </summary>
    /// <value><c>true</c> if the movement has been stopped by by <see cref="IBot.Stop()"/> or <see cref="SetStop()"/>;
    /// <c>false</c> otherwise.</value>
    /// <seealso cref="IBot.Stop()"/>
    /// <seealso cref="IBot.Stop(bool)"/>
    /// <seealso cref="SetStop()"/>
    /// <seealso cref="SetStop(bool)"/>
    bool IsStopped { get; }

    /// <summary>
    /// Returns the ids of all teammates.
    /// </summary>
    /// <returns>The ids of all teammates if the bot is participating in a team or an empty collection if the bot is not
    /// in a team.</returns>
    /// <seealso cref="IsTeammate"/>
    /// <seealso cref="SendTeamMessage"/>
    ICollection<int> TeammateIds { get; }

    /// <summary>
    /// Checks if the provided bot id is a teammate or not.
    /// </summary>
    /// <example>
    /// Example:
    /// <code>
    /// public override void OnScannedBot(ScannedBotEvent e)
    /// {
    ///     if (IsTeammate(e.ScannedBotId)
    ///     {
    ///         return; // don't do anything by leaving
    ///     }
    ///     Fire(1);
    /// }
    /// </code>
    /// </example>
    /// <param name="botId">The id of the bot to check for</param>
    /// <returns><c>true</c> if the provided is id an id of a teammate; <c>false</c> otherwise.</returns>  
    /// <seealso cref="TeammateIds"/>
    /// <seealso cref="SendTeamMessage"/>
    bool IsTeammate(int botId);

    /// <summary>
    /// Broadcasts a message to all teammates.
    ///
    /// When the message is send, it is serialized into a JSON representation, meaning that all public fields, and only
    /// public fields, are being serialized into a JSON representation as a DTO (data transfer object).
    ///
    /// The maximum team message size limit is defined by <see cref="TeamMessageMaxSize"/>. This size is the size of the
    /// message when it is serialized into a JSON representation.
    ///
    /// The maximum number of messages that can be send/broadcast per turn is defined by
    /// <see cref="MaxNumberOfTeamMessagesPerTurn"/>.
    /// </summary>
    /// <param name="message">The message to broadcast.</param>
    /// <exception cref="ArgumentException">if the size of the message exceeds the size limit.</exception>
    /// <seealso cref="SendTeamMessage"/>
    /// <seealso cref="TeammateIds"/>
    void BroadcastTeamMessage(object message);

    /// <summary>
    /// Sends a message to a specific teammate.
    ///
    /// When the message is send, it is serialized into a JSON representation, meaning that all public fields, and only
    /// public fields, are being serialized into a JSON representation as a DTO (data transfer object).
    ///
    /// The maximum team message size limit is defined by <see cref="TeamMessageMaxSize"/>. This size is the size of the
    /// message when it is serialized into a JSON representation.
    ///
    /// The maximum number of messages that can be send/broadcast per turn is defined by
    /// <see cref="MaxNumberOfTeamMessagesPerTurn"/>.
    /// </summary>
    /// <param name="teammateId">The id of the teammate to send the message to.</param>
    /// <param name="message">The message to broadcast.</param>
    /// <exception cref="ArgumentException">if the size of the message exceeds the size limit.</exception>
    /// <seealso cref="BroadcastTeamMessage"/>
    /// <seealso cref="TeammateIds"/>
    void SendTeamMessage(int teammateId, object message);

    /// <summary>
    /// The color of the body. Colors can (only) be changed each turn.
    /// </summary>
    /// <example>
    /// BodyColor = Color.RED; // the red color
    /// BodyColor = new Color(255, 0, 0); // also the red color
    /// </example>
    /// <value>Is the color of the body or <c>null</c> if the bot must use the default color.</value>
    Color? BodyColor { get; set; }

    /// <summary>
    /// The color of the gun turret. Colors can (only) be changed each turn.
    /// </summary>
    /// <example>
    /// TurretColor = Color.RED; // the red color
    /// TurretColor = new Color(255, 0, 0); // also the red color
    /// </example>
    /// <value>Is the color of the gun turret or <c>null</c> if the bot must use the default color.</value>
    Color? TurretColor { get; set; }

    /// <summary>
    /// The color of the radar. Colors can (only) be changed each turn.
    /// </summary>
    /// <example>
    /// RadarColor = Color.RED; // the red color
    /// RadarColor = new Color(255, 0, 0); // also the red color
    /// </example>
    /// <value>Is the color of the radar or <c>null</c> if the bot must use the default color.</value>
    Color? RadarColor { get; set; }

    /// <summary>
    /// The color of the fired bullets. Colors can (only) be changed each turn.
    ///
    /// Note that a fired bullet will not change is color when it has been fired. But new bullets fired
    /// after setting the bullet color will get the new color.
    /// </summary>
    /// <example>
    /// BulletColor = Color.RED; // the red color
    /// BulletColor = new Color(255, 0, 0); // also the red color
    /// </example>
    /// <value>Is the color of the fired bullets or <c>null</c> if the bot must use the default color.</value>
    Color? BulletColor { get; set; }

    /// <summary>
    /// The color of the scan arc. Colors can (only) be changed each turn.
    /// </summary>
    /// <example>
    /// ScanColor = Color.RED; // the red color
    /// ScanColor = new Color(255, 0, 0); // also the red color
    /// </example>
    /// <value>Is the color of the scan arc or <c>null</c> if the bot must use the default color.</value>
    Color? ScanColor { get; set; }

    /// <summary>
    /// The color of the tracks. Colors can (only) be changed each turn.
    /// </summary>
    /// <example>
    /// TracksColor = Color.RED; // the red color
    /// TracksColor = new Color(255, 0, 0); // also the red color
    /// </example>
    /// <value>Is the color of the tracks or <c>null</c> if the bot must use the default color.</value>
    Color? TracksColor { get; set; }

    /// <summary>
    /// The color of the gun. Colors can (only) be changed each turn.
    /// </summary>
    /// <example>
    /// GunColor = Color.RED; // the red color
    /// GunColor = new Color(255, 0, 0); // also the red color
    /// </example>
    /// <value>Is the color of the gun or <c>null</c> if the bot must use the default color.</value>
    Color? GunColor { get; set; }

    /// <summary>
    /// Flag indicating if graphical debugging is enabled and hence if <see cref="Graphics"/> can be used for debug
    /// painting.
    /// </summary>
    /// <returns>
    /// <c>true</c> if the graphics debugging is enabled; <c>false</c> otherwise.
    /// </returns>
    bool IsDebuggingEnabled { get; }

    /// <summary>
    /// Graphics object that the bot can paint debug information to.
    /// </summary>
    /// <example>
    /// var g = Graphics;
    /// g.FillRectangle(Brushes.Blue, 50, 50, 100, 100);
    /// </example>
    /// <value>A graphics canvas to use for painting graphical objects making debugging easier.</value>
    IGraphics Graphics { get; }

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
    /// The event handler triggered when a new round has started.
    /// </summary>
    /// <param name="roundStartedEvent">Event details from the game.</param>
    void OnRoundStarted(RoundStartedEvent roundStartedEvent);

    /// <summary>
    /// The event handler triggered when a round has ended.
    /// </summary>
    /// <param name="roundEndedEvent">Event details from the game.</param>
    void OnRoundEnded(RoundEndedEvent roundEndedEvent);

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
    /// <param name="deathEvent">Event details from the game.</param>
    void OnDeath(DeathEvent deathEvent);

    /// <summary>
    /// The event handler triggered when the bot has collided with another bot.
    /// </summary>
    /// <param name="botHitBotEvent">Event details from the game.</param>
    void OnHitBot(HitBotEvent botHitBotEvent);

    /// <summary>
    /// The event handler triggered when the bot has hit a wall.
    /// </summary>
    /// <param name="botHitWallEvent">Event details from the game.</param>
    void OnHitWall(HitWallEvent botHitWallEvent);

    /// <summary>
    /// The event handler triggered when the bot has fired a bullet.
    /// </summary>
    /// <param name="bulletFiredEvent">Event details from the game.</param>
    void OnBulletFired(BulletFiredEvent bulletFiredEvent);

    /// <summary>
    /// The event handler triggered when the bot has been hit by a bullet.
    /// </summary>
    /// <param name="hitByBulletEvent">Event details from the game.</param>
    void OnHitByBullet(HitByBulletEvent hitByBulletEvent);

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
    /// The event handler triggered when some condition has been met. Use the <see cref="Condition.Name"/>
    /// of the condition when you need to differentiate between different types of conditions received
    /// with this event handler.
    /// </summary>
    /// <param name="customEvent">Event details from the game.</param>
    void OnCustomEvent(CustomEvent customEvent);

    /// <summary>
    /// The event handler triggered when the bot has received a message from a teammate.
    /// </summary>
    /// <param name="teamMessageEvent">Event details from the game.</param>
    void OnTeamMessage(TeamMessageEvent teamMessageEvent);

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
    /// bearing = CalcBearing(direction) = NormalizeRelativeAngle(direction - this.Direction)
    /// </code>
    /// <param name="direction">Is the input direction to calculate the bearing from.</param>
    /// <returns>A bearing (delta angle) between the input direction and the direction of this bot.
    /// The bearing is a normalized angle in the range [-180,180[</returns>
    /// <seealso cref="Direction"/>
    /// <seealso cref="NormalizeRelativeAngle"/>
    double CalcBearing(double direction);

    /// <summary>
    /// Calculates the bearing (delta angle) between the input direction and the direction of the gun.
    /// </summary>
    /// <code>
    /// bearing = CalcGunBearing(direction) = NormalizeRelativeAngle(direction - this.GunDirection)
    /// </code>
    /// <param name="direction">Is the input direction to calculate the bearing from.</param>
    /// <returns>A bearing (delta angle) between the input direction and the direction of the gun.
    /// The bearing is a normalized angle in the range [-180,180[</returns>
    /// <seealso cref="GunDirection"/>
    /// <seealso cref="NormalizeRelativeAngle"/>
    double CalcGunBearing(double direction);

    /// <summary>
    /// Calculates the bearing (delta angle) between the input direction and the direction of the radar.
    /// </summary>
    /// <code>
    /// bearing = CalcRadarBearing(direction) = NormalizeRelativeAngle(direction - this.RadarDirection)
    /// </code>
    /// <param name="direction">Is the input direction to calculate the bearing from.</param>
    /// <returns>A bearing (delta angle) between the input direction and the direction of the radar.
    /// The bearing is a normalized angle in the range [-180,180[</returns>
    /// <seealso cref="RadarDirection"/>
    /// <seealso cref="NormalizeRelativeAngle"/>
    double CalcRadarBearing(double direction);

    /// <summary>
    /// Calculates the direction (angle) from the bot´s coordinates to a point x,y.
    /// </summary>
    /// <param name="x">Is the x coordinate of the point.</param>
    /// <param name="y">Is the y coordinate of the point.</param>
    /// <returns>The direction to the point x,y in the range [0,360[</returns>
    double DirectionTo(double x, double y);

    /// <summary>
    /// Calculates the bearing (delta angle) between the bot´s coordinates and direction and the
    /// direction to the point x,y.
    /// </summary>
    /// <param name="x">Is the x coordinate of the point.</param>
    /// <param name="y">Is the y coordinate of the point.</param>
    /// <returns>The bearing to the point x,y in the range [-180,180[</returns>
    double BearingTo(double x, double y);

    /// <summary>
    /// Calculates the bearing (delta angle) between the bot´s gun and direction and the
    /// direction to the point x,y.
    /// </summary>
    /// <param name="x">Is the x coordinate of the point.</param>
    /// <param name="y">Is the y coordinate of the point.</param>
    /// <returns>The bearing to the point x,y in the range [-180,180[</returns>
    double GunBearingTo(double x, double y);

    /// <summary>
    /// Calculates the bearing (delta angle) between the bot´s radar and direction and the
    /// direction to the point x,y.
    /// </summary>
    /// <param name="x">Is the x coordinate of the point.</param>
    /// <param name="y">Is the y coordinate of the point.</param>
    /// <returns>The bearing to the point x,y in the range [-180,180[</returns>
    double RadarBearingTo(double x, double y);

    /// <summary>
    /// Calculates the distance from the bot´s coordinates to a point x,y.
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
    double NormalizeAbsoluteAngle(double angle);

    /// <summary>
    /// Normalizes an angle to an relative angle into the range [-180,180[
    /// </summary>
    /// <param name="angle">Is the angle to normalize.</param>
    /// <returns>The normalized relative angle.</returns>
    double NormalizeRelativeAngle(double angle);

    /// <summary>
    /// Calculates the difference between two angles, i.e. the number of degrees from a source
    /// angle to a target angle. The delta angle will be in the range [-180,180]
    /// </summary>
    /// <param name="targetAngle">Is the target angle.</param>
    /// <param name="sourceAngle">Is the source angle.</param>
    /// <returns>The delta angle between a source angle and target angle.</returns>
    double CalcDeltaAngle(double targetAngle, double sourceAngle);

    /// <summary>
    /// Returns the event priority for a specific event class.
    /// </summary>
    /// <example>
    /// Example:
    /// <code>
    ///     int scannedBotEventPriority = GetPriority(ScannedBotEvent.GetType());
    /// </code>
    /// </example>
    /// <param name="eventType">Event type to get the event priority for.</param>
    /// <returns>The event priority for a specific event class.</returns>
    /// <see cref="DefaultEventPriority"/>
    /// <see cref="SetEventPriority"/>
    int GetEventPriority(Type eventType);

    /// <summary>
    /// Changes the event priority for an event class. The event priority is used for determining which event types
    /// (classes) that must be fired and handled before others. Events with higher priorities will be handled before
    /// events with lower priorities.
    /// </summary>
    /// <note>
    /// You should normally not need to change the event priority.
    /// </note>
    /// <param name="eventType">Event type to change the event priority for.</param>
    /// <param name="priority">The new priority. Typically, a positive number from 1 to 150. The greater value,
    /// the higher priority.</param>
    /// <see cref="DefaultEventPriority"/>
    /// <see cref="GetEventPriority"/>
    void SetEventPriority(Type eventType, int priority);
}