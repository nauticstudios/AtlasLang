package com.github.nautic.update;

import com.github.nautic.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class UpdateListener implements Listener {

    private final JavaPlugin plugin;
    private final UpdateChecker updateChecker;
    private final int resourceId;

    public UpdateListener(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.updateChecker = new UpdateChecker(plugin, resourceId);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp() && !player.hasPermission("atlaslang.admin")) return;

        updateChecker.getVersion(latestVersion -> {
            String currentVersion = plugin.getDescription().getVersion();
            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                player.sendMessage(ColorUtil.colorize("&r"));
                player.sendMessage(ColorUtil.colorize("  &#35ADFF&lAtlasLang &7» &fA new version is available!"));
                player.sendMessage(ColorUtil.colorize("  &f[Updated] Your version: &#FF6A6A" + currentVersion + " &f| Latest: &#90FF6A" + latestVersion));
                player.sendMessage(ColorUtil.colorize("  &fURL: &#FFBF6Ahttps://www.spigotmc.org/resources/" + resourceId + "/"));
                player.sendMessage(ColorUtil.colorize("&r"));
            }
        });
    }
}
