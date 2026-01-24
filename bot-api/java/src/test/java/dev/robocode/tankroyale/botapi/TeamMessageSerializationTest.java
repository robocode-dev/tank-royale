package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.internal.json.JsonConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to simulate team message serialization/deserialization between MyFirstLeader and MyFirstDroid.
 * This test reproduces the exact scenario where colors are sent from leader to droid.
 */
class TeamMessageSerializationTest {

    // Simulated message classes from MyFirstLeader
    static class LeaderRobotColors {
        public int bodyColor;
        public int tracksColor;
        public int turretColor;
        public int gunColor;
        public int radarColor;
        public int scanColor;
        public int bulletColor;
    }

    static class LeaderPoint {
        public double x;
        public double y;
    }

    // Simulated message classes from MyFirstDroid (separate definitions)
    static class DroidRobotColors {
        public int bodyColor;
        public int tracksColor;
        public int turretColor;
        public int gunColor;
        public int radarColor;
        public int scanColor;
        public int bulletColor;
    }

    static class DroidPoint {
        public double x;
        public double y;
    }

    @Test
    @DisplayName("Test colors serialization/deserialization")
    void testColorsSerialization() throws Exception {
        System.out.println("=== Testing RobotColors Serialization ===");

        // Create colors message as MyFirstLeader would
        var leaderColors = new LeaderRobotColors();
        leaderColors.bodyColor = 0xFF0000FF; // Red
        leaderColors.tracksColor = 0x00FFFFFF; // Cyan
        leaderColors.turretColor = 0xFF0000FF; // Red
        leaderColors.gunColor = 0xFFFF00FF; // Yellow
        leaderColors.radarColor = 0xFF0000FF; // Red
        leaderColors.scanColor = 0xFFFF00FF; // Yellow
        leaderColors.bulletColor = 0xFFFF00FF; // Yellow

        // Simulate what BaseBotInternals.sendTeamMessage does
        var messageType = leaderColors.getClass().getName();
        var json = JsonConverter.toJson(leaderColors);

        System.out.println("Message Type: " + messageType);
        System.out.println("JSON: " + json);

        // Now simulate receiving on MyFirstDroid side
        System.out.println("\n=== Attempting to deserialize ===");

        // Use the same class loader as the bot would
        var classLoader = this.getClass().getClassLoader();
        var type = classLoader.loadClass(messageType);

        assertThat(type).isNotNull();
        System.out.println("Found type: " + type.getName());

        // Deserialize
        var receivedObject = JsonConverter.fromJson(json, type);

        assertThat(receivedObject).isNotNull();
        assertThat(receivedObject).isInstanceOf(LeaderRobotColors.class);

        var receivedColors = (LeaderRobotColors) receivedObject;
        System.out.println("BodyColor: " + Integer.toHexString(receivedColors.bodyColor));
        System.out.println("GunColor: " + Integer.toHexString(receivedColors.gunColor));

        assertThat(receivedColors.bodyColor).isEqualTo(0xFF0000FF);
        assertThat(receivedColors.gunColor).isEqualTo(0xFFFF00FF);

        System.out.println("\n✓ TEST PASSED");
    }

    @Test
    @DisplayName("Test point serialization/deserialization")
    void testPointSerialization() throws Exception {
        System.out.println("=== Testing Point Serialization ===");

        // Create point message as MyFirstLeader would
        var leaderPoint = new LeaderPoint();
        leaderPoint.x = 100.5;
        leaderPoint.y = 200.7;

        // Simulate what BaseBotInternals.sendTeamMessage does
        var messageType = leaderPoint.getClass().getName();
        var json = JsonConverter.toJson(leaderPoint);

        System.out.println("Message Type: " + messageType);
        System.out.println("JSON: " + json);

        // Now simulate receiving on MyFirstDroid side
        var classLoader = this.getClass().getClassLoader();
        var type = classLoader.loadClass(messageType);

        assertThat(type).isNotNull();
        System.out.println("Found type: " + type.getName());

        // Deserialize
        var receivedObject = JsonConverter.fromJson(json, type);

        assertThat(receivedObject).isNotNull();
        assertThat(receivedObject).isInstanceOf(LeaderPoint.class);

        var receivedPoint = (LeaderPoint) receivedObject;
        System.out.println("Point X: " + receivedPoint.x + ", Y: " + receivedPoint.y);

        assertThat(receivedPoint.x).isEqualTo(100.5);
        assertThat(receivedPoint.y).isEqualTo(200.7);

        System.out.println("\n✓ TEST PASSED");
    }

    @Test
    @DisplayName("Test class loader type resolution behavior")
    void testClassLoaderBehavior() throws Exception {
        System.out.println("=== Testing ClassLoader Behavior ===");

        var colors = new LeaderRobotColors();
        var point = new LeaderPoint();

        System.out.println("LeaderRobotColors.getName(): " + colors.getClass().getName());
        System.out.println("LeaderRobotColors.getSimpleName(): " + colors.getClass().getSimpleName());
        System.out.println("LeaderRobotColors.getCanonicalName(): " + colors.getClass().getCanonicalName());

        System.out.println("\nLeaderPoint.getName(): " + point.getClass().getName());
        System.out.println("LeaderPoint.getSimpleName(): " + point.getClass().getSimpleName());
        System.out.println("LeaderPoint.getCanonicalName(): " + point.getClass().getCanonicalName());

        // Test finding these types via ClassLoader
        var classLoader = this.getClass().getClassLoader();
        var foundColors = classLoader.loadClass(colors.getClass().getName());
        var foundPoint = classLoader.loadClass(point.getClass().getName());

        System.out.println("\nFound LeaderRobotColors using getName(): " + (foundColors != null));
        System.out.println("Found LeaderPoint using getName(): " + (foundPoint != null));

        assertThat(foundColors).isNotNull();
        assertThat(foundPoint).isNotNull();

        System.out.println("\n✓ TEST PASSED");
    }
}
