import math
from typing import Any, Set, Optional
from ..bot_exception import BotException
from ..graphics import Color
from ..util.math_util import MathUtil
from ..constants import (
    MAX_SPEED,
    MAX_TURN_RATE,
    MAX_GUN_TURN_RATE,
    MAX_RADAR_TURN_RATE,
    DECELERATION,
    ACCELERATION,
    MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN,
    TEAM_MESSAGE_MAX_SIZE,
)


class IntentValidator:
    @staticmethod
    def validate_firepower(firepower: float) -> float:
        if math.isnan(firepower):
            raise ValueError("'firepower' cannot be NaN")
        return firepower

    @staticmethod
    def validate_turn_rate(turn_rate: float, max_turn_rate: float) -> float:
        if math.isnan(turn_rate):
            raise ValueError("'turn_rate' cannot be NaN")
        return MathUtil.clamp(turn_rate, -max_turn_rate, max_turn_rate)

    @staticmethod
    def validate_gun_turn_rate(gun_turn_rate: float, max_gun_turn_rate: float) -> float:
        if math.isnan(gun_turn_rate):
            raise ValueError("'gun_turn_rate' cannot be NaN")
        return MathUtil.clamp(gun_turn_rate, -max_gun_turn_rate, max_gun_turn_rate)

    @staticmethod
    def validate_radar_turn_rate(radar_turn_rate: float, max_radar_turn_rate: float) -> float:
        if math.isnan(radar_turn_rate):
            raise ValueError("'radar_turn_rate' cannot be NaN")
        return MathUtil.clamp(radar_turn_rate, -max_radar_turn_rate, max_radar_turn_rate)

    @staticmethod
    def validate_target_speed(target_speed: float, max_speed: float) -> float:
        if math.isnan(target_speed):
            raise ValueError("'target_speed' cannot be NaN")
        return MathUtil.clamp(target_speed, -max_speed, max_speed)

    @staticmethod
    def validate_max_speed(max_speed: float) -> float:
        return MathUtil.clamp(max_speed, 0, MAX_SPEED)

    @staticmethod
    def validate_max_turn_rate(max_turn_rate: float) -> float:
        return MathUtil.clamp(max_turn_rate, 0, MAX_TURN_RATE)

    @staticmethod
    def validate_max_gun_turn_rate(max_gun_turn_rate: float) -> float:
        return MathUtil.clamp(max_gun_turn_rate, 0, MAX_GUN_TURN_RATE)

    @staticmethod
    def validate_max_radar_turn_rate(max_radar_turn_rate: float) -> float:
        return MathUtil.clamp(max_radar_turn_rate, 0, MAX_RADAR_TURN_RATE)

    @staticmethod
    def get_new_target_speed(speed: float, distance: float, max_speed: float) -> float:
        if distance < 0:
            return -IntentValidator.get_new_target_speed(-speed, -distance, max_speed)

        target_speed: float
        if math.isinf(distance):
            target_speed = max_speed
        else:
            target_speed = min(max_speed, IntentValidator._get_max_speed_for_distance(distance))

        abs_deceleration = abs(DECELERATION)
        if speed >= 0:
            return MathUtil.clamp(target_speed, speed - abs_deceleration, speed + ACCELERATION)
        else:
            return MathUtil.clamp(
                target_speed,
                speed - ACCELERATION,
                speed + IntentValidator._get_max_deceleration(-speed),
            )

    @staticmethod
    def _get_max_speed_for_distance(distance: float) -> float:
        abs_deceleration = abs(DECELERATION)
        deceleration_time = max(
            1,
            math.ceil((math.sqrt((4 * 2 / abs_deceleration) * distance + 1) - 1) / 2),
        )
        if math.isinf(deceleration_time):
            return MAX_SPEED

        deceleration_distance = (
            (deceleration_time / 2) * (deceleration_time - 1) * abs_deceleration
        )
        return ((deceleration_time - 1) * abs_deceleration) + (
            (distance - deceleration_distance) / deceleration_time
        )

    @staticmethod
    def _get_max_deceleration(speed: float) -> float:
        abs_deceleration = abs(DECELERATION)
        deceleration_time = speed / abs_deceleration
        acceleration_time = 1 - deceleration_time
        return (
            min(1, deceleration_time) * abs_deceleration
            + max(0, acceleration_time) * ACCELERATION
        )

    @staticmethod
    def get_distance_traveled_until_stop(speed: float, max_speed: float) -> float:
        speed = math.fabs(speed)
        distance = 0.0
        while speed > 0:
            speed = IntentValidator.get_new_target_speed(speed, 0, max_speed)
            distance += speed
        return distance

    @staticmethod
    def validate_teammate_id(teammate_id: Optional[int], teammate_ids: Set[int]) -> None:
        if teammate_id is not None and teammate_id not in teammate_ids:
            raise ValueError("No teammate was found with the specified 'teammate_id'")

    @staticmethod
    def validate_team_message(message: Any, current_team_message_count: int) -> None:
        if current_team_message_count >= MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN:
            raise BotException(
                f"The maximum number team messages has already been reached: {MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN}"
            )
        if message is None:
            raise ValueError("The 'message' of a team message cannot be null")

    @staticmethod
    def validate_team_message_size(json_message_str: str) -> None:
        if len(json_message_str.encode("utf-8")) > TEAM_MESSAGE_MAX_SIZE:
            raise ValueError(
                f"The team message is larger than the limit of {TEAM_MESSAGE_MAX_SIZE} bytes (compact JSON format)"
            )

    @staticmethod
    def color_to_schema(color: Optional[Color]) -> Optional[str]:
        return color.to_color_schema() if color else None
