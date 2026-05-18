package com.github.nautic.language;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class LanguageManager {

    private final LanguageFileLoader fileLoader;

    private final Map<String, String> localeToFolder = new LinkedHashMap<>();
    private final Map<String, String> folderToLocale = new HashMap<>();
    private final Set<String> registeredFolders = new HashSet<>();

    private String defaultLang = "english";

    public LanguageManager(LanguageFileLoader fileLoader) {
        this.fileLoader = fileLoader;
    }

    public void loadLanguagesFromConfig(FileConfiguration config) {
        localeToFolder.clear();
        folderToLocale.clear();
        registeredFolders.clear();

        defaultLang = config.getString("default", "english").toLowerCase();

        for (String entry : config.getStringList("register.languages")) {
            String[] parts = entry.split(":");
            if (parts.length != 3) continue;

            String locale = parts[0].toLowerCase();
            String folder = parts[1].toLowerCase();
            String defaultFile = parts[2];

            localeToFolder.put(locale, folder);
            folderToLocale.put(folder, locale);
            registeredFolders.add(folder);

            fileLoader.prepareLanguage(folder, defaultFile);
            fileLoader.loadLanguageFolder(folder);
        }
    }

    public String resolveLanguageStrict(String input) {
        if (input == null || input.isEmpty()) return null;
        String key = input.toLowerCase();

        String folder = localeToFolder.get(key);
        if (folder != null) return folder;

        if (registeredFolders.contains(key)) return key;

        return null;
    }

    public boolean isRegisteredLanguage(String lang) {
        return lang != null && registeredFolders.contains(lang.toLowerCase());
    }

    public String getLocaleOf(String folder) {
        if (folder == null) return null;
        return folderToLocale.get(folder.toLowerCase());
    }

    public Set<String> getRegisteredLanguages() {
        return Collections.unmodifiableSet(registeredFolders);
    }

    public Set<String> getRegisteredLocales() {
        return Collections.unmodifiableSet(localeToFolder.keySet());
    }

    public String getDefaultLang() {
        return defaultLang;
    }

    public void reloadLanguages(FileConfiguration config) {
        fileLoader.clearCache();
        loadLanguagesFromConfig(config);
    }

    public Map<String, String> getLanguageMap() {
        return new LinkedHashMap<>(localeToFolder);
    }
}
