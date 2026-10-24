package io.slothmode.session;

/**
 * STATE: the four legal states of a SlothMode session.
 *
 * A sealed interface means only these four records can implement it,
 * so the compiler knows every branch a switch must handle.
 * Adding a fifth state is a compile-time change everywhere,
 * which is exactly what we want from a state machine.
 */
public sealed interface SessionState
        permits IdleState, FocusState, LockoutState, RewardState {

    /** Short name for logs and UI. */
    String name();

    /** Called once when this state becomes active. */
    default void onEnter(SessionContext ctx) {}

    /** Called once when this state is being left. */
    default void onExit(SessionContext ctx) {}
}
