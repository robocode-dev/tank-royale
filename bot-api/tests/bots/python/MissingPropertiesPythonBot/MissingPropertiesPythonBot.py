from robocode_tank_royale.bot_api import Bot, BotInfo

class MissingPropertiesPythonBot(Bot):
    def __init__(self):
        bot_info = BotInfo(
            name=None, # Name is missing!
            version="1.0.0",
            authors=["Author"],
            description="A bot missing its name",
        )
        super().__init__(bot_info)

    def run(self):
        while self.is_running:
            self.forward(100)

if __name__ == "__main__":
    MissingPropertiesPythonBot().start()
