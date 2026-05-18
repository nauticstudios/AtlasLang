package com.github.nautic.expansion;

import com.github.nautic.AtlasLang;
import com.github.nautic.cache.LanguageCache;
import com.github.nautic.language.LanguageHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MessageExpansion extends PlaceholderExpansion {

    private final AtlasLang plugin;
    private final LanguageHandler languageHandler;
    private final LanguageCache cache;

    public MessageExpansion(AtlasLang plugin) {
        this.plugin = plugin;
        this.languageHandler = plugin.getLanguageHandler();
        this.cache = plugin.getLanguageCache();
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

        String userLang = cache.get(player.getUniqueId());

        try {
            if (!params.contains(")_(")) {
                return languageHandler.getSystemMessage(userLang, "invalid_placeholder_format");
            }

            String[] parts = params.split("\\)_\\(");
            if (parts.length != 2) {
                return languageHandler.getSystemMessage(userLang, "invalid_placeholder_format");
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

            String keyFilePath = filePath.substring(0, filePath.length() - 4)
                    .replace("\\", "/")
                    .toLowerCase();

            String result = languageHandler.get(player, userLang, keyFilePath, keyPath);

            if (result == null || result.isEmpty()) {
                return languageHandler.getSystemMessage(userLang, "not_found_path");
            }

            return result;
        } catch (Exception e) {
            return languageHandler.getSystemMessage(userLang, "invalid_lang_format");
        }
    }
}
