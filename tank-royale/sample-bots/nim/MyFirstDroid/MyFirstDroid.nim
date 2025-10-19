## MyFirstDroid
##
## A sample bot originally made for Robocode by Mathew Nelson.
## Ported to Nim for Robocode Tank Royale.
##
## This is a droid bot meaning that is has more energy, but no radar.
## Member of the MyFirstTeam. Follows orders of team leader.

import asyncdispatch
import json
import robocode_tank_royale

type
  MyFirstDroid = ref object of Bot
    ## Droid bot - has more energy but no radar

method run(self: MyFirstDroid) {.async.} =
  ## Called when a new round is started -> just print out that the bot is ready
  echo "MyFirstDroid ready"

  while self.isRunning():
    await self.go() ## execute next turn (onTeamMessage() takes handles all bot logic based on team messages)
  ## terminates when this point is reached

method onTeamMessage(self: MyFirstDroid, event: TeamMessageEvent) {.async.} =
  ## Called when a team message is received, which will be send from MyTeamLeader
  var message = event.message

  if message.hasKey("x") and message.hasKey("y"):
    ## ------------------------------------------------------
    ## Message is a point towards a target
    ## ------------------------------------------------------

    ## Read the target point
    var targetX = message["x"].getFloat()
    var targetY = message["y"].getFloat()

    ## Turn gun to target
    await self.turnRight(self.bearingTo(targetX, targetY))

    ## Fire hard!
    await self.fire(3.0)

  elif message.hasKey("bodyColor"):
    ## ------------------------------------------------------
    ## Message is containing new robot colors
    ## ------------------------------------------------------

    ## Read and set the robot colors
    if message.hasKey("bodyColor"):
      await self.setBodyColor(Color.fromRgb(
        message["bodyColor"]["r"].getInt().uint8,
        message["bodyColor"]["g"].getInt().uint8,
        message["bodyColor"]["b"].getInt().uint8
      ))
    if message.hasKey("tracksColor"):
      await self.setTracksColor(Color.fromRgb(
        message["tracksColor"]["r"].getInt().uint8,
        message["tracksColor"]["g"].getInt().uint8,
        message["tracksColor"]["b"].getInt().uint8
      ))
    if message.hasKey("turretColor"):
      await self.setTurretColor(Color.fromRgb(
        message["turretColor"]["r"].getInt().uint8,
        message["turretColor"]["g"].getInt().uint8,
        message["turretColor"]["b"].getInt().uint8
      ))
    if message.hasKey("gunColor"):
      await self.setGunColor(Color.fromRgb(
        message["gunColor"]["r"].getInt().uint8,
        message["gunColor"]["g"].getInt().uint8,
        message["gunColor"]["b"].getInt().uint8
      ))
    if message.hasKey("radarColor"):
      await self.setRadarColor(Color.fromRgb(
        message["radarColor"]["r"].getInt().uint8,
        message["radarColor"]["g"].getInt().uint8,
        message["radarColor"]["b"].getInt().uint8
      ))
    if message.hasKey("scanColor"):
      await self.setScanColor(Color.fromRgb(
        message["scanColor"]["r"].getInt().uint8,
        message["scanColor"]["g"].getInt().uint8,
        message["scanColor"]["b"].getInt().uint8
      ))
    if message.hasKey("bulletColor"):
      await self.setBulletColor(Color.fromRgb(
        message["bulletColor"]["r"].getInt().uint8,
        message["bulletColor"]["g"].getInt().uint8,
        message["bulletColor"]["b"].getInt().uint8
      ))

when isMainModule:
  let bot = MyFirstDroid()
  waitFor bot.start()