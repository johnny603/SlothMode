package io.slothmode.plugin;

import io.slothmode.calendar.BreakSource;
import io.slothmode.config.SlothConfig;
import io.slothmode.reward.BreakStrategy;

/**
 * Extension point for third-party plugins.
 *
 * <p>A plugin is discovered via {@link java.util.ServiceLoader} from jars dropped into
 * {@code ~/.slothmode/plugins/}. Each plugin declares a stable {@link #id()} that can be
 * referenced from {@code calendar.source} (for break sources) or from the strategy
 * selection path (for break strategies).</p>
 *
 * <p>Both factory methods default to returning {@code null}: a plugin may provide one,
 * the other, or both. A plugin that returns {@code null} for a given product is treated
 * as "not applicable" by the callers.</p>
 */
public interface SlothPlugin {

    /** Stable identifier for this plugin (used in config, e.g. {@code calendar.source}). */
    String id();

    /** Optional: produce a {@link BreakSource}. Return {@code null} if not provided. */
    default BreakSource createBreakSource(SlothConfig cfg) {
        return null;
    }

    /** Optional: produce a {@link BreakStrategy}. Return {@code null} if not provided. */
    default BreakStrategy createBreakStrategy(SlothConfig cfg) {
        return null;
    }
}
