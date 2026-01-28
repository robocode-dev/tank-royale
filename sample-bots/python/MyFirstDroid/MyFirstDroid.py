from dataclasses import dataclass
from typing import Optional

from robocode_tank_royale.bot_api import Bot, team_message_type
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import TeamMessageEvent
from robocode_tank_royale.bot_api.droid_abc import DroidABC


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
# MyFirstDroid
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# This is a droid bot meaning that it has more energy, but no radar.
# Member of the MyFirstTeam. Follows orders of team leader.
# ------------------------------------------------------------------
class MyFirstDroid(Bot, DroidABC):

    def run(self) -> None:
        print("MyFirstDroid ready")
        while self.running:
            # Execute next turn (on_team_message() handles the logic based on team messages)
            self.go()
        # terminates when this point is reached

    def on_team_message(self, e: TeamMessageEvent) -> None:
        message = e.message

        if isinstance(message, Point):
            # ------------------------------------------------------
            # Message is a point towards a target
            # ------------------------------------------------------

            # Turn body to target and fire hard
            self.turn_right(self.bearing_to(message.x, message.y))
            self.fire(3)

        elif isinstance(message, RobotColors):
            # ------------------------------------------------------
            # Message is containing new robot colors
            # ------------------------------------------------------

            # Read and set the robot colors
            self.body_color = message.body_color
            self.tracks_color = message.tracks_color
            self.turret_color = message.turret_color
            self.gun_color = message.gun_color
            self.radar_color = message.radar_color
            self.scan_color = message.scan_color
            self.bullet_color = message.bullet_color


def main() -> None:
    bot = MyFirstDroid()
    bot.start()


if __name__ == "__main__":
    main()
