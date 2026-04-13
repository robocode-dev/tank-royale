import dev.robocode.tankroyale.runner.*;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs a battle and records it to a .battle.gz replay file.
 *
 * Usage:
 *   export BOTS_DIR=/path/to/sample-bots/java/build/archive
 *   java -cp lib/* RecordBattle.java
 */
public class RecordBattle {

    public static void main(String[] args) {
        Logger.getLogger("dev.robocode.tankroyale").setLevel(Level.WARNING);
        var botsDir = requireBotsDir();
        var recordingDir = Path.of("recordings");
        recordingDir.toFile().mkdirs();

        try (var runner = BattleRunner.create(b -> b
                .embeddedServer()
                .suppressServerOutput()
                .enableRecording(recordingDir))) {

            var setup = BattleSetup.classic(s -> s.setNumberOfRounds(3));
            var bots = List.of(
                    BotEntry.of(botsDir + "/Walls"),
                    BotEntry.of(botsDir + "/SpinBot")
            );

            System.out.println("Starting recorded battle: Walls vs SpinBot (3 rounds)");
            System.out.println("Recording to: " + recordingDir.toAbsolutePath());

            var results = runner.runBattle(setup, bots);

            System.out.printf("%nResults (%d rounds):%n", results.getNumberOfRounds());
            for (var bot : results.getResults()) {
                System.out.printf("  #%d  %-20s  %d pts%n",
                        bot.getRank(), bot.getName(), bot.getTotalScore());
            }

            var files = recordingDir.toFile().listFiles((dir, name) -> name.endsWith(".battle.gz"));
            if (files != null && files.length > 0) {
                for (var file : files) {
                    System.out.printf("%nRecording saved: %s (%.1f KB)%n",
                            file.getName(), file.length() / 1024.0);
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
