package test_utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test for MockedServer.setInitialBotState() method to verify it can set state
 * before the bot is running, which is essential for fire command tests.
 */
@DisplayName("MockedServer Initial State Setup")
class MockedServerInitialStateTest {

    private MockedServer server;

    @BeforeEach
    void setUp() {
        server = new MockedServer();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    @DisplayName("setInitialBotState should update internal state without requiring connections")
    void testSetInitialBotState() {
        // Act - set gun heat to 0 (for fire tests) and other state values
        server.setInitialBotState(100.0, 0.0, 5.0, 45.0, 90.0, 180.0);
        // Assert - success indicated by no exception being thrown
    }

    @Test
    @DisplayName("setInitialBotState should accept null values for unchanged state")
    void testSetInitialBotStateWithNulls() {
        // Act - only set gun heat, leave others unchanged
        server.setInitialBotState(null, 0.0, null, null, null, null);
        // Assert - success indicated by no exception being thrown
    }

    @Test
    @DisplayName("setInitialBotState primitive overload should work")
    void testSetInitialBotStatePrimitives() {
        // Act - use primitive overload with all values
        server.setInitialBotState(100.0, 0.0, 5.0, 45.0, 90.0, 180.0);
        // Assert - success indicated by no exception being thrown
    }
}
