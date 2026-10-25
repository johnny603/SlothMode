package io.slothmode.session;

import java.util.List;

/**
 * Iterates over a list of event lines.
 */
public class EventIterator {
    private final List<String> events;
    private int cursor = -1;

    public EventIterator(List<String> events) {
        this.events = events;
    }

    public void First() {
        cursor = 0;
    }

    public void Next() {
        cursor++;
    }

    public boolean IsDone() {
        return cursor >= events.size();
    }

    public String CurrentItem() {
        return events.get(cursor);
    }
}
