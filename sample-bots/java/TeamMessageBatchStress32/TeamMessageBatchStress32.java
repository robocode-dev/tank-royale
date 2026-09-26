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
    private static final int SEND_TURNS = 60;
    private static final int FIRST_SEND_TURN = 11;
    private static final int LAST_SEND_TURN = FIRST_SEND_TURN + SEND_TURNS - 1;
    private static final int ITEMS_PER_BATCH = 32;

    private final Map<Integer, Integer> lastTurnBySender = new HashMap<>();
    private int receivedItems;
    private int protocolErrors;
    private int skippedTurns;
    private int typeErrors;
    private int sizeErrors;
    private int orderErrors;
    private int contentErrors;
    private int minTimeLeftMicros = Integer.MAX_VALUE;
    private long totalTimeLeftMicros;
    private int timeLeftSamples;
    private long skippedTurnMask;

    public static void main(String[] args) {
        new TeamMessageBatchStress32().start();
    }

    @Override
    public void run() {
        while (isRunning()) {
            int turn = getTurnNumber();
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
            if (turn >= LAST_SEND_TURN + 10 || turn % 5 == 0) {
                setBodyColor(Color.fromRgb((receivedItems >>> 8) & 0xff, receivedItems & 0xff,
                        Math.min(protocolErrors, 0xff)));
                setTracksColor(Color.fromRgb(0, Math.min(skippedTurns, 0xff), 0));
                // The integration test decodes the 60 measured turns from these three colors.
                setTurretColor(encodedColor((int) (skippedTurnMask & 0xffffff)));
                int averageTimeLeftMicros = timeLeftSamples == 0 ? 0
                        : (int) Math.min(0xffff, totalTimeLeftMicros / timeLeftSamples);
                setRadarColor(Color.fromRgb(Math.min(contentErrors, 0xff),
                        (averageTimeLeftMicros >>> 8) & 0xff, averageTimeLeftMicros & 0xff));
                setScanColor(encodedColor(minTimeLeftMicros == Integer.MAX_VALUE ? 0 : minTimeLeftMicros));
                setGunColor(encodedColor((int) ((skippedTurnMask >>> 24) & 0xffffff)));
                setBulletColor(encodedColor((int) ((skippedTurnMask >>> 48) & 0xfff)));
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

    @Override
    public void onSkippedTurn(SkippedTurnEvent event) {
        int turn = event.getTurnNumber();
        if (turn >= FIRST_SEND_TURN && turn <= LAST_SEND_TURN) {
            skippedTurns++;
            skippedTurnMask |= 1L << (turn - FIRST_SEND_TURN);
        }
    }
}
