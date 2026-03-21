import dev.robocode.tankroyale.runner.*;

import java.util.List;

/**
 * Demonstrates running a battle with a team bot entry.
 * <p>
 * A team directory contains a &lt;dir&gt;.json with a "teamMembers" array. The runner
 * expands the team into one expected identity per member automatically.
 * <p>
 * Usage:
 * export BOTS_DIR=/path/to/sample-bots/java/build/archive
 * java -cp lib/* TeamBattle.java
 */
public class TeamBattle {

    public static void main(String[] args) {
        var botsDir = requireBotsDir();
        try (var runner = BattleRunner.create(b -> b.embeddedServer())) {
            var setup = BattleSetup.classic(s -> s.setNumberOfRounds(3));
            var bots = List.of(
                    BotEntry.of(botsDir + "/MyFirstTeam"),  // team — expanded to one entry per member
                    BotEntry.of(botsDir + "/Walls"),
                    BotEntry.of(botsDir + "/SpinBot")
            );
            System.out.println("Starting team battle: MyFirstTeam vs Walls vs SpinBot (3 rounds, Classic preset)");
            var results = runner.runBattle(setup, bots);
            System.out.printf("%nResults (%d rounds):%n", results.getNumberOfRounds());
            for (var bot : results.getResults()) {
                System.out.printf("  #%d  %-20s  %d pts%n",
                        bot.getRank(), bot.getName(), bot.getTotalScore());
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
