package com.github.nautic.language;

import com.github.nautic.AtlasLang;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LanguageFileLoader {

    private final ConcurrentHashMap<String, YamlConfiguration> files = new ConcurrentHashMap<>();
    private final File baseLanguageDir;

    public LanguageFileLoader(File baseLanguageDir) {
        this.baseLanguageDir = baseLanguageDir;
    }

    public void clearCache() {
        files.clear();
    }

    public void loadFile(String id, File file) {
        if (!file.exists()) return;
        files.put(id.toLowerCase(), YamlConfiguration.loadConfiguration(file));
    }

    public String get(String id, String path) {
        YamlConfiguration cfg = files.get(id.toLowerCase());
        return cfg != null ? cfg.getString(path) : null;
    }

    public FileConfiguration getConfig(String fileId) {
        return files.get(fileId.toLowerCase());
    }

    public boolean isLoaded(String id) {
        return files.containsKey(id.toLowerCase());
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
        if (!langFolder.exists() && !langFolder.mkdirs()) return;

        File mainFile = new File(langFolder, defaultFile);
        if (mainFile.exists()) return;

        String resourcePath = "languages/" + lang + "/" + defaultFile;

        if (AtlasLang.getInstance().getResource(resourcePath) != null) {
            AtlasLang.getInstance().saveResource(resourcePath, false);
            return;
        }

        try {
            if (mainFile.createNewFile()) {
                new YamlConfiguration().save(mainFile);
            }
        } catch (IOException e) {
            AtlasLang.getInstance().getLogger().warning(
                    "Could not create default language file: " + mainFile.getName()
            );
        }
    }

    private void loadFilesRecursively(String lang, File folder) {
        File[] children = folder.listFiles();
        if (children == null) return;

        File base = new File(baseLanguageDir, lang);

        for (File file : children) {
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
        return Collections.unmodifiableSet(files.keySet());
    }
}
