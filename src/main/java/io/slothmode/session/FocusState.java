package io.slothmode.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STATE: a focus session is in progress. Singleton — the remaining time
 * lives in the SessionContext, not in this state object.
 */
public final class FocusState implements SessionState {

    private static final Logger log = LoggerFactory.getLogger(FocusState.class);
    private static final FocusState INSTANCE = new FocusState();

    private FocusState() {}

    public static FocusState instance() {
        return INSTANCE;
    }

    @Override
    public String name() {
        return "Focus";
    }

    @Override
    public void onEnter(SessionContext ctx) {
        log.info("Focus session started: {}s", ctx.remainingSeconds());
    }

    /** Called by the hub once per second. */
    public void tick(SessionContext ctx) {
        int next = ctx.remainingSeconds() - 1;
        if (next <= 0) {
            ctx.setRemainingSeconds(0);
            ctx.transitionTo(LockoutState.instance());
        } else {
            ctx.setRemainingSeconds(next);
            ctx.notifyTick(this);   // <-- notify without re-entering
        }
    }

    /** User abandoned the session before it completed. */
    public void cancel(SessionContext ctx) {
        log.info("Focus session cancelled with {}s remaining", ctx.remainingSeconds());
        ctx.transitionTo(IdleState.instance());
    }
}
