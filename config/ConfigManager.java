package io.slothmode.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * SINGLETON: one config, one place.
 *
 * Loads ~/.slothmode/config.toml (TOML syntax is M4 — M0 reads a simple
 * properties-compatible subset so we can prove the wiring without a
 * TOML dependency yet).
 */
public final class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);

    private static final String SLOTHMODE_DIR = ".slothmode";
    private static final String CONFIG_FILE = "config.toml";

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

    /** For tests: inject a temporary home directory. */
    static synchronized ConfigManager forHome(Path homeDir) {
        instance = new ConfigManager(homeDir);
        return instance;
    }

    /** For tests: reset the singleton. */
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
            return new SlothConfig(
                    props.getProperty("calendar.path",
                            SlothConfig.defaults(homeDir.toString()).calendarPath()),
                    props.getProperty("lockout.mode",
                            SlothConfig.defaults(homeDir.toString()).lockoutMode()),
                    props.getProperty("model.name",
                            SlothConfig.defaults(homeDir.toString()).modelName())
            );
        } catch (IOException e) {
            log.error("Failed to read config at {}, falling back to defaults", configPath, e);
            return SlothConfig.defaults(homeDir.toString());
        }
    }

    private SlothConfig createDefault(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            Files.createDirectories(homeDir.resolve(SLOTHMODE_DIR).resolve("logs"));
            Files.createDirectories(homeDir.resolve(SLOTHMODE_DIR).resolve("data"));
            Files.createDirectories(homeDir.resolve(SLOTHMODE_DIR).resolve("calendar"));

            SlothConfig defaults = SlothConfig.defaults(homeDir.toString());
            String content = """
                    # SlothMode config
                    calendar.path = %s
                    lockout.mode = %s
                    model.name = %s
                    """.formatted(
                    defaults.calendarPath(),
                    defaults.lockoutMode(),
                    defaults.modelName());
            Files.writeString(configPath, content);
            log.info("Created default config at {}", configPath);
            return defaults;
        } catch (IOException e) {
            log.error("Could not create default config at {}", configPath, e);
            return SlothConfig.defaults(homeDir.toString());
        }
    }
}
