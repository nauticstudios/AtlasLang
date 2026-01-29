package com.github.nautic.expansion;

import com.github.nautic.AtlasLang;
import com.github.nautic.database.DatabaseManager;
import com.github.nautic.manager.LanguageManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class AtlasLangExpansion extends PlaceholderExpansion {

    private final AtlasLang plugin;
    private final LanguageManager languageManager;

    public AtlasLangExpansion(AtlasLang plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "atlaslang";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Senkex";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {

        if (player == null || params.isEmpty()) return "";

        String lang = DatabaseManager.getDatabase()
                .getLanguagePlayer(player.getUniqueId());

        if (lang == null || !languageManager.isRegisteredLanguage(lang)) {
            lang = languageManager.getDefaultLang();
        }

        switch (params.toLowerCase()) {

            case "language":
                return lang;

            case "locale":
                for (Map.Entry<String, String> entry : languageManager.getLanguageMap().entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(lang)) {
                        return entry.getKey();
                    }
                }
                return "";

            default:
                return "";
        }
    }
}