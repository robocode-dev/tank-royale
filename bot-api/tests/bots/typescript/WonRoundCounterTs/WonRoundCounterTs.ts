import { Bot, ScannedBotEvent, WonRoundEvent } from "@robocode.dev/tank-royale-bot-api";
import * as fs from "node:fs";
import * as os from "node:os";
import * as path from "node:path";

/**
 * Instrumented bot that counts received WonRoundEvents and writes the count to a temp file.
 * Used by runner integration tests to verify bot-side WonRoundEvent delivery.
 */
class WonRoundCounterTs extends Bot {
    private wonRoundCount = 0;
    private readonly countFile = path.join(os.tmpdir(), "won_round_ts.txt");

    static main() {
        new WonRoundCounterTs().start();
    }

    override run(): void {
        while (this.isRunning()) {
            this.turnRight(10);
        }
    }

    override onWonRound(_e: WonRoundEvent): void {
        this.wonRoundCount++;
        fs.writeFileSync(this.countFile, String(this.wonRoundCount));
    }

    override onScannedBot(_e: ScannedBotEvent): void {
        this.fire(1);
    }
}

WonRoundCounterTs.main();
