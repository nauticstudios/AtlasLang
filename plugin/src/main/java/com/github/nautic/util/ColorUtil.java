package com.github.nautic.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINIMESSAGE_HINT = Pattern.compile("<[^>]+>");

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    private ColorUtil() {}

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return message;

        String processed = MINIMESSAGE_HINT.matcher(message).find()
                ? safeMiniMessage(message)
                : message;

        return translateLegacy(translateHex(processed));
    }

    public static String colorizeWithPlaceholders(Player player, String message) {
        if (message == null || message.isEmpty()) return message;
        if (player != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return colorize(message);
    }

    public static String translateLegacy(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String translateHex(String message) {
        if (message.indexOf('&') < 0) return message;

        final char colorChar = ChatColor.COLOR_CHAR;
        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuilder buffer = new StringBuilder(message.length() + 16);

        while (matcher.find()) {
            final String hex = matcher.group(1);
            matcher.appendReplacement(buffer,
                    Matcher.quoteReplacement(
                            colorChar + "x"
                                    + colorChar + hex.charAt(0)
                                    + colorChar + hex.charAt(1)
                                    + colorChar + hex.charAt(2)
                                    + colorChar + hex.charAt(3)
                                    + colorChar + hex.charAt(4)
                                    + colorChar + hex.charAt(5)
                    )
            );
        }

        return matcher.appendTail(buffer).toString();
    }

    public static String safeMiniMessage(String message) {
        try {
            Component component = MINI_MESSAGE.deserialize(message);
            return LEGACY_SERIALIZER.serialize(component);
        } catch (Exception ignored) {
            return message;
        }
    }
}
