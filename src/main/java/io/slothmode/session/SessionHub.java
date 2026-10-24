package io.slothmode.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * MEDIATOR: coordinates the timer, the state machine, and the observers.
 *
 * The UI never talks to the states directly. It talks to the hub:
 * "start", "cancel", "query". The hub decides which state method to call.
 * Colleagues (SessionContext, SessionSubject) never reference each other.
 */
public final class SessionHub implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(SessionHub.class);

    private final SessionContext context;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> ticker;
    private volatile boolean running;

    public SessionHub(SessionContext context) {
        this.context = context;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "slothmode-session");
            t.setDaemon(true);
            return t;
        });
    }

    /** Begin a focus session. No-op if already running. */
    public synchronized void start() {
        if (running) {
            log.debug("Session already running");
            return;
        }
        running = true;
        SessionState state = context.current();
        if (state instanceof IdleState idle) {
            idle.startFocus(context);
        }
        scheduleTicker();
    }

    /** Cancel a focus session. No-op if not in Focus. */
    public synchronized void cancel() {
        SessionState state = context.current();
        if (state instanceof FocusState focus) {
            focus.cancel(context);
        }
        stopTicker();
        running = false;
    }

    /** Internal tick — called once per second while a session is active. */
    private void tick() {
        SessionState state = context.current();
        try {
            if (state instanceof FocusState focus) {
                focus.tick(context);
            } else if (state instanceof LockoutState) {
                // LockoutState.onEnter blocks until dismissal, so by the time we tick,
                // breakCompleted has already fired and we are in RewardState.
                // Nothing to do — the state transition already happened.
            } else if (state instanceof RewardState reward) {
                reward.finish(context);
                stopTicker();
                running = false;
            }
        } catch (RuntimeException e) {
            log.error("Tick failed in state {}", state.name(), e);
            stopTicker();
            running = false;
        }
    }

    private synchronized void scheduleTicker() {
        if (ticker != null && !ticker.isDone()) {
            return;
        }
        ticker = scheduler.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
    }

    private synchronized void stopTicker() {
        if (ticker != null) {
            ticker.cancel(false);
            ticker = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void close() {
        stopTicker();
        scheduler.shutdownNow();
    }
}
