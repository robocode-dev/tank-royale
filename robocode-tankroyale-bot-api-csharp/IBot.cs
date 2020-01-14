namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Interface for a bot containing convenient methods for movement, turning, and firing the gun.
  /// </summary>
  public interface IBot : IBaseBot
  {
    /// <summary>
    /// The Run method is used for running a program for the bot like:
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
    /// Note that the program runs in a loop in this example, meaning that it will start moving
    /// forward as soon as TurnGunRight() has executed.
    ///
    /// When running a loop that could potentially "run forever", best practise is to check if the
    /// bot is still running in order to stop and exit the loop. This gives the game a chance of
    /// stopping the thread running the loop in the code behind. If the thread is not stopped
    /// correctly, the bot may behave strange in new rounds.
    /// </summary>
    /// <seealso cref="IsRunning"/>
    void Run() { }


    /// <summary>
    /// Returns true when the bot is running, false otherwise.
    /// </summary>
    bool IsRunning { get; }

    /// <summary>
    /// Set the bot to move forward until it has traveled a specific distance from its current
    /// position, or it is moving into an obstacle. The speed is limited by <see cref="SetMaxSpeed"/>.
    ///
    /// When the bot is moving forward, the <see cref="Acceleration"/> determine the acceleration of
    /// the bot that adds 1 additional pixel to the speed per turn while accelerating. However, the bot
    /// is faster at braking. The <see cref="Deceleration"/> determine the deceleration of the bot that
    /// subtracts 2 pixels from the speed per turn.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go().
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    ///
    /// This method will cancel the effect of prior setting <see cref="TargetSpeed"/> as the
    /// <see cref="SetForward"/> and <see cref="SetBack"/> methods sets the <see cref="TargetSpeed"/>
    /// for each turn until <see cref="DistanceRemaining"/> reaches 0.
    /// </summary>
    /// <param name="distance">Distance is the distance to move forward. If negative, the bot will
    /// move backwards.</param>
    /// <seealso cref="Forward"/>
    /// <seealso cref="SetBack"/>
    /// <seealso cref="Back"/>
    /// <seealso cref="DistanceRemaining"/>
    /// <seealso cref="TargetSpeed"/>
    void SetForward(double distance);

    /// <summary>
    /// Moves the bot forward until it has traveled a specific distance from its current position, or it
    /// is moving into an obstacle. The speed is limited by <see cref="SetMaxSpeed"/>.
    ///
    /// When the bot is moving forward, the <see cref="Acceleration"/> determine the acceleration of
    /// the bot that adds 1 additional pixel to the speed per turn while accelerating. However, the bot
    /// is faster at braking. The <see cref="Deceleration"/> determine the deceleration of the bot that
    /// subtracts 2 pixels from the speed per turn.
    ///
    /// This call is executed immediately be calling Go() in the code behind. This method
    /// will block until its has been completed completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute multiple
    /// commands in parallel, use <strong>setter</strong> methods instead of this blocking method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="TargetSpeed"/>, <see
    /// cref="SetForward"/>, and <see cref="SetBack"/> methods.
    /// </summary>
    /// <param name="distance">Distance is the distance to move forward. If negative, the bot will
    /// move backwards.</param>
    /// <seealso cref="SetForward"/>
    /// <seealso cref="SetBack"/>
    /// <seealso cref="Back"/>
    /// <seealso cref="DistanceRemaining"/>
    /// <seealso cref="TargetSpeed"/>
    void Forward(double distance);

    /// <summary>
    /// Set the bot to move backwards until it has traveled a specific distance from its current
    /// position, or it is moving into an obstacle. The speed is limited by <see cref="SetMaxSpeed"/>
    ///
    /// When the bot is moving forward, the <see cref="Acceleration"/> determine the acceleration of
    /// the bot that adds 1 additional pixel to the speed per turn while accelerating. However, the bot
    /// is faster at braking. The <see cref="Deceleration"/> determine the deceleration of the bot that
    /// subtracts 2 pixels from the speed per turn.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go()
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    ///
    /// This method will cancel the effect of prior setting <see cref="TargetSpeed"/> as the
    /// <see cref="SetForward"/> and <see cref="SetBack"/> methods sets the <see cref="TargetSpeed"/>
    /// for each turn until <see cref="DistanceRemaining"/> reaches 0.
    /// </summary>
    /// <param name="distance">Distance is the distance to move backward. If negative, the bot will
    /// move forward.</param>
    /// <seealso cref="Back"/>
    /// <seealso cref="SetForward"/>
    /// <seealso cref="Forward"/>
    /// <seealso cref="DistanceRemaining"/>
    /// <seealso cref="TargetSpeed"/>
    void SetBack(double distance);

    /// <summary>
    /// Moves the bot backwards until it has traveled a specific distance from its current position, or
    /// it is moving into an obstacle. The speed is limited by <see cref="SetMaxSpeed"/>.
    ///
    /// When the bot is moving forward, the <see cref="Acceleration"/> determine the acceleration of
    /// the bot that adds 1 additional pixel to the speed per turn while accelerating. However, the bot
    /// is faster at braking. The <see cref="Deceleration"/> determine the deceleration of the bot that
    /// subtracts 2 pixels from the speed per turn.
    ///
    /// This call is executed immediately be calling Go() in the code behind. This method
    /// will block until its has been completed completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute multiple
    /// commands in parallel, use <strong>setter</strong> methods instead of this blocking method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="TargetSpeed"/>, <see
    /// cref="SetForward"/>, and <see cref="SetBack"/> methods.
    /// </summary>
    /// <param name="distance">Distance is the distance to move backward. If negative, the bot will
    /// move forward.</param>
    /// <seealso cref="SetForward"/>
    /// <seealso cref="SetBack"/>
    /// <seealso cref="Forward"/>
    /// <seealso cref="DistanceRemaining"/>
    /// <seealso cref="TargetSpeed"/>
    void Back(double distance);

    /// <summary>
    /// The distance remaining till the bot has finished moving after having called
    /// <see cref="SetForward"/>, <see cref="SetBack"/>, <see cref="Forward"/>, or <see cref="Back"/>
    /// When the distance remaining has reached 0, the bot has finished its current move.
    ///
    /// When the distance remaining is positive, the bot is moving forward. When the distance
    /// remaining is negative, the bot is moving backwards.
    /// </summary>
    /// <returns>the remaining distance to move before its current movement is completed.</returns>
    /// <seealso cref="SetForward"/>
    /// <seealso cref="SetBack"/>
    /// <seealso cref="Forward"/>
    /// <seealso cref="Back"/>
    double DistanceRemaining { get; }

    /// <summary>
    /// Sets the maximum speed which applies when moving forward and backward. The maximum speed must
    /// be an absolute value from 0 to MaxSpeed, both values are included. If the input
    /// speed is negative, the max speed will be cut to zero. If the input speed is above
    /// MaxSpeed, the max speed will be set to MaxSpeed.
    ///
    /// If for example the maximum speed is set to 5, then the bot will be able to move backwards
    /// with a speed down to -5 pixels/turn and up to 5 pixels/turn when moving forward.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go().
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    /// </summary>
    /// <param name="maxSpeed">is the new maximum speed</param>
    /// <seealso cref="SetForward"/>
    /// <seealso cref="SetBack"/>
    void SetMaxSpeed(double maxSpeed);

    /// <summary>
    /// Set the bot to turn to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="TurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="SetMaxTurnRate"/>.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go().
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnRight"/>.
    /// </summary>
    /// <param name="degrees">degrees is the amount of degrees to turn left. If negative, the bot
    /// will turn right.</param>
    /// <seealso cref="SetTurnRight"/>
    /// <seealso cref="TurnRight"/>
    /// <seealso cref="TurnLeft"/>
    /// <seealso cref="TurnRemaining"/>
    /// <seealso cref="SetTurnRate"/>
    void SetTurnLeft(double degrees);

    /// <summary>
    /// Turn the bot to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="TurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="SetMaxTurnRate"/>.
    ///
    /// This call is executed immediately be calling Go() in the code behind. This method
    /// will block until its has been completed completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute multiple
    /// commands in parallel, use <strong>setter</strong> methods instead of this blocking method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnLeft"/> and
    /// <see cref="SetTurnRight"/>
    /// </summary>
    /// <param name="degrees">degrees is the amount of degrees to turn left. If negative, the bot
    /// will turn right.</param>
    /// <seealso cref="SetTurnLeft"/>
    /// <seealso cref="SetTurnRight"/>
    /// <seealso cref="TurnRight"/>
    /// <seealso cref="TurnRemaining"/>
    /// <seealso cref="SetTurnRate"/>
    void TurnLeft(double degrees);

    /// <summary>
    /// Set the bot to turn to the right (following the decreasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="TurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="SetMaxTurnRate"/>.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go().
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnLeft"/>.
    /// </summary>
    /// <param name="degrees">degrees is the amount of degrees to turn right. If negative, the bot
    /// will turn left.</param>
    /// <seealso cref="SetTurnLeft"/>
    /// <seealso cref="TurnRight"/>
    /// <seealso cref="TurnLeft"/>
    /// <seealso cref="TurnRemaining"/>
    /// <seealso cref="SetTurnRate"/>
    void SetTurnRight(double degrees);

    /// <summary>
    /// Turn the bot to the right (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="TurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="SetMaxTurnRate"/>.
    ///
    /// This call is executed immediately be calling Go() in the code behind. This method
    /// will block until its has been completed completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute multiple
    /// commands in parallel, use <strong>setter</strong> methods instead of this blocking method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnLeft"/> and
    /// <see cref="SetTurnRight"/>
    /// </summary>
    /// <param name="degrees">degrees is the amount of degrees to turn right. If negative, the bot
    /// will turn left.</param>
    /// <seealso cref="SetTurnLeft"/>
    /// <seealso cref="SetTurnRight"/>
    /// <seealso cref="TurnLeft"/>
    /// <seealso cref="TurnRemaining"/>
    /// <seealso cref="SetTurnRate"/>
    void TurnRight(double degrees);

    /// <summary>
    /// The remaining turn in degrees till the bot has finished turning after having called
    /// <see cref="SetTurnLeft">, <see cref="SetTurnRight"/>, <see cref="TurnLeft"/>, or
    /// <see cref="TurnRight"/>. When the turn remaining has reached 0, the bot has finished
    /// turning.
    ///
    /// When the turn remaining is positive, the bot is turning to the left (along the unit circle).
    /// When the turn remaining is negative, the bot is turning to the right.
    /// </summary>
    /// <returns>the remaining degrees to turn</returns>
    /// <seealso cref="SetTurnLeft"/>
    /// <seealso cref="SetTurnRight"/>
    /// <seealso cref="TurnLeft"/>
    /// <seealso cref="TurnRight"/>
    double TurnRemaining { get; }

    /// <summary>
    /// Sets the maximum turn rate which applies to turning the bot to the left or right. The maximum
    /// turn rate must be an absolute value from 0 to MaxTurnRate, both values are
    /// included. If the input turn rate is negative, the max turn rate will be cut to zero. If the
    /// input turn rate is above MaxTurnRate, the max turn rate will be set to MaxTurnRate.
    ///
    /// If for example the max turn rate is set to 5, then the bot will be able to turn left or
    /// right with a turn rate down to -5 degrees/turn when turning right, and up to 5 degrees/turn
    /// when turning left.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go().
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    /// </summary>
    /// <param name="maxTurnRate"/>is the new maximum turn rate</param>
    /// <seealso cref="SetTurnRate"/>
    /// <seealso cref="SetTurnLeft"/>
    /// <seealso cref="SetTurnRight"/>
    /// <seealso cref="TurnLeft"/>
    /// <seealso cref="TurnRight"/>
    void SetMaxTurnRate(double maxTurnRate);

    /// <summary>
    /// Set the gun to turn to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="GunTurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="SetGunMaxTurnRate"/>.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go().
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnGunRight"/>.
    /// </summary>
    /// <param name="degrees">is the amount of degrees to turn left. If negative, the gun will turn
    /// right.</param>
    /// <seealso cref="SetTurnGunRight"/>
    /// <seealso cref="TurnGunRight"/>
    /// <seealso cref="SetTurnRight"/>
    /// <seealso cref="GunTurnRemaining"/>
    /// <seealso cref="SetGunTurnRate"/>
    void SetTurnGunLeft(double degrees);

    /// <summary>
    /// Turn the gun to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="GunTurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="SetGunMaxTurnRate"/>.
    ///
    /// This call is executed immediately be calling Go() in the code behind. This method
    /// will block until its has been completed completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute multiple
    /// commands in parallel, use <strong>setter</strong> methods instead of this blocking method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnGunLeft"/> and
    /// <see cref="SetTurnGunRight"/>
    /// </summary>
    /// <param name="degrees">is the amount of degrees to turn left. If negative, the gun will turn
    /// right.</param>
    /// <seealso cref="SetTurnGunLeft"/>
    /// <seealso cref="SetTurnGunRight"/>
    /// <seealso cref="TurnGunRight"/>
    /// <seealso cref="GunTurnRemaining"/>
    /// <seealso cref="SetGunTurnRate"/>
    void TurnGunLeft(double degrees);

    /// <summary>
    /// Set the gun to turn to the right (following the decreasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="GunTurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="SetGunMaxTurnRate"/>.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go().
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnGunLeft"/>.
    /// </summary>
    /// <param name="degrees">is the amount of degrees to turn right. If negative, the gun will
    /// turn left.</param>
    /// <seealso cref="SetTurnGunLeft"/>
    /// <seealso cref="TurnGunRight"/>
    /// <seealso cref="TurnGunLeft"/>
    /// <seealso cref="GunTurnRemaining"/>
    /// <seealso cref="SetGunTurnRate"/>
    void SetTurnGunRight(double degrees);

    /// <summary>
    /// Turn the gun to the right (following the decreasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="GunTurnRemaining"/> is 0. The amount of degrees to
    /// turn each turn is limited by <see cref="SetGunMaxTurnRate"/>.
    ///
    /// This call is executed immediately be calling Go() in the code behind. This method
    /// will block until its has been completed completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute multiple
    /// commands in parallel, use <strong>setter</strong> methods instead of this blocking method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnGunLeft"/> and
    /// <see cref="SetTurnGunRight"/>.
    /// </summary>
    /// <param name="degrees">is the amount of degrees to turn right. If negative, the gun will
    /// turn left.</param>
    /// <seealso cref="SetTurnGunLeft"/>
    /// <seealso cref="SetTurnGunRight"/>
    /// <seealso cref="TurnGunLeft"/>
    /// <seealso cref="GunTurnRemaining"/>
    /// <seealso cref="SetGunTurnRate"/>W
    void TurnGunRight(double degrees);

    /// <summary>
    /// The remaining turn in degrees till the gun has finished turning after having called
    /// <see cref="SetTurnGunLeft"/>, <see cref="SetTurnGunRight"/>, <see cref="TurnGunLeft"/>, or
    /// <see cref="TurnGunRight"/>". When the turn remaining has reached 0, the gun has finished turning.
    ///
    /// When the turn remaining is positive, the bot is turning to the left (along the unit circle).
    /// When the turn remaining is negative, the bot is turning to the right.
    /// </summary>
    /// <returns>the remaining degrees to turn the gun</returns>
    /// <seealso cref="SetTurnGunLeft"/>
    /// <seealso cref="SetTurnGunRight"/>
    /// <seealso cref="TurnGunLeft"/>
    /// <seealso cref="TurnGunRight"/>
    double GunTurnRemaining { get; }

    /// <summary>
    /// Sets the maximum turn rate which applies turning the gun to the left or right. The maximum turn
    /// rate must be an absolute value from 0 to MaxGunTurnRate, both values are
    /// included. If the input turn rate is negative, the max turn rate will be cut to zero. If the
    /// input turn rate is above MaxGunTurnRate, the max turn rate will be set to MaxGunTurnRate.
    ///
    /// If for example the max gun turn rate is set to 5, then the gun will be able to turn left or
    /// right with a turn rate down to -5 degrees/turn when turning right and up to 5 degrees/turn when
    /// turning left.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go().
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    /// </summary>
    /// <param name="maxGunTurnRate">is the new maximum gun turn rate</param>
    /// <seealso cref="SetGunTurnRate"/>
    /// <seealso cref="SetTurnGunLeft"/>
    /// <seealso cref="SetTurnGunRight"/>
    /// <seealso cref="TurnGunLeft"/>
    /// <seealso cref="TurnGunRight"/>
    void SetMaxGunTurnRate(double maxGunTurnRate);

    /// <summary>
    /// Set the radar to turn to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="RadarTurnRemaining"/> is 0. The amount of degrees
    /// to turn each turn is limited by <see cref="SetMaxRadarTurnRate"/>.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go().
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnRadarRight"/>.
    /// </summary>
    /// <param name="degrees">is the amount of degrees to turn left. If negative, the radar will
    /// turn right.</param>
    /// <seealso cref="SetTurnRadarRight"/>
    /// <seealso cref="TurnRadarRight"/>
    /// <seealso cref="TurnRadarLeft"/>
    /// <seealso cref="RadarTurnRemaining"/>
    /// <seealso cref="SetRadarTurnRate"/>
    void SetTurnRadarLeft(double degrees);

    /// <summary>
    /// Turn the radar to the left (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="RadarTurnRemaining"/> is 0. The amount of degrees
    /// to turn each turn is limited by <see cref="SetMaxRadarTurnRate"/>.
    ///
    /// This call is executed immediately be calling Go() in the code behind. This method
    /// will block until its has been completed completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute multiple
    /// commands in parallel, use <strong>setter</strong> methods instead of this blocking method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnRadarLeft"/> and
    /// <see cref="SetTurnRadarRight"/>.
    /// </summary>
    /// <param name="degrees">is the amount of degrees to turn left. If negative, the radar will
    /// turn right.</param>
    /// <seealso cref="SetTurnRadarLeft"/>
    /// <seealso cref="SetTurnRadarRight"/>
    /// <seealso cref="TurnRadarLeft"/>
    /// <seealso cref="RadarTurnRemaining"/>
    /// <seealso cref="SetRadarTurnRate"/>
    void TurnRadarLeft(double degrees);

    /// <summary>
    /// Turn the radar to the right (following the decreasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="RadarTurnRemaining"/> is 0. The amount of degrees
    /// to turn each turn is limited by <see cref="SetMaxRadarTurnRate"/>.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go().
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnRadarLeft"/> and
    /// <see cref="SetTurnRadarRight"/>.
    /// </summary>
    /// <param name="degrees">is the amount of degrees to turn right. If negative, the radar will
    /// turn left.</param>
    /// <seealso cref="SetTurnRadarLeft"/>
    /// <seealso cref="TurnRadarLeft"/>
    /// <seealso cref="TurnRadarRight"/>
    /// <seealso cref="RadarTurnRemaining"/>
    /// <seealso cref="SetRadarTurnRate"/>
    void SetTurnRadarRight(double degrees);

    /// <summary>
    /// Turn the radar to the right (following the increasing degrees of the <a
    /// href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
    /// amount of degrees. That is, when <see cref="RadarTurnRemaining"/> is 0. The amount of degrees
    /// to turn each turn is limited by <see cref="SetMaxRadarTurnRate"/>.
    ///
    /// This call is executed immediately be calling Go() in the code behind. This method
    /// will block until its has been completed completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute multiple
    /// commands in parallel, use <strong>setter</strong> methods instead of this blocking method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetTurnRadarLeft"/> and
    /// <see cref="SetTurnRadarRight"/>.
    /// </summary>
    /// <param name="degrees">is the amount of degrees to turn right. If negative, the radar will
    /// turn left.</param>
    /// <seealso cref="SetTurnRadarLeft"/>
    /// <seealso cref="SetTurnRadarRight"/>
    /// <seealso cref="TurnRadarRight"/>
    /// <seealso cref="RadarTurnRemaining"/>
    /// <seealso cref="SetRadarTurnRate"/>
    void TurnRadarRight(double degrees);

    /// <summary>
    /// The remaining turn in degrees till the radar has finished turning after having called
    /// <see cref="SetTurnRadarLeft"/>, <see cref="SetTurnRadarRight"/>, <see cref="TurnRadarLeft"/>, or
    /// <see cref="TurnRadarRight"/>. When the turn remaining has reached 0, the radar has finished turning.
    ///
    /// >When the turn remaining is positive, the bot is turning to the left (along the unit circle).
    /// When the turn remaining is negative, the bot is turning to the right.
    /// </summary>
    /// <returns>the remaining degrees to turn the radar</returns>
    /// <seealso cref="SetTurnRadarLeft"/>
    /// <seealso cref="SetTurnRadarRight"/>
    /// <seealso cref="TurnRadarLeft"/>
    /// <seealso cref="TurnRadarRight"/>
    double RadarTurnRemaining { get; }

    /// <summary>
    /// Sets the maximum turn rate which applies turning the radar to the left or right. The maximum
    /// turn rate must be an absolute value from 0 to MaxRadarTurnRate, both values are
    /// included. If the input turn rate is negative, the max turn rate will be cut to zero. If the
    /// input turn rate is above MaxRadarTurnRate, the max turn rate will be set to MaxRadarTurnRate.
    ///
    /// If for example the max radar turn rate is set to 5, then the radar will be able to turn left
    /// or right with a turn rate down to -5 degrees/turn when turning right and up to 5 degrees/turn
    /// when turning left.
    ///
    /// This method will first be executed when Go() is called making it possible to call
    /// other set methods prior to execution. This makes it possible to set the bot to move, turn the
    /// body, radar, gun, and also fire the gun in parallel in a single turn when calling Go().
    /// But notice that this is only possible execute multiple methods in parallel by using
    /// <strong>setter</strong> methods only prior to calling Go().
    ///
    /// If this method is called multiple times, the last call before Go() is executed, counts.
    /// </summary>
    /// <param name="maxRadarTurnRate">is the new maximum radar turn rate</param>
    /// <seealso cref="SetRadarTurnRate"/>
    /// <seealso cref="SetTurnRadarLeft"/>
    /// <seealso cref="SetTurnRadarRight"/>
    /// <seealso cref="TurnRadarLeft"/>
    /// <seealso cref="TurnRadarRight"/>
    void SetMaxRadarTurnRate(double maxRadarTurnRate);

    /// <summary>
    /// Fire the gun in the direction as the gun is pointing.
    ///
    /// Note that your bot is spending energy when firing a bullet, the amount of energy used for
    /// firing the bullet is taken from the bot. The amount of energy loss is equal to the firepower.
    ///
    /// If the bullet hits an opponent bot, you will gain energy from the bullet hit. When hitting
    /// another bot, your bot will be rewarded and retrieve an energy boost of 3x firepower.
    ///
    /// The gun will only fire when the firepower is at MinFirepower or higher. If the
    /// firepower is more than MaxFirepower, the power will be truncated to the MaxFirepower.
    ///
    /// Whenever the gun is fired, the gun is heated an needs to cool down before it is able to fire
    /// again. The gun heat must be zero before the gun is able to fire (see <see cref="GunHeat"/>.
    /// The gun heat generated by firing the gun is 1 + (firepower / 5). Hence, the more firepower used the
    /// longer it takes to cool down the gun. The gun cooling rate can be read from <see
    /// cref="GunCoolingRate"/>.
    ///
    /// The amount of energy used for firing the gun is subtracted from the bots total energy. The
    /// amount of damage dealt by a bullet hitting another bot is 4x firepower, and if the firepower is
    /// greater than 1 it will do an additional 2 x (firepower - 1) damage.
    ///
    /// The firepower is truncated to MinFirepower and MaxFirepower if the firepower exceeds these values.
    ///
    /// This call is executed immediately be calling Go() in the code behind. This method
    /// will block until its has been completed completed, which can take one to several turns. New
    /// commands will first take place after this method is completed. If you need to execute multiple
    /// commands in parallel, use <strong>setter</strong> methods instead of this blocking method.
    ///
    /// This method will cancel the effect of prior calls to <see cref="SetFire"/>.
    /// </summary>
    /// <param name="firepower">is the amount of energy spend on firing the gun. You cannot spend
    /// more energy that available from the bot. The bullet power must be > MinFirepower.</param>
    /// <seealso cref="OnBulletFired"/>
    /// <seealso cref="SetFire"/>
    /// <seealso cref="GunHeat"/>
    /// <seealso cref="GunCoolingRate"/>
    void Fire(double firepower);
  }
}