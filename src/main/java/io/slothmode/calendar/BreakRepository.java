package io.slothmode.calendar;

import io.slothmode.session.SessionObserver;
import io.slothmode.session.SessionSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * OBSERVER: caches the merged break list and invalidates on write.
 *
 * The repository is the Subject; the SessionSubject from M2 is a *different*
 * Subject (session state). Here we only fire when the break list changes,
 * and the session observes it directly via {@link SessionSubject}.
 *
 * For M4 simplicity, the repository is also the Subject: it maintains its
 * own observer list of {@link Runnable}s that want "breaks changed" pings.
 */
public final class BreakRepository {

    private static final Logger log = LoggerFactory.getLogger(BreakRepository.class);

    private final BreakSource source;
    private final SessionSubject sessionSubject;
    private List<Break> cache;

    public BreakRepository(BreakSource source, SessionSubject sessionSubject) {
        this.source = source;
        this.sessionSubject = sessionSubject;
    }

    public synchronized List<Break> breaks() {
        if (cache == null) {
            cache = source.listBreaks();
            log.debug("Loaded {} break(s) from {}", cache.size(), source.name());
        }
        return cache;
    }

    /** Drop the cache and ping the session so it re-plans. */
    public synchronized void invalidate() {
        cache = null;
        log.debug("BreakRepository invalidated, source={}", source.name());
        // The session observer contract only knows about SessionState;
        // for M4 we notify with the current state (a tick-style ping).
        sessionSubject.notifyStateChanged(null, null);
    }

    public BreakSource source() {
        return source;
    }
}
