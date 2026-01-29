package com.github.nautic.api;

import com.github.nautic.AtlasLang;
import com.github.nautic.database.DatabaseManager;
import com.github.nautic.handler.LangHandler;
import com.github.nautic.manager.LanguageManager;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public final class AtlasAPI {

    private static AtlasAPI instance;

    private final LangHandler langHandler;
    private final LanguageManager languageManager;

    private AtlasAPI(LangHandler langHandler, LanguageManager languageManager) {
        this.langHandler = langHandler;
        this.languageManager = languageManager;
    }

    public static void initialize(AtlasLang plugin) {
        if (instance != null) return;
        instance = new AtlasAPI(
                plugin.getLangHandler(),
                plugin.getLanguageManager()
        );
    }

    public static AtlasAPI get() {
        if (instance == null) {
            throw new IllegalStateException("[AtlasAPI] Could not be started");
        }
        return instance;
    }

    public static String get(Player player, String path) {
        return get(player.getUniqueId(), path);
    }

    public static String get(UUID uuid, String path) {
        String lang = DatabaseManager.getDatabase().getLanguagePlayer(uuid);
        if (lang == null) {
            lang = getDefaultLanguage();
        }
        return get(lang, path);
    }

    public static String get(String langInput, String path) {
        String resolved = get().languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) {
            resolved = getDefaultLanguage();
        }
        return get().langHandler.get(resolved, "atlasaddon", path);
    }

    public static String getOrDefault(Player player, String path, String fallback) {
        String value = get(player, path);
        return value != null ? value : fallback;
    }

    public static boolean has(String langInput, String path) {
        String resolved = get().languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) return false;
        return get().langHandler.get(resolved, resolved, path) != null;
    }

    public static boolean setLanguage(Player player, String langInput) {
        return setLanguage(player.getUniqueId(), langInput);
    }

    public static boolean setLanguage(UUID uuid, String langInput) {
        String resolved = get().languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) return false;
        DatabaseManager.getDatabase().setLanguagePlayer(uuid, resolved);
        return true;
    }

    public static String getLanguage(Player player) {
        return getLanguage(player.getUniqueId());
    }

    public static String getLanguage(UUID   uuid) {
        String lang = DatabaseManager.getDatabase().getLanguagePlayer(uuid);
        return lang != null ? lang : getDefaultLanguage();
    }

    public static boolean isLanguageRegistered(String langInput) {
        String resolved = get().languageManager.resolveLanguageStrict(langInput);
        return resolved != null;
    }

    public static Set<String> getRegisteredLanguages() {
        return get().languageManager.getRegisteredLanguages();
    }

    public static String getDefaultLanguage() {
        return get().languageManager.getDefaultLang();
    }

    public static LanguageManager getLanguageManager() {
        return get().languageManager;
    }

    public static LangHandler getLangHandler() {
        return get().langHandler;
    }

    public static String getAddon(Player player, String path) {
        return getAddon(player.getUniqueId(), path);
    }

    public static String getAddon(UUID uuid, String path) {
        String lang = DatabaseManager.getDatabase().getLanguagePlayer(uuid);
        if (lang == null) lang = getDefaultLanguage();
        return getAddon(lang, path);
    }

    public static String getAddon(String langInput, String path) {
        String resolved = get().languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) resolved = getDefaultLanguage();
        return get().langHandler.get(resolved, "atlasaddon", path);
    }

}
