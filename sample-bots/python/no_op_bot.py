import asyncio

from robocode_tank_royale import bot_api as ba

async def main():
    """
    A no-op bot that does nothing.
    This is useful for testing the bot framework without any actual logic.
    """
    print("No-op bot is running. It does nothing.")
    b = ba.Bot(
        bot_info=ba.BotInfo(
            name="NoOpBot",
            version="1.0",
            authors=["Robocode Tank Royale"],
            description="A bot that does nothing.",
        )
    , server_secret='RECTjjm7ntrLpoYFh+kDuA/LHONbTYsLEnLMbuCnaU')
    await b.start()

if __name__ == "__main__":
    asyncio.run(main())