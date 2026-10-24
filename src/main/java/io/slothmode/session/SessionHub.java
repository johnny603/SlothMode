package io.slothmode.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * MEDIATOR: coordinates the timer, the state machine, and the observers.
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

    public synchronized void cancel() {
        SessionState state = context.current();
        if (state instanceof FocusState focus) {
            focus.cancel(context);
        }
        stopTicker();
        running = false;
    }

    private void tick() {
        SessionState state = context.current();
        try {
            if (state instanceof FocusState focus) {
                focus.tick(context);
            } else if (state instanceof LockoutState lockout) {
                if (context.lockout().wasSkipped()) {
                    lockout.breakSkipped(context);
                } else {
                    lockout.breakCompleted(context);
                }
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
