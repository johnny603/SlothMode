package io.slothmode.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * Loads {@link SlothPlugin} implementations from jars in a directory.
 *
 * <p>Loading is best-effort and non-fatal: a missing directory, a corrupt jar, or a
 * plugin that throws during instantiation is logged and skipped. A broken plugin must
 * never prevent the app (or other plugins) from starting.</p>
 */
public final class PluginLoader {

    private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

    private PluginLoader() {
        // static utility
    }

    /**
     * Loads all plugins from jars directly inside {@code pluginsDir} (no recursion).
     *
     * @param pluginsDir directory to scan; may be null or missing
     * @return non-null list, possibly empty
     */
    public static List<SlothPlugin> load(Path pluginsDir) {
        List<SlothPlugin> plugins = new ArrayList<>();
        if (pluginsDir == null || !Files.isDirectory(pluginsDir)) {
            return plugins;
        }

        List<URL> urls = new ArrayList<>();
        try (Stream<Path> jars = Files.list(pluginsDir)) {
            jars.filter(p -> p.getFileName().toString().endsWith(".jar"))
                .forEach(p -> {
                    try {
                        urls.add(p.toUri().toURL());
                    } catch (Exception e) {
                        log.warn("Skipping unreadable plugin jar: {}", p, e);
                    }
                });
        } catch (Exception e) {
            log.error("Could not scan plugin directory {}", pluginsDir, e);
            return plugins;
        }

        if (urls.isEmpty()) {
            return plugins;
        }

        try (URLClassLoader cl = new URLClassLoader(
                urls.toArray(new URL[0]), PluginLoader.class.getClassLoader())) {
            ServiceLoader<SlothPlugin> sl = ServiceLoader.load(SlothPlugin.class, cl);
            for (SlothPlugin plugin : sl) {
                try {
                    if (plugin == null) {
                        continue;
                    }
                    plugins.add(plugin);
                    log.info("Loaded plugin: {}", plugin.id());
                } catch (Throwable t) {
                    log.warn("Skipping broken plugin", t);
                }
            }
        } catch (Throwable t) {
            // A corrupt jar can make the ServiceLoader iterator itself throw;
            // never let that escape.
            log.warn("Plugin loading aborted", t);
        }

        return plugins;
    }

    /** Convenience: null-safe list. */
    public static List<SlothPlugin> loadOrDefault(Path pluginsDir) {
        List<SlothPlugin> list = load(pluginsDir);
        return list == null ? List.of() : Objects.requireNonNull(list);
    }
}
