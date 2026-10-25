package io.slothmode.calendar;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single break: when it starts, when it ends, where it came from.
 *
 * A record because it's immutable and value equality matters for tests.
 */
public record Break(
        LocalDateTime start,
        LocalDateTime end,
        Source source
) {
    public enum Source { CALENDAR, MANUAL }

    public Break {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        Objects.requireNonNull(source, "source");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
    }

    public int durationMinutes() {
        return (int) java.time.Duration.between(start, end).toMinutes();
    }
}
