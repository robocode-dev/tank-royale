import { Bot, ColorUtil, Droid, TeamMessageEvent } from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// MyFirstDroid
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// This is a droid bot meaning that it has more energy, but no radar.
// Member of the MyFirstTeam. Follows orders of team leader.
// ------------------------------------------------------------------

// Communication objects for team messages
interface BotPoint {
  type: "BotPoint";
  x: number;
  y: number;
}

interface BotColors {
  type: "BotColors";
  bodyColor: string;
  tracksColor: string;
  turretColor: string;
  gunColor: string;
  radarColor: string;
  scanColor: string;
  bulletColor: string;
}

type TeamMessage = BotPoint | BotColors;

class MyFirstDroid extends Bot implements Droid {
  static main() {
    new MyFirstDroid().start();
  }

  override run() {
    console.log("MyFirstDroid ready");
    while (this.isRunning()) {
      this.go(); // execute next turn (onTeamMessage() handles all bot logic based on team messages)
    }
  }

  // Called when a team message is received, which will be sent from MyFirstLeader
  override onTeamMessage(e: TeamMessageEvent) {
    const message = JSON.parse(e.message) as TeamMessage;
    if (message.type === "BotPoint") {
      // ------------------------------------------------------
      // Message is a point towards a target
      // ------------------------------------------------------
      this.turnRight(this.bearingTo(message.x, message.y));
      this.fire(3);
    } else if (message.type === "BotColors") {
      // ------------------------------------------------------
      // Message is containing new robot colors
      // ------------------------------------------------------
      this.setBodyColor(ColorUtil.fromHexColor(message.bodyColor));
      this.setTracksColor(ColorUtil.fromHexColor(message.tracksColor));
      this.setTurretColor(ColorUtil.fromHexColor(message.turretColor));
      this.setGunColor(ColorUtil.fromHexColor(message.gunColor));
      this.setRadarColor(ColorUtil.fromHexColor(message.radarColor));
      this.setScanColor(ColorUtil.fromHexColor(message.scanColor));
      this.setBulletColor(ColorUtil.fromHexColor(message.bulletColor));
    }
  }
}

MyFirstDroid.main();
