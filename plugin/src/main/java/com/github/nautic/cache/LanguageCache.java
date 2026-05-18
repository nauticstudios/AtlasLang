package com.github.nautic.cache;

import com.github.nautic.AtlasLang;
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
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        loadFromDatabase(event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
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
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to preload language for " + uuid + ": " + e.getMessage());
        }
    }

    public String get(UUID uuid) {
        String cached = cache.get(uuid);
        if (cached != null) return cached;
        return languageManager.getDefaultLang();
    }

    public String getOrFetch(UUID uuid) {
        String cached = cache.get(uuid);
        if (cached != null) return cached;
        try {
            String lang = DatabaseManager.getDatabase().getLanguagePlayer(uuid);
            if (lang != null && languageManager.isRegisteredLanguage(lang)) {
                return lang;
            }
        } catch (Exception ignored) {}
        return languageManager.getDefaultLang();
    }

    public CompletableFuture<Void> setAsync(UUID uuid, String language) {
        cache.put(uuid, language);
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                DatabaseManager.getDatabase().setLanguagePlayer(uuid, language);
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
