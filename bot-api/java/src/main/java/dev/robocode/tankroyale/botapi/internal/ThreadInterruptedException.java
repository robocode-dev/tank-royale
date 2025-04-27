package dev.robocode.tankroyale.botapi.internal;

/**
 * Exception used for interrupting event handlers.
 * <p>
 * This exception is thrown to signal that the current event handler has been interrupted deliberately and
 * processing should stop so another event can take place.
 */
final class ThreadInterruptedException extends Error {
}
