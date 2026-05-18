package com.github.nautic.language;

import com.github.nautic.util.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public final class LanguageHandler {

    private final LanguageFileLoader fileLoader;
    private final LanguageManager languageManager;

    public LanguageHandler(LanguageFileLoader fileLoader, LanguageManager languageManager) {
        this.fileLoader = fileLoader;
        this.languageManager = languageManager;
    }

    public String get(String langInput, String filePath, String path) {
        return get(null, langInput, filePath, path);
    }

    public String get(Player player, String langInput, String filePath, String path) {
        String langFolder = languageManager.resolveLanguageStrict(langInput);
        if (langFolder == null) {
            langFolder = languageManager.getDefaultLang();
        }

        String fileId = langFolder + ":" + filePath.toLowerCase();

        if (!fileLoader.isLoaded(fileId)) {
            return ColorUtil.colorizeWithPlaceholders(player,
                    getSystemMessageOrDefault(
                            langFolder,
                            "file_not_found",
                            "&cFile not found &7[" + filePath + "]"
                    ).replace("{file}", filePath)
            );
        }

        FileConfiguration cfg = fileLoader.getConfig(fileId);
        if (cfg == null) {
            return ColorUtil.colorizeWithPlaceholders(player,
                    getSystemMessage(langFolder, "invalid_lang_format")
            );
        }

        String result = null;

        if (cfg.isString(path)) {
            result = cfg.getString(path);
        } else if (cfg.isList(path)) {
            List<String> list = cfg.getStringList(path);
            if (!list.isEmpty()) {
                result = String.join("\n", list);
            }
        }

        if (result == null) {
            result = getSystemMessageOrDefault(
                    langFolder,
                    "not_translated",
                    "&fNot translated &7» &a" + path
            ).replace("{path}", path);
        }

        return ColorUtil.colorizeWithPlaceholders(player, result.trim());
    }

    public String getRaw(String langInput, String filePath, String path) {
        String langFolder = languageManager.resolveLanguageStrict(langInput);
        if (langFolder == null) return null;

        String fileId = langFolder + ":" + filePath.toLowerCase();
        FileConfiguration cfg = fileLoader.getConfig(fileId);
        if (cfg == null) return null;

        if (cfg.isString(path)) return cfg.getString(path);
        if (cfg.isList(path)) {
            List<String> list = cfg.getStringList(path);
            if (!list.isEmpty()) return String.join("\n", list);
        }
        return null;
    }

    public boolean exists(String langInput, String filePath, String path) {
        String langFolder = languageManager.resolveLanguageStrict(langInput);
        if (langFolder == null) return false;

        String fileId = langFolder + ":" + filePath.toLowerCase();
        FileConfiguration cfg = fileLoader.getConfig(fileId);
        if (cfg == null) return false;

        return cfg.isString(path) || cfg.isList(path);
    }

    public String getSystemMessage(String langFolder, String key) {
        String systemId = langFolder.toLowerCase() + ":" + langFolder.toLowerCase();

        if (!fileLoader.isLoaded(systemId)) {
            return ColorUtil.colorize("&cSystem file missing");
        }

        String msg = fileLoader.get(systemId, key);
        if (msg == null) {
            return ColorUtil.colorize("&cSystem message missing");
        }

        return ColorUtil.colorize(msg.trim());
    }

    private String getSystemMessageOrDefault(String langFolder, String key, String defaultMsg) {
        String systemId = langFolder.toLowerCase() + ":" + langFolder.toLowerCase();

        if (!fileLoader.isLoaded(systemId)) {
            return ColorUtil.colorize(defaultMsg);
        }

        String msg = fileLoader.get(systemId, key);
        if (msg == null) {
            return ColorUtil.colorize(defaultMsg);
        }

        return ColorUtil.colorize(msg.trim());
    }
}
