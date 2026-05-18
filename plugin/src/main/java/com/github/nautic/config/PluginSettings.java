package com.github.nautic.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Strongly-typed view of the runtime tuning section in {@code config.yml}.
 *
 * <p>All values are loaded once and exposed via getters. Reload the plugin
 * (or call {@link AtlasLang#reloadSettings()}) to pick up changes.</p>
 *
 * <p>Defaults are tuned for large networks (1000+ players): cache enabled,
 * preload on join, auto-reload on, debug off.</p>
 */
public final class PluginSettings {

    private static final long MIN_DEBOUNCE_MS = 50L;
    private static final long MAX_DEBOUNCE_MS = 5000L;

    private final boolean autoReloadLanguages;
    private final long reloadDebounceMs;
    private final boolean preloadOnJoin;
    private final boolean debug;

    public PluginSettings(FileConfiguration cfg) {
        ConfigurationSection perf = cfg.getConfigurationSection("performance");

        if (perf != null) {
            this.autoReloadLanguages = perf.getBoolean("auto-reload-languages", true);
            this.reloadDebounceMs = clamp(
                    perf.getLong("reload-debounce-ms", 250L),
                    MIN_DEBOUNCE_MS,
                    MAX_DEBOUNCE_MS
            );
            this.preloadOnJoin = perf.getBoolean("preload-on-join", true);
        } else {
            this.autoReloadLanguages = true;
            this.reloadDebounceMs = 250L;
            this.preloadOnJoin = true;
        }

        this.debug = cfg.getBoolean("debug", false);
    }

    public boolean isAutoReloadLanguages() {
        return autoReloadLanguages;
    }

    public long getReloadDebounceMs() {
        return reloadDebounceMs;
    }

    public boolean isPreloadOnJoin() {
        return preloadOnJoin;
    }

    public boolean isDebug() {
        return debug;
    }

    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(value, max));
    }
}
