package com.github.nautic.api;

import org.jetbrains.annotations.NotNull;

/**
 * Holds the {@link AtlasAPI} singleton. Registered by the plugin at startup,
 * unregistered on shutdown. Consumers should call {@link AtlasAPI#api()} —
 * this class is intentionally minimal.
 *
 */
public final class AtlasProvider {

    private static volatile AtlasAPI instance;

    private AtlasProvider() {}

    @NotNull
    public static AtlasAPI get() {
        AtlasAPI api = instance;
        if (api == null) {
            throw new IllegalStateException(
                    "AtlasAPI is not registered. Make sure AtlasLang is enabled " +
                            "and your plugin declares it in 'depend' or 'softdepend'."
            );
        }
        return api;
    }

    public static boolean isRegistered() {
        return instance != null;
    }

    /**
     * Internal — called by AtlasLang plugin only.
     */
    public static void register(@NotNull AtlasAPI api) {
        instance = api;
    }

    /**
     * Internal — called by AtlasLang plugin only.
     */
    public static void unregister() {
        instance = null;
    }
}
