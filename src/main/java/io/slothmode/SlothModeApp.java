package io.slothmode;

import io.slothmode.config.ConfigManager;
import io.slothmode.config.SlothConfig;
import io.slothmode.logging.LogSetup;
import io.slothmode.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FACADE: the single entry point into SlothMode.
 *
 * All wiring happens here — subsystem internals stay swappable.
 */
public class SlothModeApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(SlothModeApp.class);

    public static void main(String[] args) {
        // Configure logging BEFORE the JavaFX toolkit starts.
        LogSetup.configure();
        log.info("SlothMode starting");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        SlothConfig config = ConfigManager.getInstance().loadConfig();
        log.info("Loaded config: calendar={}, lockoutMode={}, model={}",
                config.calendarPath(), config.lockoutMode(), config.modelName());

        new MainWindow(primaryStage, config).show();
        log.info("SlothMode idle");
    }
}
