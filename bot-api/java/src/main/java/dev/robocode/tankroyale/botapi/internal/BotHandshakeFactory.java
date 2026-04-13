package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.mapper.InitialPositionMapper;
import dev.robocode.tankroyale.schema.BotHandshake;
import dev.robocode.tankroyale.schema.Message.Type;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;

/**
 * Utility class used for creating bot handshakes.
 */
final class BotHandshakeFactory {

    // Hide constructor to prevent instantiation
    private BotHandshakeFactory() {
    }

    static BotHandshake create(String sessionId, BotInfo botInfo, boolean isDroid, String secret) {
        BotHandshake handshake = new BotHandshake();
        handshake.setSessionId(sessionId);
        handshake.setType(Type.BOT_HANDSHAKE);
        handshake.setName(botInfo.getName());
        handshake.setVersion(botInfo.getVersion());
        handshake.setAuthors(new ArrayList<>(botInfo.getAuthors()));
        handshake.setDescription(botInfo.getDescription());
        handshake.setHomepage(botInfo.getHomepage());
        handshake.setCountryCodes(new ArrayList<>(botInfo.getCountryCodes()));
        handshake.setGameTypes(new ArrayList<>(botInfo.getGameTypes()));
        handshake.setPlatform(botInfo.getPlatform());
        handshake.setProgrammingLang(botInfo.getProgrammingLang());
        handshake.setInitialPosition(InitialPositionMapper.map(botInfo.getInitialPosition()));
        handshake.setTeamId(EnvVars.getTeamId());
        handshake.setTeamName(EnvVars.getTeamName());
        handshake.setTeamVersion(EnvVars.getTeamVersion());
        handshake.setIsDroid(isDroid);
        handshake.setSecret(secret);
        
        // Set debuggerAttached field (ADR-0035)
        boolean debuggerAttached = isDebuggerAttached();
        handshake.setDebuggerAttached(debuggerAttached);
        
        // Log hint if debugger is detected
        if (debuggerAttached) {
            System.out.println("Debugger detected. Consider enabling breakpoint mode for this bot in the controller.");
        }
        
        return handshake;
    }

    /**
     * Detects if a debugger is attached to the JVM.
     * @return true if a debugger is attached, false otherwise
     */
    private static boolean isDebuggerAttached() {
        // Check for ROBOCODE_DEBUG environment variable override
        String debugEnv = System.getenv("ROBOCODE_DEBUG");
        if ("true".equalsIgnoreCase(debugEnv)) {
            return true;
        }
        if ("false".equalsIgnoreCase(debugEnv)) {
            return false;
        }
        
        // Check for JDWP agent in JVM arguments
        return ManagementFactory.getRuntimeMXBean()
                .getInputArguments().stream()
                .anyMatch(arg -> arg.contains("jdwp"));
    }
}
