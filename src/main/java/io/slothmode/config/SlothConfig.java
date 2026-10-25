package io.slothmode.config;

import java.nio.file.Path;

public record SlothConfig(
        String calendarPath,
        String calendarSource,
        String calendarUrl,
        String lockoutMode,
        String modelName,
        String modelType,
        Path modelPath
) {
    public static SlothConfig defaults(String homeDir) {
        return new SlothConfig(
                homeDir + "/.slothmode/calendar/slothmode.ics",
                "local",
                null,
                "fullscreen",
                "llama3.2:3b",
                "ollama",
                null
        );
    }
}
