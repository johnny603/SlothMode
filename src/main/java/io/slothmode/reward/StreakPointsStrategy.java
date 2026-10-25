package io.slothmode.reward;

import io.slothmode.config.SlothConfig;

/**
 * @deprecated Rewards for resting, not grinding.
 * Streak points are intentionally not wired to the UI.
 */
@Deprecated
public class StreakPointsStrategy implements BreakStrategy {
    private final SlothConfig config;
    private int currentStreak = 0;

    public StreakPointsStrategy(SlothConfig config) {
        this.config = config;
    }

    @Override
    public int calculatePoints() {
        currentStreak++;
        return config.pointsPerBreak() + (currentStreak * 2);
    }
}
