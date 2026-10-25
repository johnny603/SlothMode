package io.slothmode.calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * OBSERVER: caches the merged break list and invalidates on write.
 *
 * Observers are notified as Runnables (no args) when the cache is
 * invalidated. The real {@link io.slothmode.session.SessionHub} subscribes
 * in M5; for M4, tests attach a counter to prove notifications fire.
 */
public final class BreakRepository {

    private static final Logger log = LoggerFactory.getLogger(BreakRepository.class);

    private final BreakSource source;
    private final List<Runnable> observers = new ArrayList<>();
    private List<Break> cache;

    public BreakRepository(BreakSource source) {
        this.source = source;
    }

    public synchronized List<Break> breaks() {
        if (cache == null) {
            cache = source.listBreaks();
            log.debug("Loaded {} break(s) from {}", cache.size(), source.name());
        }
        return cache;
    }

    public void attach(Runnable observer) {
        observers.add(observer);
    }

    public void detach(Runnable observer) {
        observers.remove(observer);
    }

    /** Drop the cache and ping every observer. */
    public synchronized void invalidate() {
        cache = null;
        log.debug("BreakRepository invalidated, source={}", source.name());
        for (Runnable r : observers) {
            try {
                r.run();
            } catch (RuntimeException e) {
                log.warn("Break observer threw", e);
            }
        }
    }

    public BreakSource source() {
        return source;
    }
}
