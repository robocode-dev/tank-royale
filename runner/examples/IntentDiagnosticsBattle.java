import dev.robocode.tankroyale.runner.*;
import java.util.List;

/**
 * Runs a battle with intent diagnostics enabled, then prints a turn-by-turn table
 * of each bot's movement speed, turn rates, and firing decisions.
 *
 * Usage:
 *   export BOTS_DIR=/path/to/sample-bots/java/build/archive
 *   java -cp lib/* IntentDiagnosticsBattle.java
 */
public class IntentDiagnosticsBattle {

    public static void main(String[] args) {
        var botsDir = requireBotsDir();

        try (var runner = BattleRunner.create(b -> b
                .embeddedServer()
                .enableIntentDiagnostics())) {

            var setup = BattleSetup.classic(s -> s.setNumberOfRounds(1));
            var bots = List.of(
                    BotEntry.of(botsDir + "/Walls"),
                    BotEntry.of(botsDir + "/SpinBot")
            );

            System.out.println("Starting battle with intent diagnostics: Walls vs SpinBot (1 round)");
            var results = runner.runBattle(setup, bots);

            System.out.printf("%nResults (%d rounds):%n", results.getNumberOfRounds());
            for (var bot : results.getResults()) {
                System.out.printf("  #%d  %-20s  %d pts%n",
                        bot.getRank(), bot.getName(), bot.getTotalScore());
            }

            var store = runner.getIntentDiagnostics();
            if (store == null) return;

            System.out.println();
            for (var botName : store.botNames()) {
                var intents = store.getIntentsForBot(botName);
                System.out.printf("%s - %d intents captured%n", botName, intents.size());
                System.out.println("-".repeat(68));
                System.out.printf("%-6s %-6s %11s %10s %12s %11s%n",
                        "Round", "Turn", "TargetSpd", "TurnRate", "GunTurnRate", "Firepower");
                System.out.println("-".repeat(68));

                var preview = intents.subList(0, Math.min(10, intents.size()));
                for (var ci : preview) {
                    var intent = ci.getIntent();
                    System.out.printf("%-6d %-6d %11s %10s %12s %11s%n",
                            ci.getRoundNumber(),
                            ci.getTurnNumber(),
                            fmt(intent.getTargetSpeed()),
                            fmt(intent.getTurnRate()),
                            fmt(intent.getGunTurnRate()),
                            fmt(intent.getFirepower()));
                }

                int remaining = intents.size() - preview.size();
                if (remaining > 0) {
                    System.out.printf("  ... and %d more%n", remaining);
                }
                System.out.println();
            }
        }
    }

    private static String fmt(Double value) {
        return value != null ? String.format("%.2f", value) : "-";
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
