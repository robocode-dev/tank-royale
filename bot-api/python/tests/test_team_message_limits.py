import pytest

from robocode_tank_royale.bot_api.constants import (
    MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN,
    TEAM_MESSAGE_MAX_SIZE,
    TEAM_MESSAGES_MAX_BYTES_PER_TURN,
)
from robocode_tank_royale.bot_api.bot_exception import BotException
from robocode_tank_royale.bot_api.internal.intent_validator import IntentValidator

pytestmark = pytest.mark.Unit


def test_count_boundary() -> None:
    IntentValidator.validate_team_message("hello", 10)
    IntentValidator.validate_team_message("hello", MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN - 1)
    with pytest.raises(BotException):
        IntentValidator.validate_team_message("hello", MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN)


def test_unicode_payload_boundary() -> None:
    IntentValidator.validate_team_message_size("é" * (TEAM_MESSAGE_MAX_SIZE // 2))
    with pytest.raises(ValueError):
        IntentValidator.validate_team_message_size("é" * (TEAM_MESSAGE_MAX_SIZE // 2 + 1))


def test_aggregate_boundary() -> None:
    IntentValidator.validate_team_messages_size("x" * TEAM_MESSAGES_MAX_BYTES_PER_TURN)
    with pytest.raises(ValueError):
        IntentValidator.validate_team_messages_size("x" * (TEAM_MESSAGES_MAX_BYTES_PER_TURN + 1))
