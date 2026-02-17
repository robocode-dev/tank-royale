"""
Test bot for verifying stdout/stderr capture in Bot Console.
This bot prints messages in various event handlers to test that output appears in the GUI.
"""

from robocode_tank_royale.bot_api import BaseBot, BotInfo, ScannedBotEvent, HitByBulletEvent


class StdoutTestBot(BaseBot):
    """A bot that prints debug messages to test stdout capture"""

    def run(self):
        print("=" * 60)
        print("🤖 StdoutTestBot initialized!")
        print("If you see this in the Bot Console, stdout capture works!")
        print("=" * 60)

        turn_count = 0

        while self.running:
            turn_count += 1

            if turn_count % 10 == 0:
                print(f"Turn {turn_count}: Still running...")
                print(f"  Energy: {self.energy:.1f}")
                print(f"  Position: ({self.x:.1f}, {self.y:.1f})")

            # Simple movement pattern
            self.forward(100)
            self.turn_right(90)
            self.turn_radar_right(360)

            self.go()

    def on_scanned_bot(self, e: ScannedBotEvent):
        """Test print in high-frequency event handler"""
        print(f"📡 SCANNED BOT #{e.scanned_by_bot_id}:")
        print(f"   Distance: {e.distance:.1f}")
        print(f"   Bearing: {e.bearing:.1f}°")
        print(f"   Speed: {e.speed:.1f}")

        # Fire at the scanned bot
        self.fire(1)
        print(f"   🔫 Fired at target!")

    def on_hit_by_bullet(self, e: HitByBulletEvent):
        """Test print in occasional event"""
        print(f"💥 HIT BY BULLET! Damage: {e.damage:.1f}")
        print(f"   Energy remaining: {self.energy:.1f}")

        # Try to dodge
        self.turn_right(90)

    def on_death(self, e):
        """Test print on death"""
        print("=" * 60)
        print("☠️  GAME OVER")
        print(f"   Final energy: {self.energy:.1f}")
        print("=" * 60)


if __name__ == "__main__":
    # Bot info
    bot_info = BotInfo(
        name="StdoutTestBot",
        version="1.0",
        authors=["Test"],
        description="Test bot for stdout/stderr capture verification",
        homepage="",
        country_codes=["us"],
        game_types=["melee", "1v1"],
        platform="Python",
        programming_lang="Python"
    )

    # Create and start bot
    bot = StdoutTestBot(bot_info)
    bot.start()

