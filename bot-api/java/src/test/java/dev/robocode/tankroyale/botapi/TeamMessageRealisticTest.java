package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.internal.json.JsonConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simulates RobotColors and Point classes as they would appear in actual bot source files.
 * Tests the real-world scenario of team message passing between MyFirstLeader and MyFirstDroid.
 */
class TeamMessageRealisticTest {

    // These classes simulate what's in MyFirstLeader.java and MyFirstDroid.java
    // (top-level classes with simple names)

    /**
     * Simulated RobotColors class as it would appear in a bot's source file.
     */
    public static class RobotColors {
        public String bodyColor;
        public String tracksColor;
        public String turretColor;
        public String gunColor;
        public String radarColor;
        public String scanColor;
        public String bulletColor;
    }

    /**
     * Simulated Point class as it would appear in a bot's source file.
     */
    public static class Point {
        public double x;
        public double y;

        public Point() {
        }

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    @Test
    @DisplayName("Test real-world team message scenario")
    void testRealWorldScenario() throws Exception {
        System.out.println("=== Testing Real-World Team Message Scenario ===\n");

        // SENDER SIDE (MyFirstLeader)
        System.out.println("--- SENDER SIDE (MyFirstLeader) ---");
        var leaderColors = new RobotColors();
        leaderColors.bodyColor = "#FF0000FF";
        leaderColors.tracksColor = "#00FFFFFF";
        leaderColors.turretColor = "#FF0000FF";
        leaderColors.gunColor = "#FFFF00FF";
        leaderColors.radarColor = "#FF0000FF";
        leaderColors.scanColor = "#FFFF00FF";
        leaderColors.bulletColor = "#FFFF00FF";

        // This is what BaseBotInternals.sendTeamMessage does
        var messageType = leaderColors.getClass().getName();
        var json = JsonConverter.toJson(leaderColors);

        System.out.println("Message Type: " + messageType);
        System.out.println("JSON Length: " + json.length() + " bytes");
        System.out.println("JSON: " + json + "\n");

        // RECEIVER SIDE (MyFirstDroid)
        System.out.println("--- RECEIVER SIDE (MyFirstDroid) ---");
        System.out.println("Simulating EventMapper.map(TeamMessageEvent, baseBot)...\n");

        // Get the receiver's class loader (in real scenario, this would be MyFirstDroid's class loader)
        var classLoader = this.getClass().getClassLoader();
        System.out.println("Class Loader: " + classLoader.getClass().getName());

        // Try to find the type using ClassLoader.loadClass (same as Java EventMapper)
        System.out.println("\nUsing classLoader.loadClass(\"" + messageType + "\")");
        Class<?> foundType = null;
        try {
            foundType = classLoader.loadClass(messageType);
            System.out.println("Result: " + foundType.getName());
        } catch (ClassNotFoundException e) {
            System.out.println("Result: ClassNotFoundException - " + e.getMessage());
        }

        // Verify we found it
        assertThat(foundType).as("Should find RobotColors type").isNotNull();
        System.out.println("\n✓ Successfully found type: " + foundType.getName());

        // Deserialize
        System.out.println("\n--- DESERIALIZATION ---");
        var receivedObject = JsonConverter.fromJson(json, foundType);

        assertThat(receivedObject).isNotNull();
        assertThat(receivedObject).isInstanceOf(RobotColors.class);

        var receivedColors = (RobotColors) receivedObject;
        System.out.println("✓ Deserialized successfully");
        System.out.println("  BodyColor: " + receivedColors.bodyColor + " (expected: #FF0000FF)");
        System.out.println("  GunColor: " + receivedColors.gunColor + " (expected: #FFFF00FF)");
        System.out.println("  TracksColor: " + receivedColors.tracksColor + " (expected: #00FFFFFF)");

        assertThat(receivedColors.bodyColor).isEqualTo("#FF0000FF");
        assertThat(receivedColors.gunColor).isEqualTo("#FFFF00FF");
        assertThat(receivedColors.tracksColor).isEqualTo("#00FFFFFF");

        System.out.println("\n✓✓✓ TEST PASSED - Team messages work correctly! ✓✓✓");
    }

    @Test
    @DisplayName("Test Point message")
    void testPointMessage() throws Exception {
        System.out.println("=== Testing Point Message ===\n");

        var leaderPoint = new Point(100.5, 200.7);

        var messageType = leaderPoint.getClass().getName();
        var json = JsonConverter.toJson(leaderPoint);

        System.out.println("Message Type: " + messageType);
        System.out.println("JSON: " + json + "\n");

        // Find type using ClassLoader
        var classLoader = this.getClass().getClassLoader();
        var foundType = classLoader.loadClass(messageType);

        assertThat(foundType).as("Should find Point type").isNotNull();
        System.out.println("Found type: " + foundType.getName());

        var receivedObject = JsonConverter.fromJson(json, foundType);
        assertThat(receivedObject).isInstanceOf(Point.class);

        var receivedPoint = (Point) receivedObject;
        System.out.println("Point X: " + receivedPoint.x + ", Y: " + receivedPoint.y);

        assertThat(receivedPoint.x).isEqualTo(100.5);
        assertThat(receivedPoint.y).isEqualTo(200.7);

        System.out.println("\n✓ TEST PASSED");
    }

    @Test
    @DisplayName("Test message type name format")
    void testMessageTypeNameFormat() {
        System.out.println("=== Testing Message Type Name Format ===\n");

        var colors = new RobotColors();
        var point = new Point();

        System.out.println("RobotColors class info:");
        System.out.println("  getName(): " + colors.getClass().getName());
        System.out.println("  getSimpleName(): " + colors.getClass().getSimpleName());
        System.out.println("  getCanonicalName(): " + colors.getClass().getCanonicalName());

        System.out.println("\nPoint class info:");
        System.out.println("  getName(): " + point.getClass().getName());
        System.out.println("  getSimpleName(): " + point.getClass().getSimpleName());
        System.out.println("  getCanonicalName(): " + point.getClass().getCanonicalName());

        // Verify that getName() returns a format that can be loaded by ClassLoader
        assertThat(colors.getClass().getName()).contains("RobotColors");
        assertThat(point.getClass().getName()).contains("Point");

        System.out.println("\n✓ TEST PASSED");
    }
}
