import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.TeamMessageBatch;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.TeamMessageEvent;
import dev.robocode.tankroyale.botapi.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Five-bot batch stress workload used by the local CH-047 acceptance trial. */
public class TeamMessageBatchStress extends Bot {
    private static final int SEND_TURNS = 60;
    private static final int FIRST_SEND_TURN = 11;
    private static final int LAST_SEND_TURN = FIRST_SEND_TURN + SEND_TURNS - 1;
    private static final int ITEMS_PER_BATCH = 128;

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
    private int maxMessageWorkMicros;
    private int maxTeamMessageHandlerMicros;

    public static void main(String[] args) {
        new TeamMessageBatchStress().start();
    }

    @Override
    public void run() {
        while (isRunning()) {
            int turn = getTurnNumber();
            long messageWorkStartedAt = System.nanoTime();
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
                maxMessageWorkMicros = Math.max(maxMessageWorkMicros,
                        (int) ((System.nanoTime() - messageWorkStartedAt) / 1_000));
            }
            if (turn >= LAST_SEND_TURN + 10 || turn % 5 == 0) {
                setBodyColor(Color.fromRgb((receivedItems >>> 8) & 0xff, receivedItems & 0xff,
                        Math.min(protocolErrors, 0xff)));
                setTracksColor(Color.fromRgb(0, Math.min(skippedTurns, 0xff), 0));
                setTurretColor(Color.fromRgb(Math.min(typeErrors, 0xff), Math.min(sizeErrors, 0xff),
                        Math.min(orderErrors, 0xff)));
                int averageTimeLeftMicros = timeLeftSamples == 0 ? 0
                        : (int) Math.min(0xffff, totalTimeLeftMicros / timeLeftSamples);
                setRadarColor(Color.fromRgb(Math.min(contentErrors, 0xff),
                        (averageTimeLeftMicros >>> 8) & 0xff, averageTimeLeftMicros & 0xff));
                setScanColor(encodedColor(minTimeLeftMicros == Integer.MAX_VALUE ? 0 : minTimeLeftMicros));
                setGunColor(encodedColor(maxMessageWorkMicros));
                setBulletColor(encodedColor(maxTeamMessageHandlerMicros));
            }
            go();
        }
    }

    @Override
    public void onTeamMessage(TeamMessageEvent event) {
        long handlerStartedAt = System.nanoTime();
        try {
            handleTeamMessage(event);
        } finally {
            maxTeamMessageHandlerMicros = Math.max(maxTeamMessageHandlerMicros,
                    (int) ((System.nanoTime() - handlerStartedAt) / 1_000));
        }
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
            // Count a gap, but keep validating later deliveries so the report shows total
            // traffic as well as missing turns instead of treating one gap as permanent loss.
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
        if (event.getTurnNumber() >= FIRST_SEND_TURN && event.getTurnNumber() <= LAST_SEND_TURN) {
            skippedTurns++;
        }
    }
}
