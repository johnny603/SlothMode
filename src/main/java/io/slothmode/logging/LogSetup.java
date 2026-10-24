package io.slothmode.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;

import java.net.URL;

public final class LogSetup {

    private LogSetup() {}

    public static void configure() {
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
                context.putProperty("LOG_DIR", home + "/.slothmode/logs");
            }
        } catch (JoranException e) {
            System.err.println("Logback configuration failed: " + e.getMessage());
        }
    }
}
