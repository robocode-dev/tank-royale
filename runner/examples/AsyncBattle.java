import dev.robocode.tankroyale.runner.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs an asynchronous battle with real-time event streaming.
 *
 * Usage:
 *   export BOTS_DIR=/path/to/sample-bots/java/build/archive
 *   java -cp lib/* AsyncBattle.java
 */
public class AsyncBattle {

    public static void main(String[] args) {
        Logger.getLogger("dev.robocode.tankroyale").setLevel(Level.WARNING);
        var botsDir = requireBotsDir();

        try (var runner = BattleRunner.create(b -> b.embeddedServer().suppressServerOutput())) {
            var setup = BattleSetup.classic(s -> s.setNumberOfRounds(3));
            var bots = List.of(
                    BotEntry.of(botsDir + "/Walls"),
                    BotEntry.of(botsDir + "/SpinBot")
            );

            System.out.println("Starting async battle: Walls vs SpinBot (3 rounds)");
            var owner = new Object();

            try (var handle = runner.startBattleAsync(setup, bots)) {
                // Subscribe to round events
                handle.getOnRoundStarted().on(owner, event ->
                        System.out.printf("  Round %d started%n", event.getRoundNumber()));

                handle.getOnRoundEnded().on(owner, event ->
                        System.out.printf("  Round %d ended (turn %d)%n",
                                event.getRoundNumber(), event.getTurnNumber()));

                handle.getOnGameStarted().on(owner, event ->
                        System.out.println("Game started!"));

                // Wait for the battle to finish
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
