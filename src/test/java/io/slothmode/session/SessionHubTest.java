package io.slothmode.session;

import io.slothmode.calendar.BreakRepository;
import io.slothmode.calendar.ManualSource;
import io.slothmode.lockout.LockoutAbstraction;
import io.slothmode.lockout.LockoutImplementor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SessionHubTest {

    private SessionContext fastContext(LockoutImplementor implementor, SessionSubject subject) {
        SessionConfig fast = new SessionConfig(1, 1, 10);
        LockoutAbstraction abstraction = new LockoutAbstraction(implementor);
        BreakRepository breakRepo = new BreakRepository(new ManualSource());
        return new SessionContext(fast, abstraction, subject, breakRepo);
    }

    private SessionContext fastContext(LockoutImplementor implementor) {
        return fastContext(implementor, new SessionSubject());
    }

    @Test
    void start_movesToFocus() {
        LockoutImplementor implementor = mock(LockoutImplementor.class);
        SessionContext ctx = fastContext(implementor);
        try (SessionHub hub = new SessionHub(ctx)) {
            hub.start();
            assertThat(ctx.current()).isSameAs(FocusState.instance());
            assertThat(hub.isRunning()).isTrue();
        }
    }

    @Test
    void cancel_returnsToIdle() {
        LockoutImplementor implementor = mock(LockoutImplementor.class);
        SessionContext ctx = fastContext(implementor);
        try (SessionHub hub = new SessionHub(ctx)) {
            hub.start();
            hub.cancel();
            assertThat(ctx.current()).isSameAs(IdleState.instance());
            assertThat(hub.isRunning()).isFalse();
        }
    }

    static final class ImmediateLockout implements LockoutImplementor {
        @Override public void present(int durationSeconds) { /* immediate */ }
        @Override public void dismiss() { }
        @Override public boolean wasSkipped() { return false; }
    }

    static final class SkippingLockout implements LockoutImplementor {
        private boolean skipped;
        @Override public void present(int durationSeconds) { this.skipped = true; }
        @Override public void dismiss() { }
        @Override public boolean wasSkipped() { return skipped; }
    }

    @Test
    void fullCycle_invokesLockoutOnce_andAwardsPoints() throws InterruptedException {
        LockoutImplementor implementor = new ImmediateLockout();
        SessionSubject subject = new SessionSubject();
        CountDownLatch rewardEntered = new CountDownLatch(1);
        subject.attach((oldState, newState) -> {
            if (newState == RewardState.instance()) rewardEntered.countDown();
        });

        SessionContext ctx = fastContext(implementor, subject);
        try (SessionHub hub = new SessionHub(ctx)) {
            hub.start();
            assertThat(rewardEntered.await(10, TimeUnit.SECONDS))
                    .as("session never reached RewardState within 10s")
                    .isTrue();
            assertThat(ctx.points()).isEqualTo(10);
        }
    }

    @Test
    void escapeHatch_awardsZeroPoints() throws InterruptedException {
        LockoutImplementor implementor = new SkippingLockout();
        SessionSubject subject = new SessionSubject();
        CountDownLatch rewardEntered = new CountDownLatch(1);
        subject.attach((oldState, newState) -> {
            if (newState == RewardState.instance()) rewardEntered.countDown();
        });

        SessionContext ctx = fastContext(implementor, subject);
        try (SessionHub hub = new SessionHub(ctx)) {
            hub.start();
            assertThat(rewardEntered.await(10, TimeUnit.SECONDS)).isTrue();
            assertThat(ctx.points()).isEqualTo(0);
        }
    }
}
