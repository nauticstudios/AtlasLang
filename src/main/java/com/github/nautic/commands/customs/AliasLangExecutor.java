package com.github.nautic.commands.customs;

import com.github.nautic.AtlasLang;
import com.github.nautic.database.DatabaseManager;
import com.github.nautic.handler.LangHandler;
import com.github.nautic.manager.LanguageManager;
import com.github.nautic.utils.addColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AliasLangExecutor implements CommandExecutor {

    private final AtlasLang plugin;
    private final LanguageManager languageManager;
    private final LangHandler lang;

    public AliasLangExecutor(AtlasLang plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.lang = plugin.getLangHandler();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        String defaultLang = languageManager.getDefaultLang();
        String playerLang = DatabaseManager.getDatabase()
                .getLanguagePlayer(player.getUniqueId());

        if (playerLang == null) playerLang = defaultLang;

        if (args.length != 1) {
            player.sendMessage(addColor.Set(
                    lang.get(playerLang, playerLang, "usage.language-command")
                            .replace("{command}", label)
            ));
            return true;
        }

        String resolved = languageManager.resolveLanguageStrict(args[0]);
        if (resolved == null) {
            player.sendMessage(addColor.Set(
                    lang.get(playerLang, playerLang, "errors.language-not-found")
                            .replace("{input}", args[0])
            ));
            return true;
        }

        DatabaseManager.getDatabase()
                .setLanguagePlayer(player.getUniqueId(), resolved);

        player.sendMessage(addColor.Set(
                lang.get(resolved, resolved, "success.language-set")
                        .replace("{language}", resolved).replace("{player}", player.getName())
        ));

        return true;
    }
}
