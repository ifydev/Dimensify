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

import com.google.gson.JsonParser;
import org.bukkit.block.Biome;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class GenerationOptions {

    public static Map<String, String> parse(String[] arguments) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];
            if (argument.equalsIgnoreCase("structure") || argument.equalsIgnoreCase("structures")) {
                options.put("structures", "true");
                continue;
            }
            String[] split = argument.split("=", 2);
            if (split.length != 2 || split[1].isEmpty()) throw new IllegalArgumentException("Expected option=value: " + argument);
            String key = split[0].toLowerCase(Locale.ROOT);
            String value = split[1];
            if (key.equals("generator-settings")) {
                while (!validJson(value) && i + 1 < arguments.length) value += " " + arguments[++i];
                if (!validJson(value)) throw new IllegalArgumentException("Generator settings must be a JSON object.");
            }
            if (!Set.of("env", "seed", "structures", "generator", "biome", "generator-settings").contains(key))
                throw new IllegalArgumentException("Unknown generation option: " + key);
            options.put(key.equals("env") ? "environment" : key, value);
        }
        return options;
    }

    private static boolean validJson(String value) {
        try {
            return JsonParser.parseString(value).isJsonObject();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static WorldType type(String name) {
        if (name.equalsIgnoreCase("normal")) return WorldType.NORMAL;
        WorldType type = WorldType.getByName(name.replace("_", ""));
        if (type == null) throw new IllegalArgumentException("Invalid world type: " + name);
        return type;
    }

    public static boolean bool(String value) {
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
            throw new IllegalArgumentException("Expected true or false.");
        return Boolean.parseBoolean(value);
    }

    public static WorldCreator creator(String name, ConfigurationSection settings) {
        WorldCreator creator = new WorldCreator(name);
        creator.type(type(settings.getString("type", "default")));
        creator.environment(World.Environment.valueOf(settings.getString("environment", "normal").toUpperCase(Locale.ROOT)));
        creator.generateStructures(bool(settings.getString("structures", "true")));
        if (settings.contains("seed")) {
            String seed = settings.getString("seed");
            try {
                creator.seed(Long.parseLong(seed));
            } catch (NumberFormatException e) {
                creator.seed(seed.hashCode());
            }
        }
        if (settings.contains("generator")) {
            String generator = settings.getString("generator");
            String plugin = generator.split(":", 2)[0];
            if (!Bukkit.getPluginManager().isPluginEnabled(plugin))
                throw new IllegalArgumentException("Generator plugin is not enabled: " + plugin);
            creator.generator(generator);
            if (creator.generator() == null) throw new IllegalArgumentException("Generator plugin did not provide a generator.");
        }
        if (settings.contains("biome")) {
            NamespacedKey key = NamespacedKey.fromString(settings.getString("biome").toLowerCase(Locale.ROOT));
            Biome biome = key == null ? null : Registry.BIOME.get(key);
            if (biome == null) throw new IllegalArgumentException("Unknown biome: " + settings.getString("biome"));
            creator.biomeProvider(new SingleBiomeProvider(biome));
        }
        if (settings.contains("generator-settings")) {
            String json = settings.getString("generator-settings");
            if (!validJson(json)) throw new IllegalArgumentException("Generator settings must be a JSON object.");
            creator.generatorSettings(json);
        }
        return creator;
    }
}
