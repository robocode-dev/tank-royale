import {Bot, Color, ColorUtil, HitByBulletEvent, ScannedBotEvent} from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// MyFirstLeader
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// Member of the MyFirstTeam. Looks around for enemies, and orders
// teammates to fire.
// ------------------------------------------------------------------

class MyFirstLeader extends Bot {
    static main() {
        new MyFirstLeader().start();
    }

    override run() {
        // Prepare robot colors to send to teammates
        const colors = {
            type: "BotColors" as const,
            bodyColor: ColorUtil.toHex(Color.RED),
            tracksColor: ColorUtil.toHex(Color.CYAN),
            turretColor: ColorUtil.toHex(Color.RED),
            gunColor: ColorUtil.toHex(Color.YELLOW),
            radarColor: ColorUtil.toHex(Color.RED),
            scanColor: ColorUtil.toHex(Color.YELLOW),
            bulletColor: ColorUtil.toHex(Color.YELLOW),
        };

        // Set the color of this robot
        this.setBodyColor(Color.RED);
        this.setTracksColor(Color.CYAN);
        this.setTurretColor(Color.RED);
        this.setGunColor(Color.YELLOW);
        this.setRadarColor(Color.RED);
        this.setScanColor(Color.YELLOW);
        this.setBulletColor(Color.YELLOW);

        // Send BotColors object to every member in the team
        this.broadcastTeamMessage(JSON.stringify(colors));

        // Set the radar to turn left forever
        this.setTurnRadarLeft(Number.POSITIVE_INFINITY);

        // Repeat while the bot is running
        while (this.isRunning()) {
            // Move forward and back
            this.forward(100);
            this.back(100);
        }
    }

    // Called when we scanned a bot -> Send enemy position to teammates
    override onScannedBot(e: ScannedBotEvent) {
        // We scanned a teammate -> ignore
        if (this.isTeammate(e.scannedBotId)) {
            return;
        }
        // Send enemy position to teammates
        const point = {type: "BotPoint" as const, x: e.x, y: e.y};
        this.broadcastTeamMessage(JSON.stringify(point));
    }

    // Called when we have been hit by a bullet -> turn perpendicular to the bullet direction
    override onHitByBullet(e: HitByBulletEvent) {
        // Calculate the bullet bearing
        const bulletBearing = this.calcBearing(e.bullet.direction);
        // Turn perpendicular to the bullet direction
        this.turnLeft(90 - bulletBearing);
    }
}

MyFirstLeader.main();
