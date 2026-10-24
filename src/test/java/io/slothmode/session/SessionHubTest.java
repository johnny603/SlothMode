package io.slothmode.session;

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
        return new SessionContext(fast, abstraction, subject);
    }

    @Test
    void start_movesToFocus() {
        LockoutImplementor implementor = mock(LockoutImplementor.class);
        SessionContext ctx = fastContext(implementor, new SessionSubject());
        try (SessionHub hub = new SessionHub(ctx)) {
            hub.start();
            assertThat(ctx.current()).isSameAs(FocusState.instance());
            assertThat(hub.isRunning()).isTrue();
        }
    }

    @Test
    void cancel_returnsToIdle() {
        LockoutImplementor implementor = mock(LockoutImplementor.class);
        SessionContext ctx = fastContext(implementor, new SessionSubject());
        try (SessionHub hub = new SessionHub(ctx)) {
            hub.start();
            hub.cancel();
            assertThat(ctx.current()).isSameAs(IdleState.instance());
            assertThat(hub.isRunning()).isFalse();
        }
    }

    /**
     * Real fake (not a Mockito mock): present() returns immediately, so the
     * hub's tick handler sees LockoutState and next tick -> RewardState.
     * (A real FullscreenLockout blocks; a mock does not. This fake simulates
     * the "lockout dismissed immediately" case.)
     */
    static final class ImmediateLockout implements LockoutImplementor {
        @Override
        public void present(int durationSeconds) {
            // Return immediately — the break is "done" already.
        }

        @Override
        public void dismiss() {
        }

        @Override
        public boolean wasSkipped() {
           return false;
        }
    }

    @Test
    void fullCycle_invokesLockoutOnce_andAwardsPoints() throws InterruptedException {
        LockoutImplementor implementor = new ImmediateLockout();
        SessionSubject subject = new SessionSubject();

        CountDownLatch rewardEntered = new CountDownLatch(1);
        subject.attach((oldState, newState) -> {
            if (newState == RewardState.instance()) {
                rewardEntered.countDown();
            }
        });

        SessionContext ctx = fastContext(implementor, subject);
        try (SessionHub hub = new SessionHub(ctx)) {
            hub.start();
            boolean reached = rewardEntered.await(10, TimeUnit.SECONDS);
            assertThat(reached)
                    .as("session never reached RewardState within 10s")
                    .isTrue();
            assertThat(ctx.points()).isEqualTo(10);
        }
    }

    static final class SkippingLockout implements LockoutImplementor {
        private boolean skipped;
        @Override public void present(int durationSeconds) {
            this.skipped = true;
        }
        @Override public void dismiss() {}
        @Override public boolean wasSkipped() { return skipped; }
    }

    @Test
    void escapeHatch_awardsZeroPoints() throws InterruptedException {
        LockoutImplementor implementor = new SkippingLockout();
        SessionSubject subject = new SessionSubject();
        java.util.concurrent.CountDownLatch rewardEntered = new java.util.concurrent.CountDownLatch(1);
        subject.attach((o, n) -> {
            if (n == RewardState.instance()) rewardEntered.countDown();
        });
        SessionContext ctx = fastContext(implementor, subject);
        try (SessionHub hub = new SessionHub(ctx)) {
            hub.start();
            org.assertj.core.api.Assertions.assertThat(rewardEntered.await(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            org.assertj.core.api.Assertions.assertThat(ctx.points()).isEqualTo(0);
        }
    }
}
