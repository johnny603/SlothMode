package io.slothmode.session;

import io.slothmode.lockout.LockoutAbstraction;
import io.slothmode.lockout.LockoutImplementor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SessionStateTest {

    private SessionContext newContext(int focus, int lockout, int points) {
        LockoutImplementor implementor = mock(LockoutImplementor.class);
        LockoutAbstraction abstraction = new LockoutAbstraction(implementor);
        SessionSubject subject = new SessionSubject();
        return new SessionContext(
                new SessionConfig(focus, lockout, points),
                abstraction,
                subject);
    }

    @Test
    void initial_state_is_idle() {
        SessionContext ctx = newContext(5, 5, 10);
        assertThat(ctx.current()).isSameAs(IdleState.instance());
        assertThat(ctx.current().name()).isEqualTo("Idle");
    }

    @Test
    void idle_startFocus_transitionsToFocus() {
        SessionContext ctx = newContext(5, 5, 10);
        IdleState.instance().startFocus(ctx);
        assertThat(ctx.current()).isSameAs(FocusState.instance());
        // Focus duration comes from config, not from the state object.
        assertThat(ctx.remainingSeconds()).isEqualTo(5);
    }

    @Test
    void focus_tick_decrements_inContext() {
        SessionContext ctx = newContext(3, 5, 10);
        IdleState.instance().startFocus(ctx);
        ctx.setRemainingSeconds(3);
        FocusState.instance().tick(ctx);
        assertThat(ctx.remainingSeconds()).isEqualTo(2);
        assertThat(ctx.current()).isSameAs(FocusState.instance());
    }

    @Test
    void focus_tick_to_zero_transitionsToLockout() {
        SessionContext ctx = newContext(1, 5, 10);
        IdleState.instance().startFocus(ctx);
        ctx.setRemainingSeconds(1);
        FocusState.instance().tick(ctx);
        assertThat(ctx.current()).isSameAs(LockoutState.instance());
    }

    @Test
    void focus_cancel_returnsToIdle() {
        SessionContext ctx = newContext(5, 5, 10);
        IdleState.instance().startFocus(ctx);
        ctx.setRemainingSeconds(5);
        FocusState.instance().cancel(ctx);
        assertThat(ctx.current()).isSameAs(IdleState.instance());
    }

    @Test
    void lockout_breakCompleted_transitionsToReward_andAwardsPoints() {
        SessionContext ctx = newContext(1, 5, 10);
        IdleState.instance().startFocus(ctx);
        ctx.setRemainingSeconds(1);
        FocusState.instance().tick(ctx);              // -> Lockout
        LockoutState.instance().breakCompleted(ctx);  // -> Reward, awards 10
        assertThat(ctx.current()).isSameAs(RewardState.instance());
        assertThat(ctx.points()).isEqualTo(10);
    }

    @Test
    void lockout_breakSkipped_awardsNoPoints() {
        SessionContext ctx = newContext(1, 5, 10);
        IdleState.instance().startFocus(ctx);
        ctx.setRemainingSeconds(1);
        FocusState.instance().tick(ctx);
        LockoutState.instance().breakSkipped(ctx);
        assertThat(ctx.current()).isSameAs(RewardState.instance());
        assertThat(ctx.points()).isEqualTo(0);
    }

    @Test
    void reward_finish_returnsToIdle() {
        SessionContext ctx = newContext(1, 5, 10);
        IdleState.instance().startFocus(ctx);
        ctx.setRemainingSeconds(1);
        FocusState.instance().tick(ctx);
        LockoutState.instance().breakCompleted(ctx);
        RewardState.instance().finish(ctx);
        assertThat(ctx.current()).isSameAs(IdleState.instance());
    }

    @Test
    void transitioning_to_same_singleton_isNoOp() {
        SessionContext ctx = newContext(5, 5, 10);
        // Already Idle. Transitioning to Idle again must not re-fire onEnter.
        ctx.transitionTo(IdleState.instance());
        assertThat(ctx.current()).isSameAs(IdleState.instance());
    }
}
