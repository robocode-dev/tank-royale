import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.TeamMessageBatch;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.TeamMessageEvent;
import dev.robocode.tankroyale.botapi.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Five-bot 32-entry trial workload with per-turn skip telemetry. */
public class TeamMessageBatchStress32 extends Bot {
    private static final int SEND_TURNS = 1000;
    private static final int FIRST_SEND_TURN = 11;
    private static final int LAST_SEND_TURN = FIRST_SEND_TURN + SEND_TURNS - 1;
    private static final int ITEMS_PER_BATCH = 32;
    private static final int DIAGNOSTICS_FLUSH_TURN = LAST_SEND_TURN + 10;
    private static final boolean TIMING_DIAGNOSTICS_ENABLED = Boolean.parseBoolean(
            System.getenv().getOrDefault("ROBOCODE_TURN_TIMING_DIAGNOSTICS", "false"));

    private final Map<Integer, Integer> lastTurnBySender = new HashMap<>();
    private final List<String> timingRecords = new ArrayList<>(SEND_TURNS + 8);
    private final List<String> skippedTurnRecords = new ArrayList<>();
    private final List<Integer> skippedTurnNumbers = new ArrayList<>();
    private int receivedItems;
    private int protocolErrors;
    private int skippedTurns;
    private int typeErrors;
    private int sizeErrors;
    private int orderErrors;
    private int contentErrors;
    private boolean timingBatchReported;
    private int minTimeLeftMicros = Integer.MAX_VALUE;
    private long totalTimeLeftMicros;
    private int timeLeftSamples;

    public static void main(String[] args) {
        new TeamMessageBatchStress32().start();
    }

    @Override
    public void run() {
        while (isRunning()) {
            int turn = getTurnNumber();
            boolean timedTurn = TIMING_DIAGNOSTICS_ENABLED && turn >= FIRST_SEND_TURN && turn <= LAST_SEND_TURN;
            long tickReceivedAtNanos = timedTurn ? System.nanoTime() : 0;
            if (turn >= FIRST_SEND_TURN && turn <= LAST_SEND_TURN) {
                List<Object> messages = new ArrayList<>(ITEMS_PER_BATCH);
                for (int item = 0; item < ITEMS_PER_BATCH; item++) {
                    messages.add(getMyId() + ":" + turn + ":" + item);
                }
                broadcastTeamMessageBatch(messages);
                int timeLeftMicros = Math.max(0, getTimeLeft());
                minTimeLeftMicros = Math.min(minTimeLeftMicros, timeLeftMicros);
                totalTimeLeftMicros += timeLeftMicros;
                timeLeftSamples++;
            }
            if (turn % 5 == 0) {
                setBodyColor(Color.fromRgb((receivedItems >>> 16) & 0xff, (receivedItems >>> 8) & 0xff,
                        receivedItems & 0xff));
                setTracksColor(Color.fromRgb(Math.min(protocolErrors, 0xff),
                        Math.min(skippedTurns, 0xff), 0));
                // The integration test samples the rolling 60-turn skip bitmap on every tick.
                long skippedTurnMask = skippedTurnMaskAt(turn);
                setTurretColor(encodedColor((int) (skippedTurnMask & 0xffffff)));
                int averageTimeLeftMicros = timeLeftSamples == 0 ? 0
                        : (int) Math.min(0xffff, totalTimeLeftMicros / timeLeftSamples);
                setRadarColor(Color.fromRgb(Math.min(contentErrors, 0xff),
                        (averageTimeLeftMicros >>> 8) & 0xff, averageTimeLeftMicros & 0xff));
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
    public void onTeamMessage(TeamMessageEvent event) {
        handleTeamMessage(event);
    }

    private void handleTeamMessage(TeamMessageEvent event) {
        if (!(event.getMessage() instanceof TeamMessageBatch)) {
            protocolErrors++;
            typeErrors++;
            return;
        }
        List<Object> messages = ((TeamMessageBatch) event.getMessage()).getMessages();
        if (messages.size() != ITEMS_PER_BATCH) {
            protocolErrors++;
            sizeErrors++;
            return;
        }
        int senderId = event.getSenderId();
        String[] first = String.valueOf(messages.get(0)).split(":");
        if (first.length != 3 || Integer.parseInt(first[0]) != senderId) {
            protocolErrors++;
            contentErrors++;
            return;
        }
        int senderTurn = Integer.parseInt(first[1]);
        int previousSenderTurn = lastTurnBySender.getOrDefault(senderId, FIRST_SEND_TURN - 1);
        if (senderTurn <= previousSenderTurn) {
            protocolErrors++;
            orderErrors++;
            return;
        }
        if (senderTurn != previousSenderTurn + 1) {
            protocolErrors++;
            orderErrors++;
        }
        for (int item = 0; item < ITEMS_PER_BATCH; item++) {
            if (!(senderId + ":" + senderTurn + ":" + item).equals(messages.get(item))) {
                protocolErrors++;
                contentErrors++;
                return;
            }
        }
        lastTurnBySender.put(senderId, senderTurn);
        receivedItems += ITEMS_PER_BATCH;
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
                int bit = Math.floorMod(skippedTurn - FIRST_SEND_TURN, 60);
                mask |= 1L << bit;
            }
        }
        return mask;
    }

    @Override
    public void onSkippedTurn(SkippedTurnEvent event) {
        int turn = event.getTurnNumber();
        if (turn >= FIRST_SEND_TURN && turn <= LAST_SEND_TURN) {
            if (TIMING_DIAGNOSTICS_ENABLED) {
                skippedTurnRecords.add(turn + ":" + System.nanoTime());
            }
            skippedTurns++;
            skippedTurnNumbers.add(turn);
        }
    }
}
