package com.github.nautic;

import com.github.nautic.api.AtlasAPIImpl;
import com.github.nautic.api.AtlasProvider;
import com.github.nautic.cache.LanguageCache;
import com.github.nautic.command.AtlasLangCommand;
import com.github.nautic.command.AtlasLangTabCompleter;
import com.github.nautic.command.alias.AliasRegistrar;
import com.github.nautic.config.PluginSettings;
import com.github.nautic.database.DatabaseManager;
import com.github.nautic.expansion.InfoExpansion;
import com.github.nautic.expansion.MessageExpansion;
import com.github.nautic.github.GitHubSyncManager;
import com.github.nautic.language.LanguageFileLoader;
import com.github.nautic.language.LanguageFileWatcher;
import com.github.nautic.language.LanguageHandler;
import com.github.nautic.language.LanguageManager;
import com.github.nautic.library.Libraries;
import com.github.nautic.update.UpdateListener;
import com.github.nautic.util.ColorUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import revxrsal.zapper.relocation.Relocation;
import revxrsal.zapper.repository.Repository;

import java.io.File;
import java.net.URLClassLoader;

public final class AtlasLang extends JavaPlugin {

    private static AtlasLang instance;

    private PluginSettings settings;
    private LanguageFileLoader languageFileLoader;
    private LanguageManager languageManager;
    private LanguageHandler languageHandler;
    private LanguageFileWatcher fileWatcher;
    private LanguageCache languageCache;
    private GitHubSyncManager gitHubSyncManager;

    @Override
    public void onLoad() {
        File libs = new File(getDataFolder(), "libraries");
        if (!libs.exists() && !libs.mkdirs()) {
            getLogger().warning("Could not create libraries folder");
        }

        getLogger().info("Installing runtime dependencies...");

        DependencyManager manager = new DependencyManager(
                libs,
                URLClassLoaderWrapper.wrap((URLClassLoader) getClassLoader())
        );

        manager.repository(Repository.mavenCentral());
        manager.repository(Repository.maven("https://repo.codemc.org/repository/maven-public/"));

        for (Libraries lib : Libraries.values()) {
            manager.dependency(lib.dependency());
            for (Relocation relocation : lib.relocations()) {
                manager.relocate(relocation);
            }
        }

        manager.load();
    }

    @Override
    public void onEnable() {
        instance = this;

        new Metrics(this, 29130);
        new UpdateListener(this, 132278);

        saveDefaultConfig();
        settings = new PluginSettings(getConfig());

        File baseLangFolder = new File(getDataFolder(), "languages");
        if (!baseLangFolder.exists() && !baseLangFolder.mkdirs()) {
            getLogger().warning("Could not create languages folder");
        }

        languageFileLoader = new LanguageFileLoader(baseLangFolder);
        languageManager = new LanguageManager(languageFileLoader);
        languageHandler = new LanguageHandler(languageFileLoader, languageManager);

        languageManager.loadLanguagesFromConfig(getConfig());
        logRegisteredLanguages();

        if (settings.isAutoReloadLanguages()) {
            fileWatcher = new LanguageFileWatcher(this, languageFileLoader, baseLangFolder);
            fileWatcher.start();
        } else {
            getLogger().info("Auto-reload disabled in config. Use /atlaslang reload to apply YAML changes.");
        }

        DatabaseManager.loadDatabase();

        languageCache = new LanguageCache(this, languageManager);

        AtlasProvider.register(new AtlasAPIImpl(this));
        AliasRegistrar.registerLanguageCommands(this);

        gitHubSyncManager = new GitHubSyncManager(this);

        getCommand("atlaslang").setExecutor(new AtlasLangCommand(this));
        getCommand("atlaslang").setTabCompleter(new AtlasLangTabCompleter(this));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MessageExpansion(this).register();
            new InfoExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        } else {
            getLogger().warning("PlaceholderAPI not found.");
        }

        getLogger().info("AtlasLang " + getDescription().getVersion() + " enabled successfully.");
    }

    @Override
    public void onDisable() {
        AtlasProvider.unregister();
        if (fileWatcher != null) fileWatcher.stop();
        if (languageCache != null) languageCache.clear();
        DatabaseManager.close();
    }

    /**
     * Reloads the on-disk config and refreshes the runtime settings.
     * Called by /atlaslang reload.
     *
     * <p>Note: changes to {@code performance.auto-reload-languages} require
     * a server restart — the watcher is started/stopped once at boot.</p>
     */
    public void reloadSettings() {
        reloadConfig();
        settings = new PluginSettings(getConfig());
    }

    public static AtlasLang getInstance() {
        return instance;
    }

    public PluginSettings getSettings() {
        return settings;
    }

    public LanguageFileLoader getLanguageFileLoader() {
        return languageFileLoader;
    }

    public FileConfiguration getMainConfig() {
        return getConfig();
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public LanguageHandler getLanguageHandler() {
        return languageHandler;
    }

    public LanguageCache getLanguageCache() {
        return languageCache;
    }

    public GitHubSyncManager getGitHubSyncManager() {
        return gitHubSyncManager;
    }

    private void logRegisteredLanguages() {
        var map = languageManager.getLanguageMap();

        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize("&r"));
        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize(
                "&#35ADFFAtlasLang &fLanguages Available &f(&a" + map.size() + "&f):"));

        for (var entry : map.entrySet()) {
            Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize(
                    "  &8- &f" + entry.getKey().toUpperCase()
                            + " &7(&b" + entry.getValue() + "&7)"));
        }

        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize("&r"));
    }
}
