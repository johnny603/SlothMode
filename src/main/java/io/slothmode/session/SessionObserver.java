package io.slothmode.session;

/**
 * OBSERVER: an observer of the session. Called whenever the session
 * changes state (after the new state's onEnter has run).
 */
public interface SessionObserver {

    /** @param oldState the state we were in, or null on first attach */
    void onStateChanged(SessionState oldState, SessionState newState);
}
