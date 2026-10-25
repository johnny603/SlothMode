package io.slothmode.session;

import io.slothmode.calendar.BreakRepository;
import io.slothmode.command.CommandHistory;
import io.slothmode.command.DocumentRegistry;
import io.slothmode.lockout.LockoutAbstraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class SessionContext {

    private static final Logger log = LoggerFactory.getLogger(SessionContext.class);

    private final SessionConfig config;
    private final LockoutAbstraction lockout;
    private final SessionSubject subject;
    private final CommandHistory commandHistory;
    private final DocumentRegistry documentRegistry;
    private final BreakRepository breakRepository;

    private volatile SessionState current;
    private volatile int remainingSeconds;
    private volatile int pendingPoints = Integer.MIN_VALUE;
    private volatile int points;

    public SessionContext(SessionConfig config,
                          LockoutAbstraction lockout,
                          SessionSubject subject,
                          BreakRepository breakRepository) {
        this.config = Objects.requireNonNull(config, "config");
        this.lockout = Objects.requireNonNull(lockout, "lockout");
        this.subject = Objects.requireNonNull(subject, "subject");
        this.breakRepository = Objects.requireNonNull(breakRepository, "breakRepository");
        this.commandHistory = new CommandHistory();
        this.documentRegistry = new DocumentRegistry();
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
    public CommandHistory commandHistory() { return commandHistory; }
    public DocumentRegistry documentRegistry() { return documentRegistry; }
    public BreakRepository breakRepository() { return breakRepository; }

    void resetTimer() { this.remainingSeconds = 0; }
    void setRemainingSeconds(int seconds) { this.remainingSeconds = seconds; }
    void setPointsAwarded(int points) { this.pendingPoints = points; }
    void addPoints(int delta) {
        this.points += delta;
        this.pendingPoints = Integer.MIN_VALUE;
    }

    public synchronized void transitionTo(SessionState next) {
        Objects.requireNonNull(next, "next");
        SessionState previous = this.current;
        if (previous == next) {
            return;
        }
        previous.onExit(this);
        this.current = next;
        log.debug("Transition: {} -> {}", previous.name(), next.name());
        next.onEnter(this);
        subject.notifyStateChanged(previous, next);
    }

    void notifyTick(SessionState state) {
        subject.notifyStateChanged(state, state);
    }
}
