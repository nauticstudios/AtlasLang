package com.github.nautic.cache;

import com.github.nautic.AtlasLang;
import com.github.nautic.config.PluginSettings;
import com.github.nautic.database.DatabaseManager;
import com.github.nautic.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class LanguageCache implements Listener {

    private final AtlasLang plugin;
    private final LanguageManager languageManager;
    private final ConcurrentHashMap<UUID, String> cache = new ConcurrentHashMap<>();

    public LanguageCache(AtlasLang plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!plugin.getSettings().isPreloadOnJoin()) return;
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        loadFromDatabase(event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getSettings().isPreloadOnJoin()) return;
        UUID uuid = event.getPlayer().getUniqueId();
        if (!cache.containsKey(uuid)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> loadFromDatabase(uuid));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        cache.remove(event.getPlayer().getUniqueId());
    }

    private void loadFromDatabase(UUID uuid) {
        try {
            String lang = DatabaseManager.getDatabase().getLanguagePlayer(uuid);
            if (lang != null && languageManager.isRegisteredLanguage(lang)) {
                cache.put(uuid, lang);
                if (plugin.getSettings().isDebug()) {
                    plugin.getLogger().info("[debug] Cached language for " + uuid + " -> " + lang);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to preload language for " + uuid + ": " + e.getMessage());
        }
    }

    public String get(UUID uuid) {
        String cached = cache.get(uuid);
        if (cached != null) return cached;

        if (!plugin.getSettings().isPreloadOnJoin()) {
            String fetched = fetchAndCache(uuid);
            if (fetched != null) return fetched;
        }

        return languageManager.getDefaultLang();
    }

    public String getOrFetch(UUID uuid) {
        String cached = cache.get(uuid);
        if (cached != null) return cached;
        String fetched = fetchAndCache(uuid);
        return fetched != null ? fetched : languageManager.getDefaultLang();
    }

    private String fetchAndCache(UUID uuid) {
        try {
            String lang = DatabaseManager.getDatabase().getLanguagePlayer(uuid);
            if (lang != null && languageManager.isRegisteredLanguage(lang)) {
                cache.put(uuid, lang);
                return lang;
            }
        } catch (Exception ignored) {}
        return null;
    }

    public CompletableFuture<Void> setAsync(UUID uuid, String language) {
        cache.put(uuid, language);
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                DatabaseManager.getDatabase().setLanguagePlayer(uuid, language);
                if (plugin.getSettings().isDebug()) {
                    plugin.getLogger().info("[debug] Persisted language " + language + " for " + uuid);
                }
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}
