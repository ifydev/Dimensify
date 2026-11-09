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

import me.ifydev.dimensifyspigot.util.StorageUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorldSettings {

    private final File file;
    private YamlConfiguration storage;

    public WorldSettings(File file) {
        this.file = file;
        reload();
    }

    public void reload() {
        YamlConfiguration proposed = StorageUtil.load(file);
        ConfigurationSection worlds = proposed.getConfigurationSection("worlds");
        if (worlds != null) {
            for (String name : worlds.getKeys(false)) {
                WorldFiles.validateName(name);
                ConfigurationSection section = worlds.getConfigurationSection(name);
                if (section == null) throw new IllegalArgumentException("Invalid world settings: " + name);
                for (String property : WorldProperties.PROPERTIES) {
                    if (section.contains(property)) section.set(property, WorldProperties.parse(property, section.getString(property)));
                }
            }
        } else if (proposed.contains("worlds")) throw new IllegalArgumentException("World settings must contain a worlds section.");
        storage = proposed;
    }

    public void save() {
        StorageUtil.save(storage, file);
    }

    public List<String> names() {
        ConfigurationSection worlds = storage.getConfigurationSection("worlds");
        return worlds == null ? new ArrayList<>() : new ArrayList<>(worlds.getKeys(false));
    }

    public ConfigurationSection get(String name) {
        WorldFiles.validateName(name);
        return storage.getConfigurationSection("worlds." + name);
    }

    public ConfigurationSection create(String name) {
        ConfigurationSection existing = get(name);
        return existing == null ? storage.createSection("worlds." + name) : existing;
    }

    public void remove(String name) {
        WorldFiles.validateName(name);
        storage.set("worlds." + name, null);
        save();
    }

    public String resolve(String name) {
        for (String world : names()) {
            if (world.equalsIgnoreCase(name)) return world;
            ConfigurationSection section = get(world);
            if (section.getString("alias", "").equalsIgnoreCase(name)) return world;
        }
        return name;
    }

    public String group(String world) {
        ConfigurationSection settings = get(world);
        String group = settings == null ? world : settings.getString("inventory-group", world);
        WorldFiles.validateName(group);
        return group.toLowerCase(Locale.ROOT);
    }

    public ConfigurationSection anchors() {
        ConfigurationSection anchors = storage.getConfigurationSection("anchors");
        return anchors == null ? storage.createSection("anchors") : anchors;
    }
}
