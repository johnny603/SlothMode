package io.slothmode;

import io.slothmode.config.ConfigManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Non-UI smoke test: the wiring before JavaFX launches.
 * The JavaFX Stage itself is tested manually in M1.
 */
class SlothModeAppSmokeTest {

    @Test
    void configManagerIsReachableWithoutJavaFx() {
        assertThat(ConfigManager.getInstance()).isNotNull();
    }
}
