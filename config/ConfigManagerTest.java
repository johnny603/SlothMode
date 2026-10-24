package io.slothmode.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigManagerTest {

    @AfterEach
    void tearDown() {
        ConfigManager.reset();
    }

    @Test
    void getInstance_returnsSameInstance() {
        ConfigManager a = ConfigManager.getInstance();
        ConfigManager b = ConfigManager.getInstance();
        assertThat(a).isSameAs(b);
    }

    @Test
    void loadConfig_createsDefaultWhenMissing(@TempDir Path tempHome) throws Exception {
        ConfigManager manager = ConfigManager.forHome(tempHome);

        SlothConfig config = manager.loadConfig();

        assertThat(config).isEqualTo(SlothConfig.defaults(tempHome.toString()));
        assertThat(tempHome.resolve(".slothmode/config.toml")).exists();
        assertThat(tempHome.resolve(".slothmode/logs")).isDirectory();
        assertThat(tempHome.resolve(".slothmode/calendar")).isDirectory();
    }

    @Test
    void loadConfig_readsExistingFile(@TempDir Path tempHome) throws Exception {
        Path dir = tempHome.resolve(".slothmode");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("config.toml"), """
                calendar.path = /custom/breaks.ics
                lockout.mode = gentle
                model.name = mistral:7b
                """);

        ConfigManager manager = ConfigManager.forHome(tempHome);
        SlothConfig config = manager.loadConfig();

        assertThat(config.calendarPath()).isEqualTo("/custom/breaks.ics");
        assertThat(config.lockoutMode()).isEqualTo("gentle");
        assertThat(config.modelName()).isEqualTo("mistral:7b");
    }
}
