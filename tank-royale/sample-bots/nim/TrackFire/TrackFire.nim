## TrackFire
##
## A sample bot originally made for Robocode by Mathew Nelson.
## Ported to Nim for Robocode Tank Royale.
##
## Sits still while tracking and firing at the nearest robot it
## detects.

import asyncdispatch
import math
import robocode_tank_royale

type
  TrackFire = ref object of Bot

method run(self: TrackFire) {.async.} =
  ## Called when a new round is started -> initialize and do some movement
  ## Set colors
  var pink = Color.fromRgb(0xFF, 0x69, 0xB4)
  await self.setBodyColor(pink)
  await self.setTurretColor(pink)
  await self.setRadarColor(pink)
  await self.setScanColor(pink)
  await self.setBulletColor(pink)

  ## Loop while running
  while self.isRunning():
    await self.turnGunRight(10.0) ## Scans automatically as radar is mounted on gun

method onScannedBot(self: TrackFire, event: ScannedBotEvent) {.async.} =
  ## We scanned another bot -> we have a target, so go get it
  ## Calculate direction of the scanned bot and bearing to it for the gun
  var bearingFromGun = self.gunBearingTo(event.x, event.y)

  ## Turn the gun toward the scanned bot
  await self.turnGunLeft(bearingFromGun)

  ## If it is close enough, fire!
  if abs(bearingFromGun) <= 3.0 and self.getGunHeat() == 0.0:
    await self.fire(min(3.0 - abs(bearingFromGun), self.getEnergy() - 0.1))

  ## Rescan immediately to keep tracking the target bot
  await self.rescan()

method onWonRound(self: TrackFire, event: WonRoundEvent) {.async.} =
  ## We won the round -> do a victory dance!
  ## Victory dance turning right 360 degrees 100 times
  await self.turnRight(36000.0)

when isMainModule:
  let bot = TrackFire()
  waitFor bot.start()