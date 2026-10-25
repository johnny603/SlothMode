package io.slothmode;

import io.slothmode.config.ConfigManager;
import io.slothmode.config.SlothConfig;
import io.slothmode.logging.LogSetup;
import io.slothmode.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionListener;

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

        setupTray();

        new MainWindow(primaryStage, config).show();
        log.info("SlothMode idle");
    }

    private void setupTray() {
        if (!SystemTray.isSupported()) {
            log.warn("System tray is not supported on this platform. Skipping tray icon.");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        // Placeholder icon: solid green circle
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = image.createGraphics();
        g.setColor(Color.GREEN);
        g.fillOval(0, 0, 16, 16);
        g.dispose();

        TrayIcon trayIcon = new TrayIcon(image, "SlothMode");
        trayIcon.setImageAutoSize(true);

        PopupMenu menu = new PopupMenu();
        MenuItem showItem = new MenuItem("Show");
        showItem.addActionListener(e -> {
            // In a real app, we would track the MainWindow instance.
            // For v0, we log that the request was made.
            log.info("Tray: Show requested");
        });

        MenuItem nextBreakItem = new MenuItem("Next break: Checking...");
        nextBreakItem.setEnabled(false);

        MenuItem quitItem = new MenuItem("Quit");
        quitItem.addActionListener(e -> System.exit(0));

        menu.add(showItem);
        menu.add(nextBreakItem);
        menu.addSeparator();
        menu.add(quitItem);

        trayIcon.setPopupMenu(menu);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            log.error("Could not add tray icon", e);
        }
    }
}
