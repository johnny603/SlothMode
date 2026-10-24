package io.slothmode.lockout;

import org.slf4j.Logger;
import java.util.Objects;
import org.slf4j.LoggerFactory;

/**
 * BRIDGE abstraction: the high-level "enforce a break" operation.
 *
 * Delegates the actual presentation to a {@link LockoutImplementor}. The
 * abstraction is free to add behavior (logging, session hooks) without
 * the implementor knowing.
 */
public final class LockoutAbstraction {

    private static final Logger log = LoggerFactory.getLogger(LockoutAbstraction.class);

    private final LockoutImplementor implementor;

    public LockoutAbstraction(LockoutImplementor implementor) {
        this.implementor = Objects.requireNonNull(implementor, "implementor");
    }

    /** Enforce a break of the given duration. Delegates to the implementor. */
    public void enforce(int durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("durationSeconds must be > 0, was " + durationSeconds);
        }
        log.info("Enforcing lockout for {}s via {}", durationSeconds, implementor.getClass().getSimpleName());
        implementor.present(durationSeconds);
    }

    /** Dismiss a lockout that is currently visible. Safe to call when none is. */
    public void release() {
        implementor.dismiss();
    }
}
