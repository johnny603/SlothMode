package io.slothmode.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * SINGLETON: one local config, no server.
 *
 * <p>Reads from {@code ~/.slothmode/config.toml}. Creates the default file and
 * the CalDAV credentials placeholder on first run.</p>
 */
public final class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static final String SLOTHMODE_DIR = ".slothmode";
    private static final String CONFIG_FILE = "config.toml";
    private static final String CREDS_FILE = "data/caldav-credentials";

    private static ConfigManager instance;

    private final Path homeDir;
    private final Path configPath;
    private final SlothConfig config;

    private ConfigManager(Path homeDir) {
        this.homeDir = homeDir;
        this.configPath = homeDir.resolve(SLOTHMODE_DIR).resolve(CONFIG_FILE);
        this.config = load();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager(Path.of(System.getProperty("user.home")));
        }
        return instance;
    }

    public SlothConfig config() {
        return config;
    }

    public Path homeDir() {
        return homeDir;
    }

    public Path configPath() {
        return configPath;
    }

    /**
     * Directory for third-party plugin jars ({@code ~/.slothmode/plugins/}).
     * The directory is not created here; a missing directory is a no-op for
     * {@code PluginLoader}.
     */
    public Path pluginsDir() {
        return homeDir.resolve(SLOTHMODE_DIR).resolve("plugins");
    }

    // ------------------------------------------------------------ internals

    private SlothConfig load() {
        if (!Files.exists(configPath)) {
            return createDefault();
        }
        try (InputStream in = Files.newInputStream(configPath)) {
            Properties props = new Properties();
            props.load(in);

            SlothConfig defaults = SlothConfig.defaults(homeDir.toString());
            return new SlothConfig(
                    props.getProperty("calendar.path", defaults.calendarPath()),
                    props.getProperty("calendar.source", defaults.calendarSource()),
                    props.getProperty("calendar.url", defaults.calendarUrl()),
                    props.getProperty("lockout.mode", defaults.lockoutMode()),
                    props.getProperty("model.name", defaults.modelName()),
                    props.getProperty("model.type", defaults.modelType()),
                    props.getProperty("model.path", defaults.modelPath()));
        } catch (IOException e) {
            log.error("Could not read config at {}; using defaults", configPath, e);
            return SlothConfig.defaults(homeDir.toString());
        }
    }

    private SlothConfig createDefault() {
        try {
            Files.createDirectories(configPath.getParent());
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

            // Initialize CalDAV credentials placeholder
            Path credsPath = homeDir.resolve(SLOTHMODE_DIR).resolve(CREDS_FILE);
            Files.createDirectories(credsPath.getParent());
            if (!Files.exists(credsPath)) {
                Files.writeString(credsPath, "user:password\n");
                log.info("Created CalDAV credentials placeholder at {}. Please edit this file with your user:password.", credsPath);
            }

            return defaults;
        } catch (IOException e) {
            log.error("Could not create default config at {}", configPath, e);
            return SlothConfig.defaults(homeDir.toString());
        }
    }
}
