package io.slothmode.reward;

import io.slothmode.session.SessionConfig;

/**
 * @deprecated Rewards for resting, not grinding.
 * Streak points are intentionally not wired to the UI.
 */
@Deprecated
public class StreakPointsStrategy implements BreakStrategy {
    private final SessionConfig config;
    private int currentStreak = 0;

    public StreakPointsStrategy(SessionConfig config) {
        this.config = config;
    }

    @Override
    public int calculatePoints() {
        currentStreak++;
        return config.pointsPerBreak() + (currentStreak * 2);
    }
}
