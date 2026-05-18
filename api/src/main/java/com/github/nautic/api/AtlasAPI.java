package com.github.nautic.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Contract for the AtlasLang translation system.
 *
 * <p>An implementation is responsible for resolving the language of a player,
 * loading language files and returning translated, color-formatted messages.
 * Database access is cached in memory and writes are asynchronous, so callers
 * can safely invoke any read method from the main thread.</p>
 *
 * <p>The interface exposes both instance methods and static helpers. Static
 * helpers delegate to the singleton registered through {@link AtlasProvider},
 * so {@code AtlasAPI.translate(player, "key")} and
 * {@code AtlasAPI.api().message(player, "key")} are equivalent.</p>
 *
 * <p>Developed by <b>Senkex</b></p>
 */
public interface AtlasAPI {

    /**
     * Current public API version. Bumped on breaking changes.
     */
    int API_VERSION = 2;

    /**
     * Default namespace used when a caller does not specify one. Resolves
     * files like {@code languages/<lang>/atlasaddon.yml}.
     */
    String DEFAULT_NAMESPACE = "atlasaddon";

    /**
     * Translates {@code path} into the player's current language using the
     * default namespace.
     *
     * @param player the player whose language will be used
     * @param path the dotted key inside the language file
     * @return the translated, color-formatted message
     */
    @NotNull String message(@NotNull Player player, @NotNull String path);

    /**
     * Translates {@code path} into the language of the given UUID using the
     * default namespace.
     *
     * @param uuid the player UUID
     * @param path the dotted key inside the language file
     * @return the translated, color-formatted message
     */
    @NotNull String message(@NotNull UUID uuid, @NotNull String path);

    /**
     * Translates {@code path} into a specific language, ignoring any player.
     *
     * @param langInput a locale code ({@code en_US}) or folder name ({@code english})
     * @param path the dotted key inside the language file
     * @return the translated message, or the default language fallback
     */
    @NotNull String messageInLang(@NotNull String langInput, @NotNull String path);

    /**
     * Translates {@code path} for a player inside a custom namespace.
     *
     * @param player the player whose language will be used
     * @param namespace the addon namespace (a folder inside {@code languages/<lang>/})
     * @param path the dotted key inside the language file
     * @return the translated, color-formatted message
     */
    @NotNull String message(@NotNull Player player, @NotNull String namespace, @NotNull String path);

    /**
     * Translates {@code path} for a UUID inside a custom namespace.
     *
     * @param uuid the player UUID
     * @param namespace the addon namespace
     * @param path the dotted key inside the language file
     * @return the translated, color-formatted message
     */
    @NotNull String message(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path);

    /**
     * Translates {@code path} in an explicit language and namespace.
     *
     * @param langInput a locale code or folder name
     * @param namespace the addon namespace
     * @param path the dotted key inside the language file
     * @return the translated message, or the default language fallback
     */
    @NotNull String messageInLang(@NotNull String langInput, @NotNull String namespace, @NotNull String path);

    /**
     * Translates {@code path} or returns {@code fallback} if the key is
     * missing. Unlike {@link #message(UUID, String, String)} no system error
     * message is produced.
     *
     * @param uuid the player UUID
     * @param namespace the addon namespace
     * @param path the dotted key
     * @param fallback the value returned when the key does not exist
     * @return the translation, or {@code fallback}
     */
    @NotNull String messageOrDefault(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path, @NotNull String fallback);

    /**
     * Checks whether {@code path} exists in the default namespace for a
     * given language.
     *
     * @param langInput a locale code or folder name
     * @param path the dotted key
     * @return {@code true} if the key resolves to a string or list
     */
    boolean has(@NotNull String langInput, @NotNull String path);

    /**
     * Checks whether {@code path} exists in a custom namespace for a given
     * language.
     *
     * @param langInput a locale code or folder name
     * @param namespace the addon namespace
     * @param path the dotted key
     * @return {@code true} if the key resolves to a string or list
     */
    boolean has(@NotNull String langInput, @NotNull String namespace, @NotNull String path);

    /**
     * Sets a player's language. The change is fired through
     * {@code PlayerLanguageChangeEvent} and persisted asynchronously.
     *
     * @param player the player
     * @param langInput a locale code or folder name
     * @return {@code true} if the language was resolved and the change was
     *         not cancelled by a listener
     */
    boolean setPlayerLanguage(@NotNull Player player, @NotNull String langInput);

    /**
     * Sets a player's language by UUID. Behaves like
     * {@link #setPlayerLanguage(Player, String)}.
     *
     * @param uuid the player UUID
     * @param langInput a locale code or folder name
     * @return {@code true} if the change was accepted
     */
    boolean setPlayerLanguage(@NotNull UUID uuid, @NotNull String langInput);

    /**
     * Asynchronous variant of {@link #setPlayerLanguage(UUID, String)}.
     *
     * @param uuid the player UUID
     * @param langInput a locale code or folder name
     * @return a future completed with {@code true} once the change is
     *         persisted, or {@code false} if cancelled or invalid
     */
    @NotNull CompletableFuture<Boolean> setPlayerLanguageAsync(@NotNull UUID uuid, @NotNull String langInput);

    /**
     * Returns the player's current language folder. Reads from an in-memory
     * cache, no database call is made.
     *
     * @param player the player
     * @return the language folder, or the default language if absent
     */
    @NotNull String getPlayerLanguage(@NotNull Player player);

    /**
     * Returns the language folder cached for the given UUID.
     *
     * @param uuid the player UUID
     * @return the language folder, or the default language if absent
     */
    @NotNull String getPlayerLanguage(@NotNull UUID uuid);

    /**
     * Tests whether {@code langInput} matches a registered language (either
     * by locale code or folder name).
     *
     * @param langInput a locale code or folder name
     * @return {@code true} if registered
     */
    boolean isLanguageRegistered(@NotNull String langInput);

    /**
     * Alias of {@link #isLanguageRegistered(String)} for readability when
     * checking compatibility from another plugin.
     *
     * @param langInput a locale code or folder name
     * @return {@code true} if supported
     */
    boolean supports(@NotNull String langInput);

    /**
     * Returns every registered language folder.
     *
     * @return an unmodifiable set of folder names
     */
    @NotNull Set<String> getRegisteredLanguages();

    /**
     * Returns every registered locale code.
     *
     * @return an unmodifiable set of locale codes ({@code en_US}, ...)
     */
    @NotNull Set<String> getRegisteredLocales();

    /**
     * Returns the default language folder configured in {@code config.yml}.
     *
     * @return the default language folder
     */
    @NotNull String getDefaultLanguage();

    /**
     * Returns the locale code mapped to a folder, if any. O(1) lookup.
     *
     * @param folder the language folder
     * @return the matching locale code or {@code null}
     */
    @Nullable String getLocaleOf(@NotNull String folder);

    /**
     * Filters a collection of candidate locales or folder names, returning
     * only those registered on the server.
     *
     * @param candidates the locales or folders to test
     * @return the subset that AtlasLang recognises
     */
    @NotNull Set<String> compatibleLanguages(@NotNull Collection<String> candidates);

    /**
     * Returns the API version reported by this implementation.
     *
     * @return the API version
     */
    int getApiVersion();

    /**
     * Returns the registered API instance.
     *
     * @return the active {@link AtlasAPI}
     * @throws IllegalStateException if AtlasLang has not enabled yet
     */
    @NotNull
    static AtlasAPI api() {
        return AtlasProvider.get();
    }

    /**
     * Tests whether the API has been registered. Useful inside soft-depend
     * plugins to gate access.
     *
     * @return {@code true} if an API instance is available
     */
    static boolean isAvailable() {
        return AtlasProvider.isRegistered();
    }

    /**
     * Throws if the running API version is lower than {@code minVersion}.
     * Call this at the top of your {@code onEnable} to fail fast against
     * older AtlasLang builds.
     *
     * @param minVersion the minimum API version your plugin requires
     * @throws IllegalStateException if the running version is too old
     */
    static void requireVersion(int minVersion) {
        if (API_VERSION < minVersion) {
            throw new IllegalStateException(
                    "AtlasAPI version " + API_VERSION + " is lower than required " + minVersion
            );
        }
    }

    /**
     * Shortcut for {@code AtlasAPI.api().message(player, path)}.
     *
     * @param player the player
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translate(@NotNull Player player, @NotNull String path) {
        return api().message(player, path);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().message(uuid, path)}.
     *
     * @param uuid the player UUID
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translate(@NotNull UUID uuid, @NotNull String path) {
        return api().message(uuid, path);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().message(player, namespace, path)}.
     *
     * @param player the player
     * @param namespace the addon namespace
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translate(@NotNull Player player, @NotNull String namespace, @NotNull String path) {
        return api().message(player, namespace, path);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().message(uuid, namespace, path)}.
     *
     * @param uuid the player UUID
     * @param namespace the addon namespace
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translate(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path) {
        return api().message(uuid, namespace, path);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().messageInLang(langInput, path)}.
     *
     * @param langInput a locale code or folder name
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translateInLang(@NotNull String langInput, @NotNull String path) {
        return api().messageInLang(langInput, path);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().messageInLang(langInput, namespace, path)}.
     *
     * @param langInput a locale code or folder name
     * @param namespace the addon namespace
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translateInLang(@NotNull String langInput, @NotNull String namespace, @NotNull String path) {
        return api().messageInLang(langInput, namespace, path);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().messageOrDefault(uuid, namespace, path, fallback)}.
     *
     * @param uuid the player UUID
     * @param namespace the addon namespace
     * @param path the dotted key
     * @param fallback the value returned when the key is missing
     * @return the translation, or {@code fallback}
     */
    @NotNull
    static String translateOrDefault(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path, @NotNull String fallback) {
        return api().messageOrDefault(uuid, namespace, path, fallback);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().has(langInput, path)}.
     *
     * @param langInput a locale code or folder name
     * @param path the dotted key
     * @return {@code true} if the key exists
     */
    static boolean exists(@NotNull String langInput, @NotNull String path) {
        return api().has(langInput, path);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().has(langInput, namespace, path)}.
     *
     * @param langInput a locale code or folder name
     * @param namespace the addon namespace
     * @param path the dotted key
     * @return {@code true} if the key exists
     */
    static boolean exists(@NotNull String langInput, @NotNull String namespace, @NotNull String path) {
        return api().has(langInput, namespace, path);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().setPlayerLanguage(player, langInput)}.
     *
     * @param player the player
     * @param langInput a locale code or folder name
     * @return {@code true} if the change was accepted
     */
    static boolean setLanguage(@NotNull Player player, @NotNull String langInput) {
        return api().setPlayerLanguage(player, langInput);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().setPlayerLanguage(uuid, langInput)}.
     *
     * @param uuid the player UUID
     * @param langInput a locale code or folder name
     * @return {@code true} if the change was accepted
     */
    static boolean setLanguage(@NotNull UUID uuid, @NotNull String langInput) {
        return api().setPlayerLanguage(uuid, langInput);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().setPlayerLanguageAsync(uuid, langInput)}.
     *
     * @param uuid the player UUID
     * @param langInput a locale code or folder name
     * @return a future completed with {@code true} once persisted
     */
    @NotNull
    static CompletableFuture<Boolean> setLanguageAsync(@NotNull UUID uuid, @NotNull String langInput) {
        return api().setPlayerLanguageAsync(uuid, langInput);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().getPlayerLanguage(player)}.
     *
     * @param player the player
     * @return the cached language folder
     */
    @NotNull
    static String getLanguage(@NotNull Player player) {
        return api().getPlayerLanguage(player);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().getPlayerLanguage(uuid)}.
     *
     * @param uuid the player UUID
     * @return the cached language folder
     */
    @NotNull
    static String getLanguage(@NotNull UUID uuid) {
        return api().getPlayerLanguage(uuid);
    }

    /**
     * Shortcut for {@code AtlasAPI.api().getRegisteredLanguages()}.
     *
     * @return every registered language folder
     */
    @NotNull
    static Set<String> getRegistered() {
        return api().getRegisteredLanguages();
    }

    /**
     * Shortcut for {@code AtlasAPI.api().getRegisteredLocales()}.
     *
     * @return every registered locale code
     */
    @NotNull
    static Set<String> getLocales() {
        return api().getRegisteredLocales();
    }

    /**
     * Shortcut for {@code AtlasAPI.api().getDefaultLanguage()}.
     *
     * @return the default language folder
     */
    @NotNull
    static String getDefault() {
        return api().getDefaultLanguage();
    }
}
