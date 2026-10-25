package io.slothmode.calendar;

import io.slothmode.config.SlothConfig;

import java.nio.file.Paths;

/**
 * ABSTRACT FACTORY: picks the right BreakSource for the user's config.
 */
public final class BreakSourceFactory {

    private BreakSourceFactory() {}

    public static BreakSource fromConfig(SlothConfig config) {
        String source = config.calendarSource();
        String home = System.getProperty("user.home");
        java.nio.file.Path credsPath = Paths.get(home, ".slothmode", "data", "caldav-credentials");

        if (source == null || source.isEmpty() || "local".equals(source)) {
            return new CalendarSource(Paths.get(config.calendarPath()));
        }
        if ("caldav".equals(source)) {
            return new CalDAVSource(config.calendarUrl(), credsPath);
        }
        throw new IllegalArgumentException("unknown calendar.source: " + source);
    }
}
