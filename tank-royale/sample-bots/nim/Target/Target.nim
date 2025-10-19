## Target
##
## A sample bot originally made for Robocode by Mathew Nelson.
## Ported to Nim for Robocode Tank Royale.
##
## A stationary robot that moves when its energy drops below a certain
## threshold. This robot demonstrates how to use custom events.

import asyncdispatch
import robocode_tank_royale

type
  Target = ref object of Bot
    trigger: float

method run(self: Target) {.async.} =
  ## Called when a new round is started -> initialize and do some movement
  ## Set colors
  await self.setBodyColor(Color.WHITE)
  await self.setTurretColor(Color.WHITE)
  await self.setRadarColor(Color.WHITE)

  ## Initially, we'll move when energy passes 80
  self.trigger = 80.0

  ## Add a custom event
  discard self.addCustomEvent(proc(): bool = self.getEnergy() <= self.trigger)

method onCustomEvent(self: Target, event: CustomEvent) {.async.} =
  ## A custom event occurred
  ## Check if our custom event went off
  if event.condition():
    ## Adjust the trigger value, or else the event will fire again and again and again...
    self.trigger -= 20.0

    ## Print out energy level
    echo "Ouch, down to ", int(self.getEnergy() + 0.5), " energy."

    ## Move around a bit
    await self.turnRight(65.0)
    await self.forward(100.0)

when isMainModule:
  let bot = Target(trigger: 0.0)
  waitFor bot.start()