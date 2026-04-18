import pytest
from robocode_tank_royale.bot_api.base_bot import BaseBot
from robocode_tank_royale.bot_api.bot_info import BotInfo
from robocode_tank_royale.bot_api.bot_exception import BotException

class TestBot(BaseBot):
    def __init__(self):
        super().__init__(BotInfo(
            name="TestBot",
            version="1.0",
            authors=["Author"],
            game_types={"classic"}
        ))
    def run(self):
        pass

@pytest.mark.BOT
def test_TR_API_BOT_007_base_bot_accessor_defaults():
    bot = TestBot()
    
    # Metadata accessors should raise BotException when not connected
    with pytest.raises(BotException):
        _ = bot.my_id
    with pytest.raises(BotException):
        _ = bot.variant
    with pytest.raises(BotException):
        _ = bot.version
        
    # State-dependent accessors should raise BotException when no state is available
    with pytest.raises(BotException):
        _ = bot.energy
    with pytest.raises(BotException):
        _ = bot.x
    with pytest.raises(BotException):
        _ = bot.y
    with pytest.raises(BotException):
        _ = bot.direction
    with pytest.raises(BotException):
        _ = bot.gun_direction
    with pytest.raises(BotException):
        _ = bot.radar_direction
    with pytest.raises(BotException):
        _ = bot.speed
    with pytest.raises(BotException):
        _ = bot.gun_heat
    with pytest.raises(BotException):
        _ = bot.bullet_states
    with pytest.raises(BotException):
        _ = bot.events
        
    # Game setup accessors should raise BotException when no game setup is available
    with pytest.raises(BotException):
        _ = bot.arena_width
    with pytest.raises(BotException):
        _ = bot.arena_height
    with pytest.raises(BotException):
        _ = bot.game_type

@pytest.mark.BOT
def test_TR_API_BOT_008_adjustment_flags_default_false():
    bot = TestBot()
    
    assert bot.adjust_gun_for_body_turn is False
    assert bot.adjust_radar_for_body_turn is False
    assert bot.adjust_radar_for_gun_turn is False
