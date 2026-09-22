import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.TeamMessageEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Local trial workload: five copies broadcast 128 messages on each of 60 turns. */
public class TeamMessageLoadBot extends Bot {
    private final Map<Integer, String> lastBySender = new HashMap<>();
    private int received;
    private int outOfOrder;
    private int skipped;

    public static void main(String[] args) {
        new TeamMessageLoadBot().start();
    }

    @Override
    public void run() {
        while (isRunning()) {
            if (getTurnNumber() <= 60) {
                for (int index = 0; index < 128; index++) {
                    broadcastTeamMessage(getTurnNumber() + ":" + index);
                }
            }
            setTurnRight(1);
            go();
            if (getTurnNumber() >= 62) {
                writeMetrics();
            }
        }
    }

    @Override
    public void onTeamMessage(TeamMessageEvent event) {
        received++;
        var payload = (String) event.getMessage();
        var prior = lastBySender.put(event.getSenderId(), payload);
        if (prior != null) {
            var parts = prior.split(":");
            int expectedTurn = Integer.parseInt(parts[0]);
            int expectedIndex = Integer.parseInt(parts[1]) + 1;
            if (expectedIndex == 128) { expectedIndex = 0; expectedTurn++; }
            if (!payload.equals(expectedTurn + ":" + expectedIndex)) outOfOrder++;
        }
    }

    @Override
    public void onSkippedTurn(SkippedTurnEvent event) {
        skipped++;
    }

    private void writeMetrics() {
        try {
            Path file = Path.of("team-message-load-" + getMyId() + ".txt");
            Files.writeString(file, getTurnNumber() + "," + received + "," + outOfOrder + "," + skipped);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
