import asyncio
from typing import Any, Dict

from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import TeamMessageEvent


# ------------------------------------------------------------------
# MyFirstDroid
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# This is a droid bot meaning that it has more energy, but no radar.
# Member of the MyFirstTeam. Follows orders of team leader.
# ------------------------------------------------------------------
class MyFirstDroid(Bot):
    async def run(self) -> None:
        print("MyFirstDroid ready")
        while self.is_running():
            # Execute next turn (on_team_message() handles the logic based on team messages)
            await self.go()
        # terminates when this point is reached

    async def on_team_message(self, e: TeamMessageEvent) -> None:
        message: Dict[str, Any] = e.message  # JSON object
        msg_type = message.get("type")

        if msg_type == "Point":
            # ------------------------------------------------------
            # Message is a point towards a target
            # ------------------------------------------------------
            x = float(message.get("x", 0.0))
            y = float(message.get("y", 0.0))
            # Turn body to target and fire hard
            await self.turn_right(self.bearing_to(x, y))
            await self.fire(3)

        elif msg_type == "RobotColors":
            # ------------------------------------------------------
            # Message contains new robot colors (hex strings)
            # ------------------------------------------------------
            self.body_color = _parse_hex_color(message.get("bodyColor"))
            self.tracks_color = _parse_hex_color(message.get("tracksColor"))
            self.turret_color = _parse_hex_color(message.get("turretColor"))
            self.gun_color = _parse_hex_color(message.get("gunColor"))
            self.radar_color = _parse_hex_color(message.get("radarColor"))
            self.scan_color = _parse_hex_color(message.get("scanColor"))
            self.bullet_color = _parse_hex_color(message.get("bulletColor"))


def _parse_hex_color(hex_str: Any) -> Color | None:
    if not isinstance(hex_str, str) or not hex_str.startswith('#'):
        return None
    hex_body = hex_str[1:]
    try:
        if len(hex_body) == 6:
            r = int(hex_body[0:2], 16)
            g = int(hex_body[2:4], 16)
            b = int(hex_body[4:6], 16)
            return Color.from_rgb(r, g, b)
        if len(hex_body) == 8:
            r = int(hex_body[0:2], 16)
            g = int(hex_body[2:4], 16)
            b = int(hex_body[4:6], 16)
            a = int(hex_body[6:8], 16)
            return Color.from_rgba(r, g, b, a)
    except ValueError:
        return None
    return None


async def main() -> None:
    bot = MyFirstDroid()
    await bot.start()


if __name__ == "__main__":
    asyncio.run(main())
