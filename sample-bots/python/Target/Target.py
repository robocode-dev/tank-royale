from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import CustomEvent
from robocode_tank_royale.bot_api.events.condition import Condition


# ------------------------------------------------------------------
# Target
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# A stationary robot that moves when its energy drops below a certain
# threshold. This robot demonstrates how to use custom events.
# ------------------------------------------------------------------
class Target(Bot):
    def __init__(self) -> None:
        super().__init__()
        self._trigger: int = 80  # Keeps track of when to move

    def run(self) -> None:
        # Set colors
        self.body_color = Color.WHITE
        self.turret_color = Color.WHITE
        self.radar_color = Color.WHITE

        # Initially, we'll move when energy passes 80
        self._trigger = 80

        # Add a custom event named "trigger-hit"
        self.add_custom_event(
            Condition(
                name="trigger-hit",
                callable=lambda: self.energy <= self._trigger,
            )
        )

        # No continuous action; the bot reacts to events
        while self.running:
            self.go()

    def on_custom_event(self, e: CustomEvent) -> None:
        # Check if our custom event "trigger-hit" went off
        if e.condition.name == "trigger-hit":
            # Adjust the trigger value, or else the event will fire again and again and again...
            self._trigger -= 20

            # Print out energy level
            print(f"Ouch, down to {int(self.energy + 0.5)} energy.")

            # Move around a bit
            self.turn_right(65)
            self.forward(100)


def main() -> None:
    bot = Target()
    bot.start()


if __name__ == "__main__":
    main()
