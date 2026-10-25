package io.slothmode.calendar;

import io.slothmode.command.Memento;

import java.util.List;

/**
 * MEMENTO: a snapshot of the .ics file's breaks.
 *
 * Reuses the Command/Memento interface from M3. The payload is opaque
 * to anything but the CalendarSource that produced it.
 */
public record IcsMemento(List<Break> breaks) implements Memento {

    public IcsMemento {
        breaks = List.copyOf(breaks);
    }
}
