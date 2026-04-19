using System;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Graphics;
using Robocode.TankRoyale.BotApi.Util;

namespace Robocode.TankRoyale.BotApi.Internal;

public static class IntentValidator
{
    public static double ValidateFirepower(double firepower)
    {
        if (double.IsNaN(firepower)) throw new ArgumentException("'firepower' cannot be NaN");
        return firepower;
    }

    public static double ValidateTurnRate(double turnRate, double maxTurnRate)
    {
        if (double.IsNaN(turnRate)) throw new ArgumentException("'TurnRate' cannot be NaN");
        return Math.Clamp(turnRate, -maxTurnRate, maxTurnRate);
    }

    public static double ValidateGunTurnRate(double gunTurnRate, double maxGunTurnRate)
    {
        if (double.IsNaN(gunTurnRate)) throw new ArgumentException("'GunTurnRate' cannot be NaN");
        return Math.Clamp(gunTurnRate, -maxGunTurnRate, maxGunTurnRate);
    }

    public static double ValidateRadarTurnRate(double radarTurnRate, double maxRadarTurnRate)
    {
        if (double.IsNaN(radarTurnRate)) throw new ArgumentException("'RadarTurnRate' cannot be NaN");
        return Math.Clamp(radarTurnRate, -maxRadarTurnRate, maxRadarTurnRate);
    }

    public static double ValidateTargetSpeed(double targetSpeed, double maxSpeed)
    {
        if (double.IsNaN(targetSpeed)) throw new ArgumentException("'TargetSpeed' cannot be NaN");
        return Math.Clamp(targetSpeed, -maxSpeed, maxSpeed);
    }

    public static double ValidateMaxSpeed(double maxSpeed)
    {
        if (double.IsNaN(maxSpeed)) throw new ArgumentException("'MaxSpeed' cannot be NaN");
        return Math.Clamp(maxSpeed, 0, Constants.MaxSpeed);
    }

    public static double ValidateMaxTurnRate(double maxTurnRate)
    {
        if (double.IsNaN(maxTurnRate)) throw new ArgumentException("'MaxTurnRate' cannot be NaN");
        return Math.Clamp(maxTurnRate, 0, Constants.MaxTurnRate);
    }

    public static double ValidateMaxGunTurnRate(double maxGunTurnRate)
    {
        if (double.IsNaN(maxGunTurnRate)) throw new ArgumentException("'MaxGunTurnRate' cannot be NaN");
        return Math.Clamp(maxGunTurnRate, 0, Constants.MaxGunTurnRate);
    }

    public static double ValidateMaxRadarTurnRate(double maxRadarTurnRate)
    {
        if (double.IsNaN(maxRadarTurnRate)) throw new ArgumentException("'MaxRadarTurnRate' cannot be NaN");
        return Math.Clamp(maxRadarTurnRate, 0, Constants.MaxRadarTurnRate);
    }

    public static double GetNewTargetSpeed(double speed, double distance, double maxSpeed)
    {
        if (distance < 0)
            return -GetNewTargetSpeed(-speed, -distance, maxSpeed);

        var targetSpeed = double.IsPositiveInfinity(distance) ? maxSpeed : Math.Min(GetMaxSpeed(distance), maxSpeed);

        double absDeceleration = Math.Abs(Constants.Deceleration);
        return speed >= 0
            ? Math.Clamp(targetSpeed, speed - absDeceleration, speed + Constants.Acceleration)
            : Math.Clamp(targetSpeed, speed - Constants.Acceleration, speed + GetMaxDeceleration(-speed));
    }

    private static double GetMaxSpeed(double distance)
    {
        double absDeceleration = Math.Abs(Constants.Deceleration);
        var decelerationTime =
            Math.Max(1, Math.Ceiling((Math.Sqrt((4 * 2 / absDeceleration) * distance + 1) - 1) / 2));
        if (double.IsPositiveInfinity(decelerationTime))
            return Constants.MaxSpeed;

        var decelerationDistance = (decelerationTime / 2) * (decelerationTime - 1) * absDeceleration;
        return ((decelerationTime - 1) * absDeceleration) + ((distance - decelerationDistance) / decelerationTime);
    }

    private static double GetMaxDeceleration(double speed)
    {
        double absDeceleration = Math.Abs(Constants.Deceleration);
        var decelerationTime = speed / absDeceleration;
        var accelerationTime = 1 - decelerationTime;

        return Math.Min(1, decelerationTime) * absDeceleration +
               Math.Max(0, accelerationTime) * Constants.Acceleration;
    }

    public static double GetDistanceTraveledUntilStop(double speed, double maxSpeed)
    {
        speed = Math.Abs(speed);
        double distance = 0;
        while (speed > 0)
            distance += (speed = GetNewTargetSpeed(speed, 0, maxSpeed));

        return distance;
    }

    public static void ValidateTeammateId(int? teammateId, ICollection<int> teammateIds)
    {
        if (teammateId != null && !teammateIds.Contains((int)teammateId))
        {
            throw new ArgumentException("No teammate was found with the specified 'teammateId': " + teammateId);
        }
    }

    public static void ValidateTeamMessage(object message, int currentTeamMessageCount)
    {
        if (currentTeamMessageCount == IBaseBot.MaxNumberOfTeamMessagesPerTurn)
            throw new InvalidOperationException(
                "The maximum number team massages has already been reached: " +
                IBaseBot.MaxNumberOfTeamMessagesPerTurn);

        if (message == null)
            throw new ArgumentException("The 'message' of a team message cannot be null");
    }

    public static void ValidateTeamMessageSize(string json)
    {
        var bytes = System.Text.Encoding.UTF8.GetBytes(json);
        if (bytes.Length > IBaseBot.TeamMessageMaxSize)
            throw new ArgumentException(
                $"The team message is larger than the limit of {IBaseBot.TeamMessageMaxSize} bytes (compact JSON format)");
    }

    public static string ColorToHex(Color? color) => color == null ? null : "#" + ColorUtil.ToHex(color);
}
