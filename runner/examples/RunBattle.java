import dev.robocode.tankroyale.runner.*;
import java.util.List;

/**
 * Runs a synchronous battle with two sample bots and prints the results.
 *
 * Usage:
 *   export BOTS_DIR=/path/to/sample-bots/java/build/archive
 *   java -cp lib/* RunBattle.java
 */
public class RunBattle {

    public static void main(String[] args) {
        var botsDir = requireBotsDir();

        try (var runner = BattleRunner.create(b -> b.embeddedServer())) {
            var setup = BattleSetup.classic(s -> s.setNumberOfRounds(5));
            var bots = List.of(
                    BotEntry.of(botsDir + "/Walls"),
                    BotEntry.of(botsDir + "/SpinBot")
            );

            System.out.println("Starting battle: Walls vs SpinBot (5 rounds, Classic preset)");
            var results = runner.runBattle(setup, bots);

            System.out.printf("%nResults (%d rounds):%n", results.getNumberOfRounds());
            System.out.println("─".repeat(60));
            System.out.printf("%-6s %-20s %10s %10s %10s%n", "Rank", "Bot", "Total", "Bullet", "Ram");
            System.out.println("─".repeat(60));
            for (var bot : results.getResults()) {
                System.out.printf("%-6d %-20s %10d %10d %10d%n",
                        bot.getRank(),
                        bot.getName() + " " + bot.getVersion(),
                        bot.getTotalScore(),
                        bot.getBulletDamage(),
                        bot.getRamDamage());
            }
        }
    }

    private static String requireBotsDir() {
        var botsDir = System.getenv("BOTS_DIR");
        if (botsDir == null || botsDir.isBlank()) {
            System.err.println("Error: BOTS_DIR environment variable is not set.");
            System.err.println();
            System.err.println("Set it to the directory containing your bot folders, e.g.:");
            System.err.println("  export BOTS_DIR=/path/to/sample-bots/java/build/archive");
            System.exit(1);
        }
        return botsDir;
    }
}
