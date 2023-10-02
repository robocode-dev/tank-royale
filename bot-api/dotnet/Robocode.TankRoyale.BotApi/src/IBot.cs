using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Interface for a bot that extends the core API with convenient methods for movement, turning,
/// and firing the gun.
/// </summary>
public interface IBot : IBaseBot
{
    /// <summary>
    /// The Run() method is used for running a program for the bot like:
    ///
    /// <code>
    /// public void Run()
    /// {
    ///   while (IsRunning)
    ///   {
    ///     Forward(100);
    ///     TurnGunLeft(360);
    ///     Back(100);
    ///     TurnGunRight(360);
    ///   }
    /// }
    /// </code>
    ///
    /// <note>
    /// The program runs in a loop in this example (as long as the bot is running),
    /// meaning that it will start moving forward as soon as <see cref="TurnGunRight"/>
    /// has executed.
    /// </note>
    ///
    /// When running a loop that could potentially run forever. The best practice is to check if
    /// the bot is still running to stop and exit the loop. This gives the game a chance of
    /// stopping the thread running the loop in the code behind. If the thread is not stopped
    /// correctly, the bot may behave strangely in new rounds.
    /// </summary>
    /// <seealso cref="IsRunning"/>
    void Run()
    {
    }

    /// <summary>
    /// Checks if this bot is running.
    /// </summary>
    /// <value><c>true</c> when the bot is running, <c>false</c> otherwise.</value>
    bool IsRunning { get; }

    /// <summary>
    /// Set the bot to move forward until it has traveled a specific distance from its current
    /// position, or it is moving into an obstacle. The speed is limited by <see
    /// cref="IBaseBot.MaxSpeed"/>.
    ///
    /// When the bot is moving forward, the <see cref="Constants.Acceleration"/> determines the acceleration
    /// of the bot that adds 1 additional unit to the speed per turn while accelerating. However,
    /// the bot is faster at braking. The <see cref="Constants.Deceleration"/> determines the deceleration of
    /// the bot that subtracts 2 units from the speed per turn.
    ///
    /// This method will first be executed when <see cref="IBaseBot.Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="IBaseBot.Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="IBaseBot.Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="IBaseBot.Go"/> is executed,
    /// counts.
    ///
    /// This method will cancel the effect of prior setting <see cref="IBaseBot.TargetSpeed"/> as the
    /// <see cref="SetForward"/> and <see cref="SetBack"/> methods sets the <see cref="IBaseBot.TargetSpeed"/>
    /// for each turn until <see cref="DistanceRemaining"/> reaches 0.
    /// </summary>
    /// <param name="distance">Is the distance to move forward. If negative, the bot will move backward.
    /// If <see cref="double.PositiveInfinity"/> the bot will move forward infinitely.
    /// If <see cref="double.NegativeInfinity"/> the bot will move backward infinitely.
    /// </param>
    /// <seealso cref="Forward"/>
    /// <seealso cref="SetBack"/>
    /// <seealso cref="Back"/>
    /// <seealso cref="DistanceRemaining"/>
    /// <seealso cref="IBaseBot.TargetSpeed"/>
    void SetForward(double distance);

    /// <summary>
    /// Moves the bot forward until it has traveled a specific distance from its current position,
    /// or it is moving into an obstacle. The speed is limited by <see cref="IBaseBot.MaxSpeed"/>.
    ///
    /// When the bot is moving forward, the <see cref="Constants.Acceleration"/> determine the acceleration
    /// of the bot that adds 1 additional unit to the speed per turn while accelerating. However,
    /// the bot is faster at braking. The <see cref="Constants.Deceleration"/> determines the deceleration of
    /// the bot that subtracts 2 units from the speed per turn.
    ///
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="IBaseBot.TargetSpeed"/>, <see
    /// cref="SetForward"/>, and <see cref="SetBack"/> methods.
    /// </summary>
    /// <param name="distance">Is the distance to move forward. If negative, the bot will move backward.
    /// If <see cref="double.PositiveInfinity"/> the bot will move forward infinitely.
    /// If <see cref="double.NegativeInfinity"/> the bot will move backward infinitely.
    /// </param>
    /// <seealso cref="SetForward"/>
    /// <seealso cref="SetBack"/>
    /// <seealso cref="Back"/>
    /// <seealso cref="DistanceRemaining"/>
    /// <seealso cref="IBaseBot.TargetSpeed"/>
    void Forward(double distance);

    /// <summary>
    /// Set the bot to move backward until it has traveled a specific distance from its current
    /// position, or it is moving into an obstacle. The speed is limited by <see cref="IBaseBot.MaxSpeed"/>
    ///
    /// When the bot is moving forward, the <see cref="Constants.Acceleration"/> determines the acceleration of
    /// the bot that adds 1 additional unit to the speed per turn while accelerating. However, the bot
    /// is faster at braking. The <see cref="Constants.Deceleration"/> determines the deceleration of the bot that
    /// subtracts 2 units from the speed per turn.
    ///
    /// This method will first be executed when <see cref="IBaseBot.Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="IBaseBot.Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="IBaseBot.Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="IBaseBot.Go"/> is executed,
    /// counts.
    ///
    /// This method will cancel the effect of prior setting <see cref="IBaseBot.TargetSpeed"/> as the
    /// <see cref="SetForward"/> and <see cref="SetBack"/> methods sets the <see cref="IBaseBot.TargetSpeed"/>
    /// for each turn until <see cref="DistanceRemaining"/> reaches 0.
    /// </summary>
    /// <param name="distance">Is the distance to move backward. If negative, the bot will move forward.
    /// If <see cref="double.PositiveInfinity"/> the bot will move backward infinitely.
    /// If <see cref="double.NegativeInfinity"/> the bot will move forward infinitely.
    /// </param>
    /// <seealso cref="Back"/>
    /// <seealso cref="SetForward"/>
    /// <seealso cref="Forward"/>
    /// <seealso cref="DistanceRemaining"/>
    /// <seealso cref="IBaseBot.TargetSpeed"/>
    void SetBack(double distance);

    /// <summary>
    /// Moves the bot backward until it has traveled a specific distance from its current position, or
    /// it is moving into an obstacle. The speed is limited by <see cref="IBaseBot.MaxSpeed"/>.
    ///
    /// When the bot is moving forward, the <see cref="Constants.Acceleration"/> determines the acceleration of
    /// the bot that adds 1 additional unit to the speed per turn while accelerating. However, the bot
    /// is faster at braking. The <see cref="Constants.Deceleration"/> determines the deceleration of the bot that
    /// subtracts 2 units from the speed per turn.
    ///
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="IBaseBot.TargetSpeed"/>, <see
    /// cref="SetForward"/>, and <see cref="SetBack"/> methods.
    /// </summary>
    /// <param name="distance">Is the distance to move backward. If negative, the bot will move forward.
    /// If <see cref="double.PositiveInfinity"/> the bot will move backward infinitely.
    /// If <see cref="double.NegativeInfinity"/> the bot will move forward infinitely.
    /// </param>
    /// <seealso cref="SetForward"/>
    /// <seealso cref="SetBack"/>
    /// <seealso cref="Forward"/>
    /// <seealso cref="DistanceRemaining"/>
    /// <seealso cref="IBaseBot.TargetSpeed"/>
    void Back(double distance);

    /// <summary>
    /// The distance remaining till the bot has finished moving after having called
    /// <see cref="SetForward"/>, <see cref="SetBack"/>, <see cref="Forward"/>, or <see cref="Back"/>
    /// When the distance remaining has reached 0, the bot has finished its current move.
    ///
    /// When the distance remaining is positive, the bot is moving forward. When the distance
    /// remaining is negative, the bot is moving backward.
    /// </summary>
    /// <value>The remaining distance to move before its current movement is completed.
    /// If <see cref="double.PositiveInfinity"/> the bot will move forward infinitely.
    /// If <see cref="double.NegativeInfinity"/> the bot will move backward infinitely.
    /// </value>
    /// <seealso cref="SetForward"/>
    /// <seealso cref="SetBack"/>
    /// <seealso cref="Forward"/>
    /// <seealso cref="Back"/>
    double DistanceRemaining { get; }

    /// <summary>
    /// Set the bot to turn to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="TurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="IBaseBot.MaxTurnRate"/>.
    ///
    /// This method will first be executed when <see cref="IBaseBot.Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="IBaseBot.Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="IBaseBot.Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="IBaseBot.Go"/> is executed,
    /// counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnRight"/>.
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn left. If negative, the bot will turn right.
    /// If <see cref="double.PositiveInfinity"/> the bot will turn left infinitely.
    /// If <see cref="double.NegativeInfinity"/> the bot will turn right infinitely.
    /// </param>
    /// <seealso cref="SetTurnRight"/>
    /// <seealso cref="TurnRight"/>
    /// <seealso cref="TurnLeft"/>
    /// <seealso cref="TurnRemaining"/>
    /// <seealso cref="IBaseBot.TurnRate"/>
    void SetTurnLeft(double degrees);

    /// <summary>
    /// Turn the bot to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="TurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="IBaseBot.MaxTurnRate"/>.
    ///
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnLeft"/> and
    /// <see cref="SetTurnRight"/>
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn left. If negative, the bot will turn right.
    /// If <see cref="double.PositiveInfinity"/> the bot will turn left infinitely.
    /// If <see cref="double.NegativeInfinity"/> the bot will turn right infinitely.
    /// </param>
    /// <seealso cref="SetTurnLeft"/>
    /// <seealso cref="SetTurnRight"/>
    /// <seealso cref="TurnRight"/>
    /// <seealso cref="TurnRemaining"/>
    /// <seealso cref="IBaseBot.TurnRate"/>
    void TurnLeft(double degrees);

    /// <summary>
    /// Set the bot to turn to the right (following the decreasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="TurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="IBaseBot.MaxTurnRate"/>.
    ///
    /// This method will first be executed when <see cref="IBaseBot.Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="IBaseBot.Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="IBaseBot.Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="IBaseBot.Go"/> is executed,
    /// counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnLeft"/>.
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn right. If negative, the bot will turn left.
    /// If <see cref="double.PositiveInfinity"/> the bot will turn right infinitely.
    /// If <see cref="double.NegativeInfinity"/> the bot will turn left infinitely.
    /// </param>
    /// <seealso cref="SetTurnLeft"/>
    /// <seealso cref="TurnRight"/>
    /// <seealso cref="TurnLeft"/>
    /// <seealso cref="TurnRemaining"/>
    /// <seealso cref="IBaseBot.TurnRate"/>
    void SetTurnRight(double degrees);

    /// <summary>
    /// Turn the bot to the right (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="TurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="IBaseBot.MaxTurnRate"/>.
    ///
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnLeft"/> and
    /// <see cref="SetTurnRight"/>
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn right. If negative, the bot will turn left.
    /// If <see cref="double.PositiveInfinity"/> the bot will turn right infinitely.
    /// If <see cref="double.NegativeInfinity"/> the bot will turn left infinitely.
    /// </param>
    /// <seealso cref="SetTurnLeft"/>
    /// <seealso cref="SetTurnRight"/>
    /// <seealso cref="TurnLeft"/>
    /// <seealso cref="TurnRemaining"/>
    /// <seealso cref="IBaseBot.TurnRate"/>
    void TurnRight(double degrees);

    /// <summary>
    /// The remaining turn in degrees till the bot has finished turning after having called
    /// <see cref="SetTurnLeft"/>, <see cref="SetTurnRight"/>, <see cref="TurnLeft"/>, or
    /// <see cref="TurnRight"/>. When the turn remaining has reached 0, the bot has finished
    /// turning.
    ///
    /// When the turn remaining is positive, the bot is turning to the left (along the unit circle).
    /// When the turn remaining is negative, the bot is turning to the right.
    /// </summary>
    /// <value>The remaining degrees to turn before its current turning is completed.
    /// If <see cref="double.PositiveInfinity"/> the bot will turn left infinitely.
    /// If <see cref="double.NegativeInfinity"/> the bot will turn right infinitely.
    /// </value>
    /// <seealso cref="SetTurnLeft"/>
    /// <seealso cref="SetTurnRight"/>
    /// <seealso cref="TurnLeft"/>
    /// <seealso cref="TurnRight"/>
    double TurnRemaining { get; }

    /// <summary>
    /// Set the gun to turn to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="GunTurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="Constants.MaxGunTurnRate"/>.
    ///
    /// This method will first be executed when <see cref="IBaseBot.Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="IBaseBot.Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="IBaseBot.Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="IBaseBot.Go"/> is executed,
    /// counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnGunRight"/>.
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn left. If negative, the gun will turn right.
    /// If <see cref="double.PositiveInfinity"/> the gun will turn left infinitely.
    /// If <see cref="double.NegativeInfinity"/> the gun will turn right infinitely.
    /// </param>
    /// <seealso cref="SetTurnGunRight"/>
    /// <seealso cref="TurnGunRight"/>
    /// <seealso cref="SetTurnRight"/>
    /// <seealso cref="GunTurnRemaining"/>
    /// <seealso cref="IBaseBot.GunTurnRate"/>
    void SetTurnGunLeft(double degrees);

    /// <summary>
    /// Turn the gun to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="GunTurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="Constants.MaxGunTurnRate"/>.
    ///
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnGunLeft"/> and
    /// <see cref="SetTurnGunRight"/>
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn left. If negative, the gun will turn right.
    /// If <see cref="double.PositiveInfinity"/> the gun will turn left infinitely.
    /// If <see cref="double.NegativeInfinity"/> the gun will turn right infinitely.
    /// </param>
    /// <seealso cref="SetTurnGunLeft"/>
    /// <seealso cref="SetTurnGunRight"/>
    /// <seealso cref="TurnGunRight"/>
    /// <seealso cref="GunTurnRemaining"/>
    /// <seealso cref="IBaseBot.GunTurnRate"/>
    void TurnGunLeft(double degrees);

    /// <summary>
    /// Set the gun to turn to the right (following the decreasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="GunTurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="Constants.MaxGunTurnRate"/>.
    ///
    /// This method will first be executed when <see cref="IBaseBot.Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="IBaseBot.Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="IBaseBot.Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="IBaseBot.Go"/> is executed,
    /// counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnGunLeft"/>.
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn right. If negative, the gun will turn left.
    /// If <see cref="double.PositiveInfinity"/> the gun will turn right infinitely.
    /// If <see cref="double.NegativeInfinity"/> the gun will turn left infinitely.
    /// </param>
    /// <seealso cref="SetTurnGunLeft"/>
    /// <seealso cref="TurnGunRight"/>
    /// <seealso cref="TurnGunLeft"/>
    /// <seealso cref="GunTurnRemaining"/>
    /// <seealso cref="IBaseBot.GunTurnRate"/>
    void SetTurnGunRight(double degrees);

    /// <summary>
    /// Turn the gun to the right (following the decreasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="GunTurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="Constants.MaxGunTurnRate"/>.
    ///
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnGunLeft"/> and
    /// <see cref="SetTurnGunRight"/>.
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn right. If negative, the gun will turn left.
    /// If <see cref="double.PositiveInfinity"/> the gun will turn right infinitely.
    /// If <see cref="double.NegativeInfinity"/> the gun will turn left infinitely.
    /// </param>
    /// <seealso cref="SetTurnGunLeft"/>
    /// <seealso cref="SetTurnGunRight"/>
    /// <seealso cref="TurnGunLeft"/>
    /// <seealso cref="GunTurnRemaining"/>
    /// <seealso cref="IBaseBot.GunTurnRate"/>W
    void TurnGunRight(double degrees);

    /// <summary>
    /// The remaining turn in degrees till the gun has finished turning after having called
    /// <see cref="SetTurnGunLeft"/>, <see cref="SetTurnGunRight"/>, <see cref="TurnGunLeft"/>, or
    /// <see cref="TurnGunRight"/>". When the turn remaining has reached 0, the gun has finished turning.
    ///
    /// When the turn remaining is positive, the bot is turning to the left (along the unit circle).
    /// When the turn remaining is negative, the bot is turning to the right.
    /// </summary>
    /// <value>The remaining degrees to turn the gun before its current turning is completed.
    /// If <see cref="double.PositiveInfinity"/> the gun will turn left infinitely.
    /// If <see cref="double.NegativeInfinity"/> the gun will turn right infinitely.
    /// </value>
    /// <seealso cref="SetTurnGunLeft"/>
    /// <seealso cref="SetTurnGunRight"/>
    /// <seealso cref="TurnGunLeft"/>
    /// <seealso cref="TurnGunRight"/>
    double GunTurnRemaining { get; }

    /// <summary>
    /// Set the radar to turn to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="RadarTurnRemaining"/> is 0. The amount of degrees
    /// to turn each turn is limited by <see cref="IBaseBot.MaxRadarTurnRate"/>.
    ///
    /// This method will first be executed when <see cref="IBaseBot.Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="IBaseBot.Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="IBaseBot.Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="IBaseBot.Go"/> is executed,
    /// counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnRadarRight"/>.
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn left. If negative, the radar will turn right.
    /// If <see cref="double.PositiveInfinity"/> the radar will turn left infinitely.
    /// If <see cref="double.NegativeInfinity"/> the radar will turn right infinitely.
    /// </param>
    /// <seealso cref="SetTurnRadarRight"/>
    /// <seealso cref="TurnRadarRight"/>
    /// <seealso cref="TurnRadarLeft"/>
    /// <seealso cref="RadarTurnRemaining"/>
    /// <seealso cref="IBaseBot.RadarTurnRate"/>
    void SetTurnRadarLeft(double degrees);

    /// <summary>
    /// Turn the radar to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="RadarTurnRemaining"/> is 0. The amount of degrees
    /// to turn each turn is limited by <see cref="IBaseBot.MaxRadarTurnRate"/>.
    ///
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnRadarLeft"/> and
    /// <see cref="SetTurnRadarRight"/>.
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn left. If negative, the radar will turn right.
    /// If <see cref="double.PositiveInfinity"/> the radar will turn left infinitely.
    /// If <see cref="double.NegativeInfinity"/> the radar will turn right infinitely.
    /// </param>
    /// <seealso cref="SetTurnRadarLeft"/>
    /// <seealso cref="SetTurnRadarRight"/>
    /// <seealso cref="TurnRadarLeft"/>
    /// <seealso cref="RadarTurnRemaining"/>
    /// <seealso cref="IBaseBot.RadarTurnRate"/>
    void TurnRadarLeft(double degrees);

    /// <summary>
    /// Turn the radar to the right (following the decreasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="RadarTurnRemaining"/> is 0. The amount of degrees
    /// to turn each turn is limited by <see cref="IBaseBot.MaxRadarTurnRate"/>.
    ///
    /// This method will first be executed when <see cref="IBaseBot.Go"/> is called making it possible to
    /// call other set methods before execution. This makes it possible to set the bot to move,
    /// turn the body, radar, gun, and also fire the gun in parallel in a single turn when calling
    /// <see cref="IBaseBot.Go"/>. But notice that this is only possible to execute multiple methods in
    /// parallel by using <em>setter</em> methods only prior to calling <see cref="IBaseBot.Go"/>.
    ///
    /// If this method is called multiple times, the last call before <see cref="IBaseBot.Go"/> is executed,
    /// counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnRadarLeft"/> and
    /// <see cref="SetTurnRadarRight"/>.
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn right. If negative, the radar will turn left.
    /// If <see cref="double.PositiveInfinity"/> the radar will turn right infinitely.
    /// If <see cref="double.NegativeInfinity"/> the radar will turn left infinitely.
    /// </param>
    /// <seealso cref="SetTurnRadarLeft"/>
    /// <seealso cref="TurnRadarLeft"/>
    /// <seealso cref="TurnRadarRight"/>
    /// <seealso cref="RadarTurnRemaining"/>
    /// <seealso cref="IBaseBot.RadarTurnRate"/>
    void SetTurnRadarRight(double degrees);

    /// <summary>
    /// Turn the radar to the right (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="RadarTurnRemaining"/> is 0. The amount of degrees
    /// to turn each turn is limited by <see cref="IBaseBot.MaxRadarTurnRate"/>.
    ///
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnRadarLeft"/> and
    /// <see cref="SetTurnRadarRight"/>.
    /// </summary>
    /// <param name="degrees">Is the amount of degrees to turn right. If negative, the radar will turn left.
    /// If <see cref="double.PositiveInfinity"/> the radar will turn right infinitely.
    /// If <see cref="double.NegativeInfinity"/> the radar will turn left infinitely.
    /// </param>
    /// <seealso cref="SetTurnRadarLeft"/>
    /// <seealso cref="SetTurnRadarRight"/>
    /// <seealso cref="TurnRadarRight"/>
    /// <seealso cref="RadarTurnRemaining"/>
    /// <seealso cref="IBaseBot.RadarTurnRate"/>
    void TurnRadarRight(double degrees);

    /// <summary>
    /// The remaining turn in degrees till the radar has finished turning after having called
    /// <see cref="SetTurnRadarLeft"/>, <see cref="SetTurnRadarRight"/>, <see cref="TurnRadarLeft"/>, or
    /// <see cref="TurnRadarRight"/>. When the turn remaining has reached 0, the radar has finished turning.
    ///
    /// When the turn remaining is positive, the bot is turning to the left (along the unit circle).
    /// When the turn remaining is negative, the bot is turning to the right.
    /// </summary>
    /// <value>The remaining degrees to turn the radar before its current turning is completed.
    /// If <see cref="double.PositiveInfinity"/> the radar will turn left infinitely.
    /// If <see cref="double.NegativeInfinity"/> the radar will turn right infinitely.
    /// </value>
    /// <seealso cref="SetTurnRadarLeft"/>
    /// <seealso cref="SetTurnRadarRight"/>
    /// <seealso cref="TurnRadarLeft"/>
    /// <seealso cref="TurnRadarRight"/>
    double RadarTurnRemaining { get; }

    /// <summary>
    /// Fire the gun in the direction as the gun is pointing.
    ///
    /// <note>
    /// Your bot is spending energy when firing a bullet, the amount of energy used for firing the
    /// bullet is taken from the bot. The amount of energy loss is equal to the firepower.
    /// </note>
    ///
    /// If the bullet hits an opponent bot, you will gain energy from the bullet hit. When hitting
    /// another bot, your bot will be rewarded and retrieve an energy boost of 3x firepower.
    ///
    /// The gun will only fire when the firepower is at <see cref="Constants.MinFirepower"/> or higher. If
    /// the firepower is more than <see cref="Constants.MaxFirepower"/>, the power will be truncated to the
    /// <see cref="Constants.MaxFirepower"/>.
    ///
    /// Whenever the gun is fired, the gun is heated and needs to cool down before it can fire
    /// again. The gun heat must be zero before the gun can fire (see <see cref="IBaseBot.GunHeat"/>.
    /// The gun heat generated by firing the gun is 1 + (firepower / 5). Hence, the more firepower
    /// used the longer it takes to cool down the gun. The gun cooling rate can be read from <see
    /// cref="IBaseBot.GunCoolingRate"/>.
    ///
    /// The amount of energy used for firing the gun is subtracted from the bot´s total energy. The
    /// amount of damage dealt by a bullet hitting another bot is 4x firepower, and if the firepower is
    /// greater than 1 it will do an additional 2 x (firepower - 1) damage.
    ///
    /// The firepower is truncated to <see cref="Constants.MinFirepower"/> and
    /// <see cref="Constants.MaxFirepower"/> if the firepower exceeds these values.
    ///
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    ///
    /// This method will cancel the effect of prior calls to setting <see cref="IBaseBot.SetFire"/>.
    /// </summary>
    /// <param name="firepower">Is the amount of energy spent on firing the gun. You cannot spend
    /// more energy than available from the bot. The bullet power must be > MinFirepower.</param>
    /// <seealso cref="IBaseBot.OnBulletFired"/>
    /// <seealso cref="IBaseBot.SetFire"/>
    /// <seealso cref="IBaseBot.GunHeat"/>
    /// <seealso cref="IBaseBot.GunCoolingRate"/>
    void Fire(double firepower);

    /// <summary>
    /// Stop all movement including turning the gun and radar. The remaining movement is saved for a
    /// call to <see cref="IBaseBot.SetResume"/> or <see cref="Resume"/>. This method has no effect, if it has already
    /// been called.
    /// 
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    /// </summary>
    /// <seealso cref="Stop"/>
    /// <seealso cref="IBaseBot.SetResume"/>
    /// <seealso cref="Resume"/>
    void Stop();

    /// <summary>
    /// Stop all movement including turning the gun and radar. The remaining movement is saved for a
    /// call to <see cref="IBaseBot.SetResume"/> or <see cref="Resume"/>.
    /// 
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    /// </summary>
    /// <param name="overwrite">overwrite is set to <c>true</c> if the movement saved by a previous call
    /// to this method or <see cref="IBaseBot.SetStop()"/> must be overridden with the current movement.
    /// When set to <c>false</c> this method is identical to <see cref="IBaseBot.SetStop()"/>.</param>
    /// <seealso cref="Stop"/>
    /// <seealso cref="IBaseBot.SetResume"/>
    /// <seealso cref="Resume"/>
    void Stop(bool overwrite);

    /// <summary>
    /// Resume the movement prior to calling the <see cref="IBaseBot.SetStop"/> or <see cref="Stop"/> method. This
    /// method has no effect, if it has already been called.
    /// 
    /// This call is executed immediately by calling <see cref="IBaseBot.Go"/> in the code behind. This
    /// method will block until it has been completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute
    /// multiple commands in parallel, use <em>setter</em> methods instead of this blocking
    /// method.
    /// </summary>
    /// <seealso cref="IBaseBot.SetStop"/>
    /// <seealso cref="IBaseBot.SetResume"/>
    /// <seealso cref="Stop"/>
    void Resume();

    /// <summary>
    /// Scan (again) with the radar. This method is useful if the radar has not been turning and
    /// thereby will not be able to automatically scan bots. This method is useful when the bot
    /// movement has stopped, e.g. when <see cref="Stop"/> has been called. The last radar direction and
    /// sweep angle will be used for rescanning for bots.
    /// </summary>
    /// <seealso cref="Stop"/>
    void Rescan();

    /// <summary>
    /// Blocks until a condition is met, i.e. when a <see cref="Condition.Test"/> returns <c>true</c>.
    /// </summary>
    /// <seealso cref="Condition"/>
    /// <seealso cref="IBaseBot.OnCustomEvent(CustomEvent)"/>
    void WaitFor(Condition condition);
}