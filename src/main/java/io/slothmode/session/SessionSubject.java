package io.slothmode.session;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * OBSERVER Subject: maintains the observer list, notifies on change.
 *
 * CopyOnWriteArrayList because observers may add/remove themselves
 * during a notification, and we don't want ConcurrentModificationException.
 */
public final class SessionSubject {

    private final List<SessionObserver> observers = new CopyOnWriteArrayList<>();

    public void attach(SessionObserver observer) {
        observers.add(Objects.requireNonNull(observer, "observer"));
    }

    public void detach(SessionObserver observer) {
        observers.remove(observer);
    }

    public void notifyStateChanged(SessionState oldState, SessionState newState) {
        for (SessionObserver o : observers) {
            try {
                o.onStateChanged(oldState, newState);
            } catch (RuntimeException e) {
                // One misbehaving observer must not break the others.
                // (Same rule the Observer pattern warns about: "unexpected updates".)
                // We log and continue.
                System.err.println("Observer " + o + " threw: " + e);
            }
        }
    }

    public int observerCount() {
        return observers.size();
    }
}
