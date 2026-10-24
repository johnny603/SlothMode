package io.slothmode.lockout;

/**
 * BRIDGE implementor: the low-level way a lockout is presented.
 *
 * The abstraction ({@link LockoutAbstraction}) holds a reference to this.
 * Implementors know how to show and hide themselves; they know nothing
 * about session state or planning.
 */
public interface LockoutImplementor {

    /**
     * Present the lockout. Blocks until the user has completed the break
     * (or, if an escape hatch fires, until the implementor has dismissed itself).
     *
     * @param durationSeconds how long the break is scheduled for
     */
    void present(int durationSeconds);

    /** Hide the lockout UI, if visible. Idempotent. */
    void dismiss();

    /** True if the escape hatch fired during the most recent present(). */
    boolean wasSkipped();

}
