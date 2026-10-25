package io.slothmode.reward;

import io.slothmode.config.SlothConfig;

/**
 * Default point strategy.
 */
public class GentleSlothStrategy implements BreakStrategy {
    private final SlothConfig config;

    public GentleSlothStrategy(SlothConfig config) {
        this.config = config;
    }

    @Override
    public int calculatePoints() {
        return config.pointsPerBreak();
    }
}
