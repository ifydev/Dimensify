package me.ifydev.dimensifyspigot.util;

import org.bukkit.ChatColor;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class ColorUtil {
    public static String makeReadable(String convert) {
        return ChatColor.translateAlternateColorCodes('&', convert);
    }
}

