import dev.robocode.tankroyale.runner.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reproduces issue #202: SkippedTurnEvent at turn 1 despite time remaining.
 *
 * Run BEFORE fix (with Thread.sleep simulation) → should see skipped turns.
 * Run AFTER fix → should see zero skipped turns.
 *
 * Usage:
 *   java -cp lib/* TimingBugTest.java
 */
public class TimingBugTest {

    private static final int NUM_ROUNDS = 10;
    private static final String TIMING_BOT_DIR = "C:/Code/bots/java/TimingBugBot";
    private static final String SAMPLE_BOTS_DIR = "C:/Code/tank-royale-main/sample-bots/java/build/archive";
    private static final Path BOT_OUTPUT = Path.of(TIMING_BOT_DIR, "bot_output.txt");

    public static void main(String[] args) throws IOException {
        Logger.getLogger("dev.robocode.tankroyale").setLevel(Level.WARNING);

        // Clear previous output
        Files.deleteIfExists(BOT_OUTPUT);

        System.out.println("Running " + NUM_ROUNDS + " rounds to check for tick-1 skipped turns...");

        try (var runner = BattleRunner.create(b -> b.embeddedServer().suppressServerOutput())) {
            var setup = BattleSetup.classic(s -> s.setNumberOfRounds(NUM_ROUNDS));
            var bots = List.of(
                    BotEntry.of(TIMING_BOT_DIR),
                    BotEntry.of(SAMPLE_BOTS_DIR + "/SpinBot")
            );
            runner.runBattle(setup, bots);
        }

        // Analyse output
        if (!Files.exists(BOT_OUTPUT)) {
            System.out.println("ERROR: bot_output.txt was not created — bot may not have run.");
            System.exit(1);
        }

        var lines = Files.readAllLines(BOT_OUTPUT);
        long skippedCount = lines.stream().filter(l -> l.startsWith("Skipped turn")).count();

        System.out.println("\n=== Results ===");
        System.out.println("Skipped-turn events: " + skippedCount);

        // Print context around each skipped turn
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("Skipped turn")) {
                int from = Math.max(0, i - 5);
                int to   = Math.min(lines.size(), i + 3);
                System.out.println("\n--- Skipped turn context (lines " + from + "-" + to + ") ---");
                lines.subList(from, to).forEach(System.out::println);
            }
        }

        if (skippedCount == 0) {
            System.out.println("\n✅ PASS: No skipped turns detected.");
        } else {
            System.out.println("\n❌ FAIL: " + skippedCount + " skipped turn(s) detected.");
            System.exit(1);
        }
    }
}
