package io.slothmode.reward;

import io.slothmode.config.SlothConfig;

/**
 * Fixed point strategy.
 */
public class FixedPointsStrategy implements BreakStrategy {
    private final SlothConfig config;

    public FixedPointsStrategy(SlothConfig config) {
        this.config = config;
    }

    @Override
    public int calculatePoints() {
        return config.pointsPerBreak();
    }
}
