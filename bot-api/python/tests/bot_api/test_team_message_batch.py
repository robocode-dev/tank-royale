import pytest

from robocode_tank_royale.bot_api import BotException, TeamMessageBatch
from robocode_tank_royale.bot_api.constants import MAX_LOGICAL_TEAM_MESSAGES_PER_TURN
from robocode_tank_royale.bot_api.internal.intent_validator import IntentValidator


@pytest.mark.TR_API_TCK_022
def test_batch_keeps_order_and_limit_is_shared():
    batch = TeamMessageBatch(["first", "second"])
    assert batch.messages == ("first", "second")
    assert MAX_LOGICAL_TEAM_MESSAGES_PER_TURN == 128
    IntentValidator.validate_logical_team_message_count(128)
    with pytest.raises(BotException):
        IntentValidator.validate_logical_team_message_count(129)


@pytest.mark.TR_API_TCK_022
def test_batch_requires_nonempty_nonnull_values():
    with pytest.raises(ValueError):
        TeamMessageBatch([])
    with pytest.raises(ValueError):
        TeamMessageBatch(["ok", None])


@pytest.mark.TR_API_TCK_022
def test_batch_accepts_one_shot_iterables():
    batch = TeamMessageBatch(message for message in ["first", "second"])
    assert batch.messages == ("first", "second")
    with pytest.raises(ValueError):
        TeamMessageBatch(message for message in [])
    with pytest.raises(ValueError):
        TeamMessageBatch(message for message in ["ok", None])
