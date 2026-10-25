package io.slothmode.calendar;

import io.slothmode.config.SlothConfig;
import io.slothmode.plugin.PluginLoader;
import io.slothmode.plugin.SlothPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * ABSTRACT FACTORY: picks the right BreakSource for the user's config.
 *
 * <p>Built-in sources ({@code local}, {@code caldav}, {@code google}) are checked first.
 * If none match, {@link PluginLoader} is consulted for a plugin whose {@link SlothPlugin#id()}
 * equals {@code calendar.source} and which returns a non-null {@link BreakSource}.</p>
 *
 * <p>The public {@link #fromConfig(SlothConfig)} delegates to
 * {@link #fromConfig(SlothConfig, Path)} with the default plugins directory,
 * which keeps the factory testable without touching {@code ~/.slothmode/plugins/}.</p>
 */
public final class BreakSourceFactory {

    private static final Logger log = LoggerFactory.getLogger(BreakSourceFactory.class);

    private BreakSourceFactory() {
    }

    public static BreakSource fromConfig(SlothConfig config) {
        return fromConfig(config, defaultPluginsDir());
    }

    static BreakSource fromConfig(SlothConfig config, Path pluginsDir) {
        String source = config.calendarSource();

        if (source == null || source.isEmpty() || "local".equals(source)) {
            return new CalendarSource(Paths.get(config.calendarPath()));
        }
        if ("caldav".equals(source)) {
            Path credsPath = defaultDataDir().resolve("caldav-credentials");
            return new CalDAVSource(config.calendarUrl(), credsPath);
        }

        // Plugin fallback: any id that wasn't a built-in.
        List<SlothPlugin> plugins = PluginLoader.load(pluginsDir);
        for (SlothPlugin plugin : plugins) {
            if (source.equals(plugin.id())) {
                BreakSource bs = plugin.createBreakSource(config);
                if (bs != null) {
                    log.info("Using plugin BreakSource for id '{}': {}", source, bs.name());
                    return bs;
                }
                log.warn("Plugin '{}' matched calendar.source but returned null BreakSource", source);
            }
        }

        throw new IllegalArgumentException("unknown calendar.source: " + source);
    }

    private static Path defaultPluginsDir() {
        return Paths.get(System.getProperty("user.home"), ".slothmode", "plugins");
    }

    private static Path defaultDataDir() {
        return Paths.get(System.getProperty("user.home"), ".slothmode", "data");
    }
}
