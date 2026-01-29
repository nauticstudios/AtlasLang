package com.github.nautic.commands.customs;

import com.github.nautic.AtlasLang;
import com.github.nautic.utils.addColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public class LangsLoader {

    public static void registerLanguageCommands(AtlasLang plugin) {
        List<String> aliases = plugin.getMainConfig().getStringList("commands");

        if (aliases == null || aliases.isEmpty()) {
            sendConsole("&c[AtlasLang] No language commands registered (commands list is empty)");
            return;
        }

        try {
            Field commandMapField = Bukkit.getServer()
                    .getClass()
                    .getDeclaredField("commandMap");

            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            for (String alias : aliases) {
                alias = alias.toLowerCase();

                PluginCommand cmd = createPluginCommand(alias, plugin);
                if (cmd == null) continue;

                cmd.setExecutor(new AliasLangExecutor(plugin));
                cmd.setTabCompleter(new AliasLangTabCompleter(plugin));

                commandMap.register(plugin.getName(), cmd);

                sendConsole("&#35ADFF(AtlasLang) &fRegistered language alias: &a/" + alias);
            }

        } catch (Exception e) {
            sendConsole("&c[AtlasLang] Failed to register language aliases");
            e.printStackTrace();
        }
    }

    private static PluginCommand createPluginCommand(String name, Plugin plugin) {
        try {
            Constructor<PluginCommand> constructor =
                    PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, plugin);
        } catch (Exception e) {
            return null;
        }
    }

    private static void sendConsole(String message) {
        Bukkit.getConsoleSender().sendMessage(addColor.Set(message));
    }
}