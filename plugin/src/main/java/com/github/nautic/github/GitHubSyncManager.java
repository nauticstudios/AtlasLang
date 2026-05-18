package com.github.nautic.github;

import com.github.nautic.AtlasLang;
import com.github.nautic.language.LanguageManager;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;

public final class GitHubSyncManager {

    private final AtlasLang plugin;
    private final LanguageManager languageManager;

    public GitHubSyncManager(AtlasLang plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    public GitHubSyncResult sync() {
        try {
            GitHubConfig cfg = GitHubConfig.load(plugin.getConfig());
            GitHubSynchronizer synchronizer =
                    new GitHubSynchronizer(cfg, plugin.getDataFolder());

            GitHubSyncResult result = synchronizer.execute();

            if (result == GitHubSyncResult.SUCCESS && cfg.reloadAfterSync) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        languageManager.reloadLanguages(plugin.getConfig())
                );
            }

            return result;

        } catch (Exception e) {
            plugin.getLogger().warning("GitHub sync failed: " + e.getMessage());
            return GitHubSyncResult.FAILED;
        }
    }

    public CompletableFuture<GitHubSyncResult> syncAsync() {
        CompletableFuture<GitHubSyncResult> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                future.complete(sync())
        );
        return future;
    }
}
