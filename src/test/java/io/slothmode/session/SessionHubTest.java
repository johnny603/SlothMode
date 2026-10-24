package io.slothmode.session;

import io.slothmode.lockout.LockoutAbstraction;
import io.slothmode.lockout.LockoutImplementor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SessionHubTest {

    private SessionContext fastContext(LockoutImplementor implementor) {
        SessionConfig fast = new SessionConfig(1, 1, 10);
        SessionSubject subject = new SessionSubject();
        LockoutAbstraction abstraction = new LockoutAbstraction(implementor);
        SessionContext ctx = new SessionContext(fast, abstraction, subject);
        AtomicInteger points = new AtomicInteger();
        subject.attach((o, n) -> points.set(n instanceof RewardState r ? r.pointsAwarded() : 0));
        return ctx;
    }

    @Test
    void start_movesToFocus() {
        LockoutImplementor implementor = mock(LockoutImplementor.class);
        SessionContext ctx = fastContext(implementor);
        try (SessionHub hub = new SessionHub(ctx)) {
            hub.start();
            assertThat(ctx.current()).isInstanceOf(FocusState.class);
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
            assertThat(ctx.current()).isInstanceOf(IdleState.class);
            assertThat(hub.isRunning()).isFalse();
        }
    }

    @Test
    void fullCycle_invokesLockoutOnce() throws InterruptedException {
        LockoutImplementor implementor = mock(LockoutImplementor.class);
        SessionContext ctx = fastContext(implementor);
        try (SessionHub hub = new SessionHub(ctx)) {
            hub.start();
            // focus (1s) + lockout (1s) + reward tick + safety margin
            Thread.sleep(3500);
            assertThat(ctx.points()).isEqualTo(10);
            verify(implementor).present(1);
        }
    }
}
