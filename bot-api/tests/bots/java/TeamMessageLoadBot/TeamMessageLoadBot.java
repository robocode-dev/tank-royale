import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.TeamMessageBatch;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.TeamMessageEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Local trial workload: five copies broadcast one 64-message batch on each of 60 measured turns. */
public class TeamMessageLoadBot extends Bot {
    private final Map<Integer, String> lastBySender = new HashMap<>();
    private int received;
    private int outOfOrder;
    private final List<Integer> skippedTurnNumbers = new ArrayList<>();

    public static void main(String[] args) {
        new TeamMessageLoadBot().start();
    }

    @Override
    public void run() {
        while (isRunning()) {
            if (getTurnNumber() > 10 && getTurnNumber() <= 70) {
                List<String> messages = new ArrayList<>(64);
                for (int index = 0; index < 64; index++) {
                    messages.add(getTurnNumber() + ":" + index);
                }
                broadcastTeamMessageBatch(messages);
            }
            setTurnRight(1);
            go();
            if (getTurnNumber() >= 72) {
                writeMetrics();
            }
        }
    }

    @Override
    public void onTeamMessage(TeamMessageEvent event) {
        Object message = event.getMessage();
        if (message instanceof TeamMessageBatch batch) {
            for (Object payload : batch.getMessages()) recordMessage(event.getSenderId(), payload);
        } else {
            recordMessage(event.getSenderId(), message);
        }
    }

    private void recordMessage(int senderId, Object message) {
        received++;
        if (!(message instanceof String)) {
            outOfOrder++;
            return;
        }
        var payload = (String) message;
        var prior = lastBySender.put(senderId, payload);
        if (prior != null) {
            var parts = prior.split(":");
            int expectedTurn = Integer.parseInt(parts[0]);
            int expectedIndex = Integer.parseInt(parts[1]) + 1;
            if (expectedIndex == 64) { expectedIndex = 0; expectedTurn++; }
            if (!payload.equals(expectedTurn + ":" + expectedIndex)) outOfOrder++;
        }
    }

    @Override
    public void onSkippedTurn(SkippedTurnEvent event) {
        skippedTurnNumbers.add(event.getTurnNumber());
    }

    private void writeMetrics() {
        try {
            Path file = Path.of("team-message-load-" + getMyId() + ".txt");
            String skippedTurns = skippedTurnNumbers.stream().map(String::valueOf).collect(Collectors.joining("|"));
            if (skippedTurns.isEmpty()) skippedTurns = "-";
            Files.writeString(file, getTurnNumber() + "," + received + "," + outOfOrder + "," + skippedTurns);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
