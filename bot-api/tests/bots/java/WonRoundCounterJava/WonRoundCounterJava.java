import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

import java.io.IOException;
import java.nio.file.*;

/**
 * Instrumented bot that counts received WonRoundEvents and writes the count to a temp file.
 * Used by runner integration tests to verify bot-side WonRoundEvent delivery.
 */
public class WonRoundCounterJava extends Bot {

    private int wonRoundCount = 0;
    private final Path countFile = Path.of(
            System.getProperty("java.io.tmpdir"), "won_round_java.txt");

    public static void main(String[] args) {
        new WonRoundCounterJava().start();
    }

    @Override
    public void run() {
        while (isRunning()) {
            turnRight(10);
        }
    }

    @Override
    public void onWonRound(WonRoundEvent e) {
        wonRoundCount++;
        try {
            Files.writeString(countFile, String.valueOf(wonRoundCount));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        fire(1);
    }
}
