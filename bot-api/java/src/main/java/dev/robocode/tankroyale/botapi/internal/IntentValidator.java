package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.graphics.Color;
import dev.robocode.tankroyale.botapi.util.ColorUtil;
import java.util.Set;
import static dev.robocode.tankroyale.botapi.Constants.*;
import static dev.robocode.tankroyale.botapi.util.MathUtil.clamp;
import static java.lang.Math.*;

public final class IntentValidator {

    private IntentValidator() {}

    public static double validateFirepower(double firepower) {
        if (Double.isNaN(firepower)) {
            throw new IllegalArgumentException("'firepower' cannot be NaN");
        }
        return firepower;
    }

    public static double validateTurnRate(double turnRate, double maxTurnRate) {
        if (Double.isNaN(turnRate)) {
            throw new IllegalArgumentException("'turnRate' cannot be NaN");
        }
        return clamp(turnRate, -maxTurnRate, maxTurnRate);
    }

    public static double validateGunTurnRate(double gunTurnRate, double maxGunTurnRate) {
        if (Double.isNaN(gunTurnRate)) {
            throw new IllegalArgumentException("'gunTurnRate' cannot be NaN");
        }
        return clamp(gunTurnRate, -maxGunTurnRate, maxGunTurnRate);
    }

    public static double validateRadarTurnRate(double radarTurnRate, double maxRadarTurnRate) {
        if (Double.isNaN(radarTurnRate)) {
            throw new IllegalArgumentException("'radarTurnRate' cannot be NaN");
        }
        return clamp(radarTurnRate, -maxRadarTurnRate, maxRadarTurnRate);
    }

    public static double validateTargetSpeed(double targetSpeed, double maxSpeed) {
        if (Double.isNaN(targetSpeed)) {
            throw new IllegalArgumentException("'targetSpeed' cannot be NaN");
        }
        return clamp(targetSpeed, -maxSpeed, maxSpeed);
    }

    public static double validateMaxSpeed(double maxSpeed) {
        return clamp(maxSpeed, 0, MAX_SPEED);
    }

    public static double validateMaxTurnRate(double maxTurnRate) {
        return clamp(maxTurnRate, 0, MAX_TURN_RATE);
    }

    public static double validateMaxGunTurnRate(double maxGunTurnRate) {
        return clamp(maxGunTurnRate, 0, MAX_GUN_TURN_RATE);
    }

    public static double validateMaxRadarTurnRate(double maxRadarTurnRate) {
        return clamp(maxRadarTurnRate, 0, MAX_RADAR_TURN_RATE);
    }

    public static double getNewTargetSpeed(double speed, double distance, double maxSpeed) {
        if (distance < 0) {
            return -getNewTargetSpeed(-speed, -distance, maxSpeed);
        }
        var targetSpeed = (distance == Double.POSITIVE_INFINITY) ?
                maxSpeed : min(maxSpeed, getMaxSpeed(distance));

        double absDeceleration = abs(DECELERATION);
        return (speed >= 0) ?
                clamp(targetSpeed, speed - absDeceleration, speed + ACCELERATION) :
                clamp(targetSpeed, speed - ACCELERATION, speed + getMaxDeceleration(-speed));
    }

    private static double getMaxSpeed(double distance) {
        double absDeceleration = abs(DECELERATION);
        double decelerationTime =
                max(1, Math.ceil((Math.sqrt((4 * 2 / absDeceleration) * distance + 1) - 1) / 2));
        if (decelerationTime == Double.POSITIVE_INFINITY) {
            return MAX_SPEED;
        }
        double decelerationDistance = (decelerationTime / 2) * (decelerationTime - 1) * absDeceleration;
        return ((decelerationTime - 1) * absDeceleration) + ((distance - decelerationDistance) / decelerationTime);
    }

    private static double getMaxDeceleration(double speed) {
        double absDeceleration = abs(DECELERATION);
        double decelerationTime = speed / absDeceleration;
        double accelerationTime = 1 - decelerationTime;

        return min(1, decelerationTime) * absDeceleration + max(0, accelerationTime) * ACCELERATION;
    }

    public static double getDistanceTraveledUntilStop(double speed, double maxSpeed) {
        speed = abs(speed);
        double distance = 0;
        while (speed > 0) {
            distance += (speed = getNewTargetSpeed(speed, 0, maxSpeed));
        }
        return distance;
    }

    public static void validateTeammateId(Integer teammateId, Set<Integer> teammateIds) {
        if (teammateId != null && !teammateIds.contains(teammateId)) {
            throw new IllegalArgumentException("No teammate was found with the specified 'teammateId': " + teammateId);
        }
    }

    public static void validateTeamMessage(Object message, int currentTeamMessageCount) {
        if (currentTeamMessageCount == MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN) {
            throw new BotException(
                    "The maximum number team massages has already been reached: " + MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN);
        }
        if (message == null) {
            throw new IllegalArgumentException("The 'message' of a team message cannot be null");
        }
    }

    public static void validateTeamMessageSize(String json) {
        if (json.getBytes().length > TEAM_MESSAGE_MAX_SIZE) {
            throw new IllegalArgumentException(
                    "The team message is larger than the limit of " + TEAM_MESSAGE_MAX_SIZE + " bytes (compact JSON format)");
        }
    }

    public static String colorToHex(Color color) {
        return color == null ? null : "#" + ColorUtil.toHex(color);
    }
}
