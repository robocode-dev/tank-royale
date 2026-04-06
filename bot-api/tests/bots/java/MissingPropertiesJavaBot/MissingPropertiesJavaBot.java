package dev.robocode.tankroyale.botapi.tests;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;

import java.util.Collections;

public class MissingPropertiesJavaBot extends Bot {

    public static void main(String[] args) {
        new MissingPropertiesJavaBot().start();
    }

    public MissingPropertiesJavaBot() {
        super(new BotInfo(
            null, // Name is missing!
            "1.0.0",
            Collections.singletonList("Author"),
            "A bot missing its name",
            null,
            null,
            null,
            null,
            null,
            null
        ));
    }

    @Override
    public void run() {
        while (isRunning()) {
            forward(100);
        }
    }
}
