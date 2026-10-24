package io.slothmode.session;

import io.slothmode.lockout.LockoutAbstraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STATE: the break is being enforced. Singleton.
 */
public final class LockoutState implements SessionState {

    private static final Logger log = LoggerFactory.getLogger(LockoutState.class);
    private static final LockoutState INSTANCE = new LockoutState();

    private LockoutState() {}

    public static LockoutState instance() {
        return INSTANCE;
    }

    @Override
    public String name() {
        return "Lockout";
    }

    @Override
    public void onEnter(SessionContext ctx) {
        int duration = ctx.config().lockoutSeconds();
        log.info("Lockout started: {}s", duration);
        LockoutAbstraction lockout = ctx.lockout();
        lockout.enforce(duration);
    }

    public void breakCompleted(SessionContext ctx) {
        log.info("Break completed");
        ctx.transitionTo(RewardState.instance());
    }

    public void breakSkipped(SessionContext ctx) {
        log.warn("Break skipped via escape hatch");
        ctx.setPointsAwarded(0);
        ctx.transitionTo(RewardState.instance());
    }
}
