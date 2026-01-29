package com.github.nautic.expansion;

import com.github.nautic.AtlasLang;
import com.github.nautic.database.DatabaseManager;
import com.github.nautic.handler.LangHandler;
import com.github.nautic.manager.LanguageManager;
import com.github.nautic.utils.addColor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ALExpansion extends PlaceholderExpansion {

    private final AtlasLang plugin;

    public ALExpansion(AtlasLang plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "alang";
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

        LangHandler langHandler = plugin.getLangHandler();
        LanguageManager languageManager = plugin.getLanguageManager();
        String userLang = DatabaseManager.getDatabase().getLanguagePlayer(player.getUniqueId());

        if (userLang == null || !languageManager.isRegisteredLanguage(userLang)) {
            userLang = languageManager.getDefaultLang();
        }

        try {
            if (!params.contains(")_(")) {
                return addColor.Set(langHandler.getSystemMessage(userLang, "invalid_placeholder_format"));
            }

            String[] parts = params.split("\\)_\\(");
            if (parts.length != 2) {
                return addColor.Set(langHandler.getSystemMessage(userLang, "invalid_placeholder_format"));
            }

            String filePart = parts[0].replace("(", "").trim().toLowerCase();
            String keyPath = parts[1].replace(")", "").trim();

            int colonIndex = filePart.lastIndexOf(':');
            String folderPath = "";
            String fileName;

            if (colonIndex == -1) {
                fileName = filePart;
            } else {
                folderPath = filePart.substring(0, colonIndex);
                fileName = filePart.substring(colonIndex + 1);
            }

            if (!fileName.endsWith(".yml")) {
                fileName += ".yml";
            }

            String filePath = folderPath.isEmpty()
                    ? fileName
                    : folderPath + "/" + fileName;

            String keyFilePath = filePath.substring(0, filePath.length() - 4).replace("\\", "/").toLowerCase();

            String result = langHandler.get(userLang, keyFilePath, keyPath);

            if (result == null || result.isEmpty()) {
                return addColor.Set(langHandler.getSystemMessage(userLang, "not_found_path"));
            }

            return addColor.Set(result);
        } catch (Exception e) {
            return addColor.Set(langHandler.getSystemMessage(userLang, "invalid_lang_format"));
        }
    }
}
