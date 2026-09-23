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

    public static void main(String[] args) {
        new TeamMessageBatchStress().start();
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
            }
            if (turn >= LAST_SEND_TURN + 10 || turn % 5 == 0) {
                setBodyColor(Color.fromRgb((receivedItems >>> 8) & 0xff, receivedItems & 0xff,
                        Math.min(protocolErrors, 0xff)));
                setTracksColor(Color.fromRgb(0, Math.min(skippedTurns, 0xff), 0));
                setTurretColor(Color.fromRgb(Math.min(typeErrors, 0xff), Math.min(sizeErrors, 0xff),
                        Math.min(orderErrors, 0xff)));
                setRadarColor(Color.fromRgb(Math.min(contentErrors, 0xff), 0, 0));
            }
            go();
        }
    }

    @Override
    public void onTeamMessage(TeamMessageEvent event) {
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

    @Override
    public void onSkippedTurn(SkippedTurnEvent event) {
        if (event.getTurnNumber() >= FIRST_SEND_TURN && event.getTurnNumber() <= LAST_SEND_TURN) {
            skippedTurns++;
        }
    }
}
