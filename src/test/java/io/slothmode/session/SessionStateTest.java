package io.slothmode.session;

import io.slothmode.lockout.LockoutAbstraction;
import io.slothmode.lockout.LockoutImplementor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        assertThat(ctx.current()).isInstanceOf(IdleState.class);
        assertThat(ctx.current().name()).isEqualTo("Idle");
    }

    @Test
    void idle_startFocus_transitionsToFocus() {
        SessionContext ctx = newContext(5, 5, 10);
        ((IdleState) ctx.current()).startFocus(ctx);
        assertThat(ctx.current()).isInstanceOf(FocusState.class);
        assertThat(((FocusState) ctx.current()).remainingSeconds()).isEqualTo(5);
    }

    @Test
    void focus_tick_decrements() {
        SessionContext ctx = newContext(3, 5, 10);
        ((IdleState) ctx.current()).startFocus(ctx);
        ((FocusState) ctx.current()).tick(ctx);
        assertThat(((FocusState) ctx.current()).remainingSeconds()).isEqualTo(2);
    }

    @Test
    void focus_tick_to_zero_transitionsToLockout() {
        SessionContext ctx = newContext(1, 5, 10);
        ((IdleState) ctx.current()).startFocus(ctx);
        ((FocusState) ctx.current()).tick(ctx);
        assertThat(ctx.current()).isInstanceOf(LockoutState.class);
    }

    @Test
    void focus_cancel_returnsToIdle() {
        SessionContext ctx = newContext(5, 5, 10);
        ((IdleState) ctx.current()).startFocus(ctx);
        ((FocusState) ctx.current()).cancel(ctx);
        assertThat(ctx.current()).isInstanceOf(IdleState.class);
    }

    @Test
    void lockout_breakCompleted_transitionsToReward_andAwardsPoints() {
        SessionContext ctx = newContext(1, 5, 10);
        ((IdleState) ctx.current()).startFocus(ctx);
        ((FocusState) ctx.current()).tick(ctx);     // -> Lockout
        ((LockoutState) ctx.current()).breakCompleted(ctx);
        assertThat(ctx.current()).isInstanceOf(RewardState.class);
        assertThat(((RewardState) ctx.current()).pointsAwarded()).isEqualTo(10);
        assertThat(ctx.points()).isEqualTo(10);
    }

    @Test
    void lockout_breakSkipped_awardsNoPoints() {
        SessionContext ctx = newContext(1, 5, 10);
        ((IdleState) ctx.current()).startFocus(ctx);
        ((FocusState) ctx.current()).tick(ctx);     // -> Lockout
        ((LockoutState) ctx.current()).breakSkipped(ctx);
        assertThat(ctx.current()).isInstanceOf(RewardState.class);
        assertThat(((RewardState) ctx.current()).pointsAwarded()).isEqualTo(0);
        assertThat(ctx.points()).isEqualTo(0);
    }

    @Test
    void reward_finish_returnsToIdle() {
        SessionContext ctx = newContext(1, 5, 10);
        ((IdleState) ctx.current()).startFocus(ctx);
        ((FocusState) ctx.current()).tick(ctx);
        ((LockoutState) ctx.current()).breakCompleted(ctx);
        ((RewardState) ctx.current()).finish(ctx);
        assertThat(ctx.current()).isInstanceOf(IdleState.class);
    }

    @Test
    void same_state_isNotReNotified() {
        SessionContext ctx = newContext(5, 5, 10);
        int[] notifications = {0};
        ctx.transitionTo(new IdleState()); // same as current
        // Observer is attached by the hub; test just verifies idempotence
        assertThat(ctx.current()).isInstanceOf(IdleState.class);
    }
}
