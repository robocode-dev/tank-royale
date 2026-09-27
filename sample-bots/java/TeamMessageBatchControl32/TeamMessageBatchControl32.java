import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.TeamMessageEvent;
import dev.robocode.tankroyale.botapi.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/** Matched no-message control for the 32-entry local team-message trial. */
public class TeamMessageBatchControl32 extends Bot {
    private static final int FIRST_MEASURED_TURN = 11;
    private static final int MEASURED_SEND_TURNS = 1000;
    private static final int LAST_MEASURED_TURN = FIRST_MEASURED_TURN + MEASURED_SEND_TURNS - 1;
    private static final int ITEMS_PER_BATCH = 32;
    private static final int DIAGNOSTICS_FLUSH_TURN = LAST_MEASURED_TURN + 10;
    private static final boolean TIMING_DIAGNOSTICS_ENABLED = Boolean.parseBoolean(
            System.getenv().getOrDefault("ROBOCODE_TURN_TIMING_DIAGNOSTICS", "false"));

    private final List<String> timingRecords = new ArrayList<>(MEASURED_SEND_TURNS + 8);
    private final List<String> skippedTurnRecords = new ArrayList<>();
    private final List<Integer> skippedTurnNumbers = new ArrayList<>();
    private int skippedTurns;
    private int unexpectedMessages;
    private int minTimeLeftMicros = Integer.MAX_VALUE;
    private long totalTimeLeftMicros;
    private int timeLeftSamples;
    private boolean timingBatchReported;

    public static void main(String[] args) {
        new TeamMessageBatchControl32().start();
    }

    @Override
    public void run() {
        while (isRunning()) {
            int turn = getTurnNumber();
            boolean timedTurn = TIMING_DIAGNOSTICS_ENABLED && turn >= FIRST_MEASURED_TURN && turn <= LAST_MEASURED_TURN;
            long tickReceivedAtNanos = timedTurn ? System.nanoTime() : 0;
            if (turn >= FIRST_MEASURED_TURN && turn <= LAST_MEASURED_TURN) {
                List<Object> messages = new ArrayList<>(ITEMS_PER_BATCH);
                for (int item = 0; item < ITEMS_PER_BATCH; item++) {
                    messages.add(getMyId() + ":" + turn + ":" + item);
                }
                int timeLeftMicros = Math.max(0, getTimeLeft());
                minTimeLeftMicros = Math.min(minTimeLeftMicros, timeLeftMicros);
                totalTimeLeftMicros += timeLeftMicros;
                timeLeftSamples++;
            }
            if (turn % 5 == 0) {
                int averageTimeLeftMicros = timeLeftSamples == 0 ? 0
                        : (int) Math.min(0xffff, totalTimeLeftMicros / timeLeftSamples);
                setBodyColor(Color.fromRgb(0, 0, Math.min(unexpectedMessages, 0xff)));
                setTracksColor(Color.fromRgb(0, Math.min(skippedTurns, 0xff), 0));
                long skippedTurnMask = skippedTurnMaskAt(turn);
                setTurretColor(encodedColor((int) (skippedTurnMask & 0xffffff)));
                setRadarColor(Color.fromRgb(0, (averageTimeLeftMicros >>> 8) & 0xff,
                        averageTimeLeftMicros & 0xff));
                setScanColor(encodedColor(minTimeLeftMicros == Integer.MAX_VALUE ? 0 : minTimeLeftMicros));
                setGunColor(encodedColor((int) ((skippedTurnMask >>> 24) & 0xffffff)));
                setBulletColor(encodedColor((int) ((skippedTurnMask >>> 48) & 0xfff)));
            }
            if (timedTurn) {
                timingRecords.add(turn + ":" + tickReceivedAtNanos + ":" + System.nanoTime());
            }
            if (TIMING_DIAGNOSTICS_ENABLED && !timingBatchReported && turn == DIAGNOSTICS_FLUSH_TURN) {
                System.out.println("BOT_TIMING_BATCH botId=" + getMyId()
                        + " turns=" + String.join(",", timingRecords)
                        + " skipped=" + String.join(",", skippedTurnRecords));
                timingBatchReported = true;
            }
            go();
        }
    }

    @Override
    public void onSkippedTurn(SkippedTurnEvent event) {
        int turn = event.getTurnNumber();
        if (turn >= FIRST_MEASURED_TURN && turn <= LAST_MEASURED_TURN) {
            if (TIMING_DIAGNOSTICS_ENABLED) {
                skippedTurnRecords.add(turn + ":" + System.nanoTime());
            }
            skippedTurns++;
            skippedTurnNumbers.add(turn);
        }
    }

    @Override
    public void onTeamMessage(TeamMessageEvent event) {
        unexpectedMessages++;
    }

    private static Color encodedColor(int value) {
        int rgb = Math.min(Math.max(value, 0), 0xffffff);
        return Color.fromRgb((rgb >>> 16) & 0xff, (rgb >>> 8) & 0xff, rgb & 0xff);
    }

    private long skippedTurnMaskAt(int turn) {
        long mask = 0;
        int oldestTurn = turn - 59;
        for (int skippedTurn : skippedTurnNumbers) {
            if (skippedTurn >= oldestTurn && skippedTurn <= turn) {
                int bit = Math.floorMod(skippedTurn - FIRST_MEASURED_TURN, 60);
                mask |= 1L << bit;
            }
        }
        return mask;
    }
}
