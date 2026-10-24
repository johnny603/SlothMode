package io.slothmode.config;

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
