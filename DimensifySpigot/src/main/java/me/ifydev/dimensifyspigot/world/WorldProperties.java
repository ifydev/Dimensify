/*
 *
 * This file is part of Dimensify, licensed under the MIT License (MIT).
 * Copyright (c) Innectic
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.ifydev.dimensifyspigot.world;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;

import java.util.List;
import java.util.Locale;

public class WorldProperties {

    public static final List<String> PROPERTIES = List.of("alias", "gamemode", "difficulty", "pvp", "monsters", "animals",
            "weather", "time", "allow-flight", "player-limit", "autoload", "respawn-world", "bed-respawn", "anchor-respawn",
            "inventory-group", "border-size", "border-x", "border-z", "coordinate-scale", "nether-portals", "end-portals");

    public static Object parse(String property, String value) {
        if (!PROPERTIES.contains(property)) throw new IllegalArgumentException("Unknown property: " + property);
        if (List.of("pvp", "monsters", "animals", "allow-flight", "autoload", "bed-respawn", "anchor-respawn", "nether-portals", "end-portals").contains(property))
            return GenerationOptions.bool(value);
        if (property.equals("gamemode")) return GameMode.valueOf(value.toUpperCase(Locale.ROOT)).name();
        if (property.equals("difficulty")) return Difficulty.valueOf(value.toUpperCase(Locale.ROOT)).name();
        if (property.equals("weather")) {
            if (!List.of("normal", "clear", "rain", "thunder").contains(value))
                throw new IllegalArgumentException("Weather must be normal, clear, rain or thunder.");
            return value;
        }
        if (property.equals("time")) {
            if (value.equals("normal")) return value;
            int time = Integer.parseInt(value);
            if (time < 0 || time >= 24000) throw new IllegalArgumentException("Time must be normal or 0-23999.");
            return time;
        }
        if (property.equals("player-limit")) {
            int limit = Integer.parseInt(value);
            if (limit < 0) throw new IllegalArgumentException("Player limit must be zero or greater.");
            return limit;
        }
        if (property.startsWith("border-")) {
            double number = Double.parseDouble(value);
            if (!Double.isFinite(number) || Math.abs(number) > 29999984 || (property.equals("border-size") && number < 1))
                throw new IllegalArgumentException("Invalid world border value.");
            return number;
        }
        if (property.equals("coordinate-scale")) {
            double scale = Double.parseDouble(value);
            if (!Double.isFinite(scale) || scale < 0.001 || scale > 1000) throw new IllegalArgumentException("Coordinate scale must be 0.001-1000.");
            return scale;
        }
        if (!property.equals("alias")) WorldFiles.validateName(value);
        if (value.isBlank()) throw new IllegalArgumentException("Value must not be empty.");
        return value;
    }

    public static void apply(World world, ConfigurationSection settings) {
        if (settings == null) return;
        if (settings.contains("difficulty")) world.setDifficulty(Difficulty.valueOf(settings.getString("difficulty").toUpperCase(Locale.ROOT)));
        if (settings.contains("pvp")) world.setPVP(settings.getBoolean("pvp"));
        if (settings.contains("animals") || settings.contains("monsters"))
            world.setSpawnFlags(settings.getBoolean("monsters", world.getAllowMonsters()), settings.getBoolean("animals", world.getAllowAnimals()));
        if (settings.contains("border-size")) world.getWorldBorder().setSize(settings.getDouble("border-size"));
        if (settings.contains("border-x") || settings.contains("border-z"))
            world.getWorldBorder().setCenter(settings.getDouble("border-x", world.getWorldBorder().getCenter().getX()),
                    settings.getDouble("border-z", world.getWorldBorder().getCenter().getZ()));
        ConfigurationSection rules = settings.getConfigurationSection("gamerules");
        if (rules != null) rules.getKeys(false).forEach(rule -> setRule(world, rule, rules.getString(rule)));
        tick(world, settings);
        world.getPlayers().forEach(player -> applyPlayer(player, settings));
    }

    public static void tick(World world, ConfigurationSection settings) {
        if (settings == null) return;
        String time = settings.getString("time", "normal");
        if (!time.equals("normal")) world.setTime(Long.parseLong(time));
        String weather = settings.getString("weather", "normal");
        if (!weather.equals("normal")) {
            world.setStorm(!weather.equals("clear"));
            world.setThundering(weather.equals("thunder"));
            world.setWeatherDuration(1200);
            world.setThunderDuration(1200);
            if (weather.equals("clear")) world.setClearWeatherDuration(1200);
        }
    }

    public static void applyPlayer(Player player, ConfigurationSection settings) {
        if (settings == null) return;
        if (settings.contains("gamemode")) player.setGameMode(GameMode.valueOf(settings.getString("gamemode").toUpperCase(Locale.ROOT)));
        boolean flight = settings.getBoolean("allow-flight", false) || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
        if (!flight) player.setFlying(false);
        player.setAllowFlight(flight);
    }

    public static Object ruleValue(GameRule<?> rule, String value) {
        if (rule.getType() == Boolean.class) return GenerationOptions.bool(value);
        if (rule.getType() == Integer.class) return Integer.parseInt(value);
        throw new IllegalArgumentException("Unsupported game rule type.");
    }

    @SuppressWarnings("unchecked")
    public static void setRule(World world, String name, String value) {
        GameRule<?> rule = rule(name);
        if (rule == null) throw new IllegalArgumentException("Unknown game rule: " + name);
        if (!world.setGameRule((GameRule<Object>) rule, ruleValue(rule, value)))
            throw new IllegalArgumentException("Could not set game rule: " + name);
    }

    public static GameRule<?> rule(String name) {
        String normalized = name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(normalized);
        return key == null ? null : Registry.GAME_RULE.get(key);
    }
}
