package io.slothmode.session;

import io.slothmode.lockout.LockoutAbstraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * STATE Context: holds the current state AND the mutable session data
 * (remaining time, pending points, total points). The states are singletons
 * with no fields; all variability lives here.
 */
public final class SessionContext {

    private static final Logger log = LoggerFactory.getLogger(SessionContext.class);

    private final SessionConfig config;
    private final LockoutAbstraction lockout;
    private final SessionSubject subject;

    private volatile SessionState current;
    private volatile int remainingSeconds;
    private volatile int pendingPoints = Integer.MIN_VALUE;
    private volatile int points;

    public SessionContext(SessionConfig config,
                          LockoutAbstraction lockout,
                          SessionSubject subject) {
        this.config = Objects.requireNonNull(config, "config");
        this.lockout = Objects.requireNonNull(lockout, "lockout");
        this.subject = Objects.requireNonNull(subject, "subject");
        this.current = IdleState.instance();
        this.remainingSeconds = 0;
        this.points = 0;
    }

    public SessionConfig config() { return config; }
    public LockoutAbstraction lockout() { return lockout; }
    public SessionState current() { return current; }
    public int remainingSeconds() { return remainingSeconds; }
    public int pendingPoints() { return pendingPoints; }
    public int points() { return points; }

    void resetTimer() {
        this.remainingSeconds = 0;
    }

    void setRemainingSeconds(int seconds) {
        this.remainingSeconds = seconds;
    }

    void setPointsAwarded(int points) {
        this.pendingPoints = points;
    }

    void addPoints(int delta) {
        this.points += delta;
        this.pendingPoints = Integer.MIN_VALUE;   // consumed
    }

    /**
     * Move to a new state. Fires onEnter, notifies observers.
     * Because all states are singletons, ticking within Focus does NOT
     * go through here — it calls {@link #notifyTick} directly.
     */
    public synchronized void transitionTo(SessionState next) {
        Objects.requireNonNull(next, "next");
        SessionState previous = this.current;
        if (previous == next) {
            return;   // same singleton — no-op
        }
        previous.onExit(this);
        this.current = next;
        log.debug("Transition: {} -> {}", previous.name(), next.name());
        next.onEnter(this);
        subject.notifyStateChanged(previous, next);
    }

    /**
     * A tick within the current state (e.g. Focus counting down).
     * Updates observers but does NOT fire onEnter/onExit — this is not
     * a transition, just a data change.
     */
    void notifyTick(SessionState state) {
        subject.notifyStateChanged(state, state);
    }
}
