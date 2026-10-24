package io.slothmode.session;

/**
 * Immutable tunables for the session state machine.
 * Kept in one record so tests can construct a fast config
 * (1-second focus, 1-second lockout) without touching the app defaults.
 */
public record SessionConfig(
        int focusSeconds,
        int lockoutSeconds,
        int pointsPerBreak
) {

    public static SessionConfig defaults() {
        return new SessionConfig(50 * 60, 10, 10);
    }

    public SessionConfig {
        if (focusSeconds <= 0) {
            throw new IllegalArgumentException("focusSeconds must be > 0, was " + focusSeconds);
        }
        if (lockoutSeconds <= 0) {
            throw new IllegalArgumentException("lockoutSeconds must be > 0, was " + lockoutSeconds);
        }
        if (pointsPerBreak < 0) {
            throw new IllegalArgumentException("pointsPerBreak must be >= 0, was " + pointsPerBreak);
        }
    }
}
