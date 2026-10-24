package io.slothmode.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STATE: nothing is happening. Singleton — the state has no data of its own.
 */
public final class IdleState implements SessionState {

    private static final Logger log = LoggerFactory.getLogger(IdleState.class);
    private static final IdleState INSTANCE = new IdleState();

    private IdleState() {}

    public static IdleState instance() {
        return INSTANCE;
    }

    @Override
    public String name() {
        return "Idle";
    }

    @Override
    public void onEnter(SessionContext ctx) {
        log.debug("Session is idle");
        ctx.resetTimer();
    }

    /** User clicked "Start focus". Transition to Focus. */
    public void startFocus(SessionContext ctx) {
        ctx.transitionTo(FocusState.instance());
    }
}
