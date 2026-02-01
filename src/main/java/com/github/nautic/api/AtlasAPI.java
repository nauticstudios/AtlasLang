package com.github.nautic.api;

import com.github.nautic.AtlasLang;
import com.github.nautic.database.DatabaseManager;
import com.github.nautic.handler.LangHandler;
import com.github.nautic.manager.LanguageManager;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * AtlasAPI is the main public API for interacting with the AtlasLang language system.
 *
 * @author Senkex
 * @powered Nautic Studios
 *
 * This class provides static utility methods to:
 * - Retrieve translated messages
 * - Manage player languages
 * - Validate registered languages
 * - Access internal language managers
 *
 * The API is designed to be simple, safe, and developer-friendly.
 */
public final class AtlasAPI {

    /**
     * Singleton instance of the AtlasAPI.
     */
    private static AtlasAPI instance;

    /**
     * Handles language file access and message retrieval.
     */
    private final LangHandler langHandler;

    /**
     * Manages registered languages and language resolution.
     */
    private final LanguageManager languageManager;

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @param langHandler     The language handler instance
     * @param languageManager The language manager instance
     */
    private AtlasAPI(LangHandler langHandler, LanguageManager languageManager) {
        this.langHandler = langHandler;
        this.languageManager = languageManager;
    }

    /**
     * Initializes the AtlasAPI.
     * This method should be called once during plugin startup.
     *
     * @param plugin The main AtlasLang plugin instance
     */
    public static void initialize(AtlasLang plugin) {
        if (instance != null) return;
        instance = new AtlasAPI(
                plugin.getLangHandler(),
                plugin.getLanguageManager()
        );
    }

    /**
     * Returns the AtlasAPI singleton instance.
     *
     * @return AtlasAPI instance
     * @throws IllegalStateException if the API has not been initialized
     */
    public static AtlasAPI get() {
        if (instance == null) {
            throw new IllegalStateException("[AtlasAPI] Could not be started");
        }
        return instance;
    }

    /**
     * Retrieves a translated message for a player using their UUID.
     *
     * @param player The player
     * @param path   The language path
     * @return The translated message
     */
    public static String get(Player player, String path) {
        return get(player.getUniqueId(), path);
    }

    /**
     * Retrieves a translated message using a player's UUID.
     *
     * @param uuid The player's UUID
     * @param path The language path
     * @return The translated message
     */
    public static String get(UUID uuid, String path) {
        String lang = DatabaseManager.getDatabase().getLanguagePlayer(uuid);
        if (lang == null) {
            lang = getDefaultLanguage();
        }
        return get(lang, path);
    }

    /**
     * Retrieves a translated message using a language identifier.
     *
     * @param langInput The language input (alias, locale, or key)
     * @param path      The language path
     * @return The translated message
     */
    public static String get(String langInput, String path) {
        String resolved = get().languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) {
            resolved = getDefaultLanguage();
        }
        return get().langHandler.get(resolved, "atlasaddon", path);
    }

    /**
     * Retrieves a translated message or returns a fallback value if not found.
     *
     * @param player  The player
     * @param path    The language path
     * @param fallback The fallback value
     * @return The translated message or fallback
     */
    public static String getOrDefault(Player player, String path, String fallback) {
        String value = get(player, path);
        return value != null ? value : fallback;
    }

    /**
     * Checks whether a specific path exists for a given language.
     *
     * @param langInput The language input
     * @param path      The language path
     * @return true if the path exists, false otherwise
     */
    public static boolean has(String langInput, String path) {
        String resolved = get().languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) return false;
        return get().langHandler.get(resolved, resolved, path) != null;
    }

    /**
     * Sets the language for a player.
     *
     * @param player   The player
     * @param langInput The language input
     * @return true if the language was set successfully
     */
    public static boolean setLanguage(Player player, String langInput) {
        return setLanguage(player.getUniqueId(), langInput);
    }

    /**
     * Sets the language for a player using UUID.
     *
     * @param uuid      The player's UUID
     * @param langInput The language input
     * @return true if the language was set successfully
     */
    public static boolean setLanguage(UUID uuid, String langInput) {
        String resolved = get().languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) return false;
        DatabaseManager.getDatabase().setLanguagePlayer(uuid, resolved);
        return true;
    }

    /**
     * Returns the current language of a player.
     *
     * @param player The player
     * @return The player's language
     */
    public static String getLanguage(Player player) {
        return getLanguage(player.getUniqueId());
    }

    /**
     * Returns the current language of a player using UUID.
     *
     * @param uuid The player's UUID
     * @return The player's language or the default language
     */
    public static String getLanguage(UUID uuid) {
        String lang = DatabaseManager.getDatabase().getLanguagePlayer(uuid);
        return lang != null ? lang : getDefaultLanguage();
    }

    /**
     * Checks if a language is registered in the system.
     *
     * @param langInput The language input
     * @return true if the language exists
     */
    public static boolean isLanguageRegistered(String langInput) {
        String resolved = get().languageManager.resolveLanguageStrict(langInput);
        return resolved != null;
    }

    /**
     * Returns all registered languages.
     *
     * @return A set of registered language identifiers
     */
    public static Set<String> getRegisteredLanguages() {
        return get().languageManager.getRegisteredLanguages();
    }

    /**
     * Returns the default language.
     *
     * @return Default language identifier
     */
    public static String getDefaultLanguage() {
        return get().languageManager.getDefaultLang();
    }

    /**
     * Provides access to the LanguageManager.
     *
     * @return LanguageManager instance
     */
    public static LanguageManager getLanguageManager() {
        return get().languageManager;
    }

    /**
     * Provides access to the LangHandler.
     *
     * @return LangHandler instance
     */
    public static LangHandler getLangHandler() {
        return get().langHandler;
    }

    /**
     * Retrieves an addon-specific message for a player.
     *
     * @param player The player
     * @param path   The addon language path
     * @return The translated addon message
     */
    public static String getAddon(Player player, String path) {
        return getAddon(player.getUniqueId(), path);
    }

    /**
     * Retrieves an addon-specific message using UUID.
     *
     * @param uuid The player's UUID
     * @param path The addon language path
     * @return The translated addon message
     */
    public static String getAddon(UUID uuid, String path) {
        String lang = DatabaseManager.getDatabase().getLanguagePlayer(uuid);
        if (lang == null) lang = getDefaultLanguage();
        return getAddon(lang, path);
    }

    /**
     * Retrieves an addon-specific message using a language identifier.
     *
     * @param langInput The language input
     * @param path      The addon language path
     * @return The translated addon message
     */
    public static String getAddon(String langInput, String path) {
        String resolved = get().languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) resolved = getDefaultLanguage();
        return get().langHandler.get(resolved, "atlasaddon", path);
    }

}