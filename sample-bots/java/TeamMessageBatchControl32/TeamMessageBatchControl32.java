import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.TeamMessageEvent;
import dev.robocode.tankroyale.botapi.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/** Matched no-message control for the 32-entry local team-message trial. */
public class TeamMessageBatchControl32 extends Bot {
    private static final int FIRST_MEASURED_TURN = 11;
    private static final int LAST_MEASURED_TURN = 70;
    private static final int ITEMS_PER_BATCH = 32;

    private int skippedTurns;
    private int unexpectedMessages;
    private int minTimeLeftMicros = Integer.MAX_VALUE;
    private long totalTimeLeftMicros;
    private int timeLeftSamples;
    private long skippedTurnMask;

    public static void main(String[] args) {
        new TeamMessageBatchControl32().start();
    }

    @Override
    public void run() {
        while (isRunning()) {
            int turn = getTurnNumber();
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
            if (turn >= LAST_MEASURED_TURN + 10 || turn % 5 == 0) {
                int averageTimeLeftMicros = timeLeftSamples == 0 ? 0
                        : (int) Math.min(0xffff, totalTimeLeftMicros / timeLeftSamples);
                setBodyColor(Color.fromRgb(0, 0, Math.min(unexpectedMessages, 0xff)));
                setTracksColor(Color.fromRgb(0, Math.min(skippedTurns, 0xff), 0));
                // Keep the control's turn bitmap encoding identical to the stress bot's.
                setTurretColor(encodedColor((int) (skippedTurnMask & 0xffffff)));
                setRadarColor(Color.fromRgb(0, (averageTimeLeftMicros >>> 8) & 0xff,
                        averageTimeLeftMicros & 0xff));
                setScanColor(encodedColor(minTimeLeftMicros == Integer.MAX_VALUE ? 0 : minTimeLeftMicros));
                setGunColor(encodedColor((int) ((skippedTurnMask >>> 24) & 0xffffff)));
                setBulletColor(encodedColor((int) ((skippedTurnMask >>> 48) & 0xfff)));
            }
            go();
        }
    }

    @Override
    public void onSkippedTurn(SkippedTurnEvent event) {
        int turn = event.getTurnNumber();
        if (turn >= FIRST_MEASURED_TURN && turn <= LAST_MEASURED_TURN) {
            skippedTurns++;
            skippedTurnMask |= 1L << (turn - FIRST_MEASURED_TURN);
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
}
