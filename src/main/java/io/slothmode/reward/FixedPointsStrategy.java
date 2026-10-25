package io.slothmode.reward;

import io.slothmode.session.SessionConfig;

/**
 * Fixed point strategy.
 */
public class FixedPointsStrategy implements BreakStrategy {
    private final SessionConfig config;

    public FixedPointsStrategy(SessionConfig config) {
        this.config = config;
    }

    @Override
    public int calculatePoints() {
        return config.pointsPerBreak();
    }
}
