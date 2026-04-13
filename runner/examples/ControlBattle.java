import dev.robocode.tankroyale.runner.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs an asynchronous battle, pauses at turn 5, steps through 3 turns manually,
 * then resumes. Demonstrates pause(), nextTurn(), and resume() on a BattleHandle.
 *
 * Usage:
 *   export BOTS_DIR=/path/to/sample-bots/java/build/archive
 *   java -cp lib/* ControlBattle.java
 */
public class ControlBattle {

    public static void main(String[] args) {
        Logger.getLogger("dev.robocode.tankroyale").setLevel(Level.WARNING);
        var botsDir = requireBotsDir();

        try (var runner = BattleRunner.create(b -> b.embeddedServer().suppressServerOutput())) {
            var setup = BattleSetup.classic(s -> s.setNumberOfRounds(3));
            var bots = List.of(
                    BotEntry.of(botsDir + "/Walls"),
                    BotEntry.of(botsDir + "/SpinBot")
            );

            System.out.println("Starting controlled battle: Walls vs SpinBot (3 rounds)");
            var owner = new Object();
            var controlled = new AtomicBoolean();

            try (var handle = runner.startBattleAsync(setup, bots)) {
                handle.getOnRoundStarted().on(owner, event ->
                        System.out.printf("  Round %d started%n", event.getRoundNumber()));

                handle.getOnRoundEnded().on(owner, event ->
                        System.out.printf("  Round %d ended (turn %d)%n",
                                event.getRoundNumber(), event.getTurnNumber()));

                // Pause at turn 5 of each round (once per battle)
                handle.getOnTickEvent().on(owner, tick -> {
                    if (tick.getTurnNumber() == 5 && controlled.compareAndSet(false, true)) {
                        handle.pause();
                    }
                });

                // When paused: step 3 turns manually, then resume
                handle.getOnGamePaused().on(owner, event -> {
                    System.out.println("  Battle paused - stepping 3 turns manually...");
                    handle.nextTurn();
                    handle.nextTurn();
                    handle.nextTurn();
                    System.out.println("  Resuming...");
                    handle.resume();
                });

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
