import dev.robocode.tankroyale.runner.*;

import java.time.Duration;
import java.util.List;

/**
 * Demonstrates boot progress reporting via the onBootProgress event.
 * <p>
 * Usage:
 * export BOTS_DIR=/path/to/sample-bots/java/build/archive
 * java -cp lib/* BootProgressBattle.java
 */
public class BootProgressBattle {

    public static void main(String[] args) {
        var botsDir = requireBotsDir();
        try (var runner = BattleRunner.create(b -> {
            b.embeddedServer();
            b.botConnectTimeout(Duration.ofSeconds(60));
        })) {
            var setup = BattleSetup.classic(s -> s.setNumberOfRounds(3));
            var bots = List.of(
                    BotEntry.of(botsDir + "/Walls"),
                    BotEntry.of(botsDir + "/SpinBot")
            );
            System.out.println("Starting battle with boot progress reporting...");
            var owner = new Object();
            try (var handle = runner.startBattleAsync(setup, bots)) {
                // Subscribe to boot progress events
                handle.getOnBootProgress().on(owner, progress -> {
                    System.out.printf("  Boot progress: %d/%d connected (%dms / %dms timeout)%n",
                            progress.getTotalConnected(),
                            progress.getTotalExpected(),
                            progress.getElapsedMs(),
                            progress.getTimeoutMs());
                    if (!progress.getPending().isEmpty()) {
                        System.out.println("    Pending: " + progress.getPending());
                    }
                });
                handle.getOnGameStarted().on(owner, event ->
                        System.out.println("All bots connected — game started!"));
                var results = handle.awaitResults();
                System.out.printf("%nResults (%d rounds):%n", results.getNumberOfRounds());
                for (var bot : results.getResults()) {
                    System.out.printf("  #%d  %-20s  %d pts%n",
                            bot.getRank(), bot.getName(), bot.getTotalScore());
                }
            }
        }
    }

    private static String requireBotsDir() {
        var botsDir = System.getenv("BOTS_DIR");
        if (botsDir == null || botsDir.isBlank()) {
            System.err.print("""
                    Error: BOTS_DIR environment variable is not set.
                    Set it to the directory containing your bot folders, e.g.:
                      export BOTS_DIR=/path/to/sample-bots/java/build/archive
                    """);
            System.exit(1);
        }
        return botsDir;
    }
}
