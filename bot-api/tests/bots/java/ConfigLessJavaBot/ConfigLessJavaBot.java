package dev.robocode.tankroyale.botapi.tests;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ConfigLessJavaBot extends Bot {

    public static void main(String[] args) {
        new ConfigLessJavaBot().start();
    }

    public ConfigLessJavaBot() {
        super(new BotInfo(
            "ConfigLessJavaBot",
            "1.0.0",
            Collections.singletonList("Author"),
            "A bot without a .json file",
            null,
            Collections.singletonList("US"),
            new HashSet<>(Collections.singletonList("classic")),
            "jvm",
            "java",
            null
        ));
    }

    @Override
    public void run() {
        while (isRunning()) {
            forward(100);
            turnLeft(90);
        }
    }
}
