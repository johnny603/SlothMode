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
        int awarded = ctx.pendingPoints();
        if (awarded == Integer.MIN_VALUE) {
            awarded = ctx.config().pointsPerBreak();   // default award
        }
        log.info("Awarding {} points", awarded);
        ctx.addPoints(awarded);
    }

    public void finish(SessionContext ctx) {
        ctx.transitionTo(IdleState.instance());
    }
}
