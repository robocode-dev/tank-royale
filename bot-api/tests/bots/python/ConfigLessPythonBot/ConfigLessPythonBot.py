import sys
from robocode_tank_royale.bot_api import Bot, BotInfo

class ConfigLessPythonBot(Bot):
    def __init__(self):
        bot_info = BotInfo(
            name="ConfigLessPythonBot",
            version="1.0.0",
            authors=["Author"],
            description="A bot without a .json file",
            country_codes=["US"],
            game_types={"classic"},
            platform="python",
            programming_lang="python"
        )
        super().__init__(bot_info)

    def run(self):
        while self.is_running:
            self.forward(100)
            self.turn_left(90)

if __name__ == "__main__":
    ConfigLessPythonBot().start()
