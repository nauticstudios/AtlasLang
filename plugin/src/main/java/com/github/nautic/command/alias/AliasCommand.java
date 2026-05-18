package com.github.nautic.command.alias;

import com.github.nautic.AtlasLang;
import com.github.nautic.api.AtlasAPI;
import com.github.nautic.cache.LanguageCache;
import com.github.nautic.language.LanguageHandler;
import com.github.nautic.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AliasCommand implements CommandExecutor {

    private final AtlasLang plugin;
    private final LanguageManager languageManager;
    private final LanguageHandler lang;
    private final LanguageCache cache;

    public AliasCommand(AtlasLang plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.lang = plugin.getLanguageHandler();
        this.cache = plugin.getLanguageCache();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        String playerLang = cache.get(player.getUniqueId());

        if (args.length != 1) {
            player.sendMessage(lang.get(playerLang, playerLang, "usage.language-command")
                    .replace("{command}", label));
            return true;
        }

        String resolved = languageManager.resolveLanguageStrict(args[0]);
        if (resolved == null) {
            player.sendMessage(lang.get(playerLang, playerLang, "errors.language-not-found")
                    .replace("{input}", args[0]));
            return true;
        }

        AtlasAPI.setLanguageAsync(player.getUniqueId(), resolved).whenComplete((ok, ex) ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (ex != null || !Boolean.TRUE.equals(ok)) return;
                    player.sendMessage(lang.get(resolved, resolved, "success.language-set")
                            .replace("{language}", resolved)
                            .replace("{player}", player.getName()));
                })
        );
        return true;
    }
}
