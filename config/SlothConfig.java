package io.slothmode.config;

/**
 * Immutable configuration record (Java 21).
 *
 * A record here means equals/hashCode/toString are free,
 * which makes ConfigManagerTest a one-line assertion.
 */
public record SlothConfig(
        String calendarPath,
        String lockoutMode,
        String modelName
) {
    public static SlothConfig defaults(String homeDir) {
        return new SlothConfig(
                homeDir + "/.slothmode/calendar/slothmode.ics",
                "fullscreen",
                "llama3.2:3b"
        );
    }
}
