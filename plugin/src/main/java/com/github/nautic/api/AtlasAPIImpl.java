package com.github.nautic.api;

import com.github.nautic.AtlasLang;
import com.github.nautic.api.event.PlayerLanguageChangeEvent;
import com.github.nautic.cache.LanguageCache;
import com.github.nautic.language.LanguageHandler;
import com.github.nautic.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Internal implementation of {@link AtlasAPI}. Not part of the public API surface.
 * Consumers should always go through {@link AtlasAPI} / {@link AtlasProvider}.
 */
public final class AtlasAPIImpl implements AtlasAPI {

    private final AtlasLang plugin;
    private final LanguageHandler languageHandler;
    private final LanguageManager languageManager;
    private final LanguageCache cache;

    public AtlasAPIImpl(AtlasLang plugin) {
        this.plugin = plugin;
        this.languageHandler = plugin.getLanguageHandler();
        this.languageManager = plugin.getLanguageManager();
        this.cache = plugin.getLanguageCache();
    }


    @Override
    public @NotNull String message(@NotNull Player player, @NotNull String path) {
        return message(player.getUniqueId(), DEFAULT_NAMESPACE, path);
    }

    @Override
    public @NotNull String message(@NotNull UUID uuid, @NotNull String path) {
        return message(uuid, DEFAULT_NAMESPACE, path);
    }

    @Override
    public @NotNull String messageInLang(@NotNull String langInput, @NotNull String path) {
        return messageInLang(langInput, DEFAULT_NAMESPACE, path);
    }

    @Override
    public @NotNull String message(@NotNull Player player, @NotNull String namespace, @NotNull String path) {
        return message(player.getUniqueId(), namespace, path);
    }

    @Override
    public @NotNull String message(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path) {
        return messageInLang(cache.get(uuid), namespace, path);
    }

    @Override
    public @NotNull String messageInLang(@NotNull String langInput, @NotNull String namespace, @NotNull String path) {
        String resolved = languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) resolved = languageManager.getDefaultLang();
        return languageHandler.get(resolved, namespace, path);
    }

    @Override
    public @NotNull String messageOrDefault(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path, @NotNull String fallback) {
        String lang = cache.get(uuid);
        String resolved = languageManager.resolveLanguageStrict(lang);
        if (resolved == null) resolved = languageManager.getDefaultLang();
        String raw = languageHandler.getRaw(resolved, namespace, path);
        return raw != null ? raw : fallback;
    }


    @Override
    public boolean has(@NotNull String langInput, @NotNull String path) {
        return has(langInput, DEFAULT_NAMESPACE, path);
    }

    @Override
    public boolean has(@NotNull String langInput, @NotNull String namespace, @NotNull String path) {
        String resolved = languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) return false;
        return languageHandler.exists(resolved, namespace, path);
    }


    @Override
    public boolean setPlayerLanguage(@NotNull Player player, @NotNull String langInput) {
        return setPlayerLanguage(player.getUniqueId(), langInput);
    }

    @Override
    public boolean setPlayerLanguage(@NotNull UUID uuid, @NotNull String langInput) {
        String resolved = languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) return false;

        String previous = cache.get(uuid);

        PlayerLanguageChangeEvent event = new PlayerLanguageChangeEvent(uuid, previous, resolved);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        cache.setAsync(uuid, event.getNewLanguage());
        return true;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> setPlayerLanguageAsync(@NotNull UUID uuid, @NotNull String langInput) {
        String resolved = languageManager.resolveLanguageStrict(langInput);
        if (resolved == null) return CompletableFuture.completedFuture(false);

        String previous = cache.get(uuid);
        PlayerLanguageChangeEvent event = new PlayerLanguageChangeEvent(uuid, previous, resolved);

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                result.complete(false);
                return;
            }
            cache.setAsync(uuid, event.getNewLanguage())
                    .whenComplete((v, ex) -> result.complete(ex == null));
        });
        return result;
    }


    @Override
    public @NotNull String getPlayerLanguage(@NotNull Player player) {
        return cache.get(player.getUniqueId());
    }

    @Override
    public @NotNull String getPlayerLanguage(@NotNull UUID uuid) {
        return cache.get(uuid);
    }


    @Override
    public boolean isLanguageRegistered(@NotNull String langInput) {
        return languageManager.resolveLanguageStrict(langInput) != null;
    }

    @Override
    public boolean supports(@NotNull String langInput) {
        return isLanguageRegistered(langInput);
    }

    @Override
    public @NotNull Set<String> getRegisteredLanguages() {
        return languageManager.getRegisteredLanguages();
    }

    @Override
    public @NotNull Set<String> getRegisteredLocales() {
        return languageManager.getRegisteredLocales();
    }

    @Override
    public @NotNull String getDefaultLanguage() {
        return languageManager.getDefaultLang();
    }

    @Override
    public @Nullable String getLocaleOf(@NotNull String folder) {
        return languageManager.getLocaleOf(folder);
    }

    @Override
    public @NotNull Set<String> compatibleLanguages(@NotNull Collection<String> candidates) {
        Set<String> matched = new HashSet<>();
        for (String candidate : candidates) {
            if (languageManager.resolveLanguageStrict(candidate) != null) {
                matched.add(candidate);
            }
        }
        return matched;
    }

    @Override
    public int getApiVersion() {
        return API_VERSION;
    }
}
