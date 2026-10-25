package io.slothmode.calendar;

import io.slothmode.config.SlothConfig;

import java.nio.file.Paths;

/**
 * ABSTRACT FACTORY: picks the right BreakSource for the user's config.
 *
 * For M4, the choice is based on whether calendar.path is set.
 * Real CalDAV / Google Calendar support lands in M7+.
 */
public final class BreakSourceFactory {

    private BreakSourceFactory() {}

    public static BreakSource fromConfig(SlothConfig config) {
        return new CalendarSource(Paths.get(config.calendarPath()));
    }
}
