package io.slothmode.reward;

import io.slothmode.session.SessionConfig;

/**
 * Default point strategy.
 */
public class GentleSlothStrategy implements BreakStrategy {
    private final SessionConfig config;

    public GentleSlothStrategy(SessionConfig config) {
        this.config = config;
    }

    @Override
    public int calculatePoints() {
        return config.pointsPerBreak();
    }
}
