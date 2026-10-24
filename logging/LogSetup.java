package io.slothmode.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Programmatic Logback setup.
 *
 * Called from SlothModeApp.main() BEFORE launch() so the first log line
 * goes to the file, not stderr.
 */
public final class LogSetup {

    private LogSetup() {}

    public static void configure() {
        // Ensure the log directory exists before Logback tries to open the file.
        String home = System.getProperty("user.home");
        new java.io.File(home, ".slothmode/logs").mkdirs();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        URL config = LogSetup.class.getResource("/logback.xml");
        try {
            if (config != null) {
                configurator.doConfigure(config);
            } else {
                // Fallback: Logback's own default config.
                context.putProperty("LOG_DIR", home + "/.slothmode/logs");
            }
        } catch (JoranException e) {
            System.err.println("Logback configuration failed: " + e.getMessage());
        }
    }
}
