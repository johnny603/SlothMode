package io.slothmode.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static final String SLOTHMODE_DIR = ".slothmode";
    private static final String CONFIG_FILE = "config.toml";
    private static final String CREDS_FILE = "data/caldav-credentials";

    private static ConfigManager instance;
    private final Path homeDir;

    private ConfigManager(Path homeDir) {
        this.homeDir = homeDir;
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager(Paths.get(System.getProperty("user.home")));
        }
        return instance;
    }

    static synchronized ConfigManager forHome(Path homeDir) {
        instance = new ConfigManager(homeDir);
        return instance;
    }

    static synchronized void reset() {
        instance = null;
    }

    public SlothConfig loadConfig() {
        Path configPath = homeDir.resolve(SLOTHMODE_DIR).resolve(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            return createDefault(configPath);
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            props.load(in);
            SlothConfig defaults = SlothConfig.defaults(homeDir.toString());
            return new SlothConfig(
                    props.getProperty("calendar.path", defaults.calendarPath()),
                    props.getProperty("calendar.source", defaults.calendarSource()),
                    props.getProperty("calendar.url", defaults.calendarUrl()),
                    props.getProperty("lockout.mode", defaults.lockoutMode()),
                    props.getProperty("model.name", defaults.modelName()),
                    props.getProperty("model.type", defaults.modelType()),
                    expandPath(props.getProperty("model.path"))
            );
        } catch (IOException e) {
            log.error("Failed to read config at {}", configPath, e);
            return SlothConfig.defaults(homeDir.toString());
        }
    }

    private Path expandPath(String pathStr) {
        if (pathStr == null || pathStr.isBlank()) {
            return null;
        }
        if (pathStr.startsWith("~")) {
            return Paths.get(System.getProperty("user.home"), pathStr.substring(1));
        }
        return Paths.get(pathStr);
    }

    private SlothConfig createDefault(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            Files.createDirectories(homeDir.resolve(SLOTHMODE_DIR).resolve("logs"));
            Files.createDirectories(homeDir.resolve(SLOTHMODE_DIR).resolve("data"));
            Files.createDirectories(homeDir.resolve(SLOTHMODE_DIR).resolve("calendar"));

            SlothConfig defaults = SlothConfig.defaults(homeDir.toString());
            String content = """
                    calendar.path = %s
                    calendar.source = %s
                    calendar.url =
                    lockout.mode = %s
                    model.name = %s
                    model.type = %s
                    model.path =
                    """.formatted(
                    defaults.calendarPath(),
                    defaults.calendarSource(),
                    defaults.lockoutMode(),
                    defaults.modelName(),
                    defaults.modelType());
            Files.writeString(configPath, content);
            log.info("Created default config at {}", configPath);

            // Initialize credentials file
            Path credsPath = homeDir.resolve(SLOTHMODE_DIR).resolve(CREDS_FILE);
            Files.writeString(credsPath, "user:password\n");
            log.info("Created CalDAV credentials placeholder at {}. Please edit this file with your user:password.", credsPath);

            return defaults;
        } catch (IOException e) {
            log.error("Could not create default config at {}", configPath, e);
            return SlothConfig.defaults(homeDir.toString());
        }
    }
}
