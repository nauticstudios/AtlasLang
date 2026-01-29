package com.github.nautic.manager;

import com.github.nautic.AtlasLang;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileManager {

    private final Map<String, YamlConfiguration> loadedFiles = new HashMap<>();
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final File baseLanguageDir;

    public FileManager(File baseLanguageDir) {
        this.baseLanguageDir = baseLanguageDir;
    }

    public void clearCache() {
        loadedFiles.clear();
        configs.clear();
    }

    public void loadFile(String id, File file) {
        if (!file.exists()) return;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String key = id.toLowerCase();

        loadedFiles.put(key, cfg);
        configs.put(key, cfg);
    }

    public String get(String id, String path) {
        YamlConfiguration cfg = loadedFiles.get(id.toLowerCase());
        return cfg != null ? cfg.getString(path) : null;
    }

    public FileConfiguration getConfig(String fileId) {
        return configs.get(fileId.toLowerCase());
    }

    public boolean isLoaded(String id) {
        return loadedFiles.containsKey(id.toLowerCase());
    }

    public void loadByLangAndPath(String lang, String relativePath) {
        String cleanPath = relativePath.replace(".yml", "").toLowerCase();
        String id = lang.toLowerCase() + ":" + cleanPath;
        File file = new File(baseLanguageDir, lang + "/" + relativePath);
        loadFile(id, file);
    }

    public void loadLanguageFolder(String lang) {
        File folder = new File(baseLanguageDir, lang);
        if (!folder.exists() || !folder.isDirectory()) return;
        loadFilesRecursively(lang.toLowerCase(), folder);
    }

    public void prepareLanguage(String lang, String defaultFile) {
        File langFolder = new File(baseLanguageDir, lang);
        if (!langFolder.exists()) langFolder.mkdirs();

        File mainFile = new File(langFolder, defaultFile);
        if (mainFile.exists()) return;

        String resourcePath = "languages/" + lang + "/" + defaultFile;

        if (AtlasLang.getInstance().getResource(resourcePath) != null) {
            AtlasLang.getInstance().saveResource(resourcePath, false);
            return;
        }

        try {
            mainFile.createNewFile();
            YamlConfiguration cfg = new YamlConfiguration();
            cfg.save(mainFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFilesRecursively(String lang, File folder) {
        File[] files = folder.listFiles();
        if (files == null) return;

        File base = new File(baseLanguageDir, lang);

        for (File file : files) {
            if (file.isDirectory()) {
                loadFilesRecursively(lang, file);
                continue;
            }

            if (!file.getName().endsWith(".yml")) continue;

            String relative = file.getAbsolutePath()
                    .substring(base.getAbsolutePath().length() + 1)
                    .replace("\\", "/")
                    .replace(".yml", "");

            loadFile(lang + ":" + relative.toLowerCase(), file);
        }
    }

    public Set<String> getLoadedFileIds() {
        return new HashSet<>(loadedFiles.keySet());
    }
}