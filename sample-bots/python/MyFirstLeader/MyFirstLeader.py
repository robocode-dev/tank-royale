import asyncio
from dataclasses import dataclass

from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import ScannedBotEvent, HitByBulletEvent


@dataclass
class Point:
    type: str
    x: float
    y: float


def colors_to_message_dict() -> dict:
    # Prepare robot colors to send to teammates as hex strings
    body = Color.RED
    tracks = Color.CYAN
    turret = Color.RED
    gun = Color.YELLOW
    radar = Color.RED
    scan = Color.YELLOW
    bullet = Color.YELLOW

    return {
        "type": "RobotColors",
        "bodyColor": body.to_hex_color(),
        "tracksColor": tracks.to_hex_color(),
        "turretColor": turret.to_hex_color(),
        "gunColor": gun.to_hex_color(),
        "radarColor": radar.to_hex_color(),
        "scanColor": scan.to_hex_color(),
        "bulletColor": bullet.to_hex_color(),
    }


# ------------------------------------------------------------------
# MyFirstLeader
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# Member of the MyFirstTeam. Looks around for enemies, and orders
# teammates to fire.
# ------------------------------------------------------------------
class MyFirstLeader(Bot):
    async def run(self) -> None:
        # Prepare and set robot colors
        colors = colors_to_message_dict()

        # Apply colors to this robot
        self.body_color = Color.from_rgb(int(colors["bodyColor"][1:3], 16), int(colors["bodyColor"][3:5], 16), int(colors["bodyColor"][5:7], 16))
        self.tracks_color = Color.from_rgb(int(colors["tracksColor"][1:3], 16), int(colors["tracksColor"][3:5], 16), int(colors["tracksColor"][5:7], 16))
        self.turret_color = Color.from_rgb(int(colors["turretColor"][1:3], 16), int(colors["turretColor"][3:5], 16), int(colors["turretColor"][5:7], 16))
        self.gun_color = Color.from_rgb(int(colors["gunColor"][1:3], 16), int(colors["gunColor"][3:5], 16), int(colors["gunColor"][5:7], 16))
        self.radar_color = Color.from_rgb(int(colors["radarColor"][1:3], 16), int(colors["radarColor"][3:5], 16), int(colors["radarColor"][5:7], 16))
        self.scan_color = Color.from_rgb(int(colors["scanColor"][1:3], 16), int(colors["scanColor"][3:5], 16), int(colors["scanColor"][5:7], 16))
        self.bullet_color = Color.from_rgb(int(colors["bulletColor"][1:3], 16), int(colors["bulletColor"][3:5], 16), int(colors["bulletColor"][5:7], 16))

        # Send RobotColors to every member in the team
        self.broadcast_team_message(colors)

        # Set the radar to turn left forever
        self.set_turn_radar_left(float("inf"))

        # Repeat while the bot is running: Move forward and back
        while self.running:
            await self.forward(100)
            await self.back(100)

    async def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        # We scanned a teammate -> ignore
        if self.is_teammate(e.scanned_bot_id):
            return
        # Send enemy position to teammates
        self.broadcast_team_message({"type": "Point", "x": float(e.x), "y": float(e.y)})

    async def on_hit_by_bullet(self, e: HitByBulletEvent) -> None:
        # Calculate the bullet bearing
        bullet_bearing = self.calc_bearing(e.bullet.direction)
        # Turn perpendicular to the bullet direction
        await self.turn_left(90 - bullet_bearing)


async def main() -> None:
    bot = MyFirstLeader()
    await bot.start()


if __name__ == "__main__":
    asyncio.run(main())
