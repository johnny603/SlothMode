package io.slothmode.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STATE: points are awarded, then the session returns to Idle. Singleton.
 */
public final class RewardState implements SessionState {

    private static final Logger log = LoggerFactory.getLogger(RewardState.class);
    private static final RewardState INSTANCE = new RewardState();

    private RewardState() {}

    public static RewardState instance() {
        return INSTANCE;
    }

    @Override
    public String name() {
        return "Reward";
    }

    @Override
    public void onEnter(SessionContext ctx) {
        int delta = ctx.pendingPoints() == Integer.MIN_VALUE
                ? ctx.config().pointsPerBreak()
                : ctx.pendingPoints();
        ctx.commandHistory().execute(new io.slothmode.reward.AwardRewardCommand(ctx, delta));
        log.info("Awarding {} points", delta);
    }

    public void finish(SessionContext ctx) {
        ctx.transitionTo(IdleState.instance());
    }
}
