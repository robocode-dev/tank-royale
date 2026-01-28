from dataclasses import dataclass
from typing import Optional

from robocode_tank_royale.bot_api import Bot, team_message_type
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import ScannedBotEvent, HitByBulletEvent


# ------------------------------------------------------------------
# Communication objects for team messages
# ------------------------------------------------------------------

# Point (x,y) class
@team_message_type
@dataclass
class Point:
    x: float
    y: float


# Robot colors
@team_message_type
@dataclass
class RobotColors:
    body_color: Optional[Color] = None
    tracks_color: Optional[Color] = None
    turret_color: Optional[Color] = None
    gun_color: Optional[Color] = None
    radar_color: Optional[Color] = None
    scan_color: Optional[Color] = None
    bullet_color: Optional[Color] = None


# ------------------------------------------------------------------
# MyFirstLeader
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# Member of the MyFirstTeam. Looks around for enemies, and orders
# teammates to fire.
# ------------------------------------------------------------------
class MyFirstLeader(Bot):

    def run(self) -> None:
        # Prepare robot colors to send to teammates
        colors = RobotColors()

        colors.body_color = Color.RED
        colors.tracks_color = Color.CYAN
        colors.turret_color = Color.RED
        colors.gun_color = Color.YELLOW
        colors.radar_color = Color.RED
        colors.scan_color = Color.YELLOW
        colors.bullet_color = Color.YELLOW

        # Set the color of this robot containing the robot colors
        self.body_color = colors.body_color
        self.tracks_color = colors.tracks_color
        self.turret_color = colors.turret_color
        self.gun_color = colors.gun_color
        self.radar_color = colors.radar_color
        self.scan_color = colors.scan_color
        self.bullet_color = colors.bullet_color

        # Send RobotColors object to every member in the team
        self.broadcast_team_message(colors)

        # Set the radar to turn left forever
        self.set_turn_radar_left(float("inf"))

        # Repeat while the bot is running: Move forward and back
        while self.running:
            self.forward(100)
            self.back(100)

    def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        # We scanned a teammate -> ignore
        if self.is_teammate(e.scanned_bot_id):
            return
        # Send enemy position to teammates
        self.broadcast_team_message(Point(x=e.x, y=e.y))

    def on_hit_by_bullet(self, e: HitByBulletEvent) -> None:
        # Calculate the bullet bearing
        bullet_bearing = self.calc_bearing(e.bullet.direction)
        # Turn perpendicular to the bullet direction
        self.turn_left(90 - bullet_bearing)


def main() -> None:
    bot = MyFirstLeader()
    bot.start()


if __name__ == "__main__":
    main()
