## MyFirstLeader
##
## A sample bot originally made for Robocode by Mathew Nelson.
## Ported to Nim for Robocode Tank Royale.
##
## Member of the MyFirstTeam. Looks around for enemies, and orders
## teammates to fire.

import asyncdispatch
import json
import robocode_tank_royale

type
  MyFirstLeader = ref object of Bot

method run(self: MyFirstLeader) {.async.} =
  ## Called when a new round is started -> Leader's default behavior
  ## Prepare robot colors to send to teammates
  var colors = %*{
    "bodyColor": {"r": 255, "g": 0, "b": 0},
    "tracksColor": {"r": 0, "g": 255, "b": 255},
    "turretColor": {"r": 255, "g": 0, "b": 0},
    "gunColor": {"r": 255, "g": 255, "b": 0},
    "radarColor": {"r": 255, "g": 0, "b": 0},
    "scanColor": {"r": 255, "g": 255, "b": 0},
    "bulletColor": {"r": 255, "g": 255, "b": 0}
  }

  ## Set the color of this robot containing the robot colors
  await self.setBodyColor(Color.fromRgb(255'u8, 0'u8, 0'u8))
  await self.setTracksColor(Color.fromRgb(0'u8, 255'u8, 255'u8))
  await self.setTurretColor(Color.fromRgb(255'u8, 0'u8, 0'u8))
  await self.setGunColor(Color.fromRgb(255'u8, 255'u8, 0'u8))
  await self.setRadarColor(Color.fromRgb(255'u8, 0'u8, 0'u8))
  await self.setScanColor(Color.fromRgb(255'u8, 255'u8, 0'u8))
  await self.setBulletColor(Color.fromRgb(255'u8, 255'u8, 0'u8))

  ## Send RobotColors object to every member in the team
  await self.broadcastTeamMessage(colors)

  ## Set the radar to turn right forever
  await self.setTurnRadarLeft(Inf)

  ## Repeat while the bot is running
  while self.isRunning():
    ## Move forward and back
    await self.forward(100.0)
    await self.back(100.0)

method onScannedBot(self: MyFirstLeader, event: ScannedBotEvent) {.async.} =
  ## Called when we scanned a bot -> Send enemy position to teammates
  ## We scanned a teammate -> ignore
  if self.isTeammate(event.scannedBotId):
    return

  ## Send enemy position to teammates
  var point = %*{"x": event.x, "y": event.y}
  await self.broadcastTeamMessage(point)

method onHitByBullet(self: MyFirstLeader, event: HitByBulletEvent) {.async.} =
  ## Called when we have been hit by a bullet -> turn perpendicular to the bullet direction
  ## Calculate the bullet bearing
  var bulletBearing = self.calcBearing(event.bullet.direction)

  ## Turn perpendicular to the bullet direction
  await self.turnLeft(90.0 - bulletBearing)

when isMainModule:
  let bot = MyFirstLeader()
  waitFor bot.start()