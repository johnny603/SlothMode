package io.slothmode.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlothConfigTest {

    @Test
    void defaults_containsExpectedValues() {
        SlothConfig config = SlothConfig.defaults("/home/test");
        assertThat(config.calendarPath()).endsWith("/.slothmode/calendar/slothmode.ics");
        assertThat(config.lockoutMode()).isEqualTo("fullscreen");
        assertThat(config.modelName()).isEqualTo("llama3.2:3b");
    }

    @Test
    void records_withSameValuesAreEqual() {
        SlothConfig a = new SlothConfig("/c.ics", "fullscreen", "llama3.2:3b");
        SlothConfig b = new SlothConfig("/c.ics", "fullscreen", "llama3.2:3b");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
