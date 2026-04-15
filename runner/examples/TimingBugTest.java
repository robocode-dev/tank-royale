import dev.robocode.tankroyale.runner.*;
import dev.robocode.tankroyale.client.model.BotState;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Verifies the fix for issue #202: radar turn command issued in run() before the first go()
 * must be honoured on turn 1 (not silently dropped).
 *
 * TimingBugBot calls setTurnRadarLeft(MAX_RADAR_TURN_RATE=45) then go() in its run() loop.
 * After the fix, the bot's radar should rotate ~45 degrees between turn 1 and turn 2.
 *
 * Strategy: collect radar angles for ALL bots during the battle, then after the battle
 * use BattleResults to find TimingBugBot's numeric ID and look up its radar history.
 * This avoids any race condition on onGameStarted.
 *
 * Usage:
 *   java -cp lib/* TimingBugTest.java
 */
public class TimingBugTest {

    private static final int NUM_ROUNDS = 5;
    private static final double MAX_RADAR_TURN_RATE = 45.0;
    private static final String TIMING_BOT_DIR = "C:/Code/bots/java/TimingBugBot";
    private static final String SAMPLE_BOTS_DIR = "C:/Code/tank-royale/sample-bots/java/build/archive";

    public static void main(String[] args) {
        Logger.getLogger("dev.robocode.tankroyale").setLevel(Level.WARNING);

        System.out.println("Running " + NUM_ROUNDS + " rounds to verify issue #202 radar fix...");
        System.out.println("TimingBugBot dir : " + TIMING_BOT_DIR);
        System.out.println("SpinBot dir      : " + SAMPLE_BOTS_DIR + "/SpinBot");
        System.out.println();

        var failures = new ArrayList<String>();

        // Maps: botId → (roundNumber → (turnNumber → radarDirection))
        var radarByBotRoundTurn = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Double>>>();

        try (var runner = BattleRunner.create(b -> b
                .embeddedServer()
                .suppressServerOutput())) {

            var setup = BattleSetup.classic(s -> s.setNumberOfRounds(NUM_ROUNDS));
            var bots = List.of(
                    BotEntry.of(TIMING_BOT_DIR),
                    BotEntry.of(SAMPLE_BOTS_DIR + "/SpinBot")
            );

            var owner = new Object();
            BattleResults results;
            try (var handle = runner.startBattleAsync(setup, bots)) {

                handle.getOnTickEvent().on(owner, tick -> {
                    int round = tick.getRoundNumber();
                    int turn = tick.getTurnNumber();
                    for (BotState bs : tick.getBotStates()) {
                        int id = bs.getId();
                        radarByBotRoundTurn
                                .computeIfAbsent(id, k -> new ConcurrentHashMap<>())
                                .computeIfAbsent(round, k -> new ConcurrentHashMap<>())
                                .put(turn, bs.getRadarDirection());
                    }
                });

                results = handle.awaitResults();
            }

            // Use BattleResults to find TimingBugBot's numeric ID (avoids onGameStarted race)
            int timingBotId = -1;
            System.out.println("Participants from results:");
            for (var br : results.getResults()) {
                System.out.printf("  id=%d  name=%s%n", br.getId(), br.getName());
                if ("TimingBugBot".equals(br.getName())) {
                    timingBotId = br.getId();
                }
            }
            System.out.println();

            if (timingBotId == -1) {
                System.out.println("❌ FAIL: TimingBugBot was not found in battle results");
                System.exit(1);
            }

            var radarByRoundTurn = radarByBotRoundTurn.get(timingBotId);

            // Verify radar moved ~45° between turn 1 and turn 2 in every round
            System.out.println("TimingBugBot (id=" + timingBotId + ") radar angles by round:");
            for (int round = 1; round <= NUM_ROUNDS; round++) {
                var turns = radarByRoundTurn != null ? radarByRoundTurn.get(round) : null;
                if (turns == null || !turns.containsKey(1)) {
                    failures.add("Round " + round + ": no radar data for turn 1");
                    System.out.printf("  Round %d: NO DATA%n", round);
                    continue;
                }
                double turn1Radar = turns.get(1);
                Double turn2Radar = turns.get(2);

                System.out.printf("  Round %d: turn1=%.1f°  turn2=%s%n",
                        round, turn1Radar,
                        turn2Radar != null ? String.format("%.1f°", turn2Radar) : "N/A");

                if (turn2Radar == null) {
                    failures.add("Round " + round + ": no radar data for turn 2");
                    continue;
                }

                // Radar should have moved by MAX_RADAR_TURN_RATE (45°) from turn 1 to turn 2
                double expectedTurn2 = normalizeAngle(turn1Radar + MAX_RADAR_TURN_RATE);
                double actual = normalizeAngle(turn2Radar);
                double diff = Math.abs(expectedTurn2 - actual);
                if (diff > 180) diff = 360 - diff;

                if (diff > 1.0) {
                    failures.add(String.format(
                            "Round %d: radar moved %.1f° (turn1=%.1f° → turn2=%.1f°), expected ~%.1f°",
                            round, turn2Radar - turn1Radar, turn1Radar, turn2Radar, MAX_RADAR_TURN_RATE));
                }
            }
        }

        System.out.println();
        System.out.println("=== Results ===");
        System.out.println("Failures: " + failures.size());
        failures.forEach(s -> System.out.println("  FAIL: " + s));

        if (failures.isEmpty()) {
            System.out.println("\n✅ PASS: Radar rotated " + MAX_RADAR_TURN_RATE + "° on turn 1 in all " + NUM_ROUNDS + " rounds.");
        } else {
            System.out.println("\n❌ FAIL");
            System.exit(1);
        }
    }

    private static double normalizeAngle(double angle) {
        angle %= 360;
        if (angle < 0) angle += 360;
        return angle;
    }
}
