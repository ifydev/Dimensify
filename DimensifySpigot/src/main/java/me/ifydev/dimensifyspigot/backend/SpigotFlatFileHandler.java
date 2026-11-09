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
package me.ifydev.dimensifyspigot.backend;

import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.backend.ConnectionInformation;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensify.api.portal.PortalMeta;
import me.ifydev.dimensify.api.portal.PortalType;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.StorageUtil;
import me.ifydev.dimensifyspigot.world.WorldFiles;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import java.util.Optional;

public class SpigotFlatFileHandler extends AbstractDataHandler {

    private File storageFile;
    private YamlConfiguration storage;
    private String defaultWorld;

    public SpigotFlatFileHandler(ConnectionInformation connectionInformation) {
        super(connectionInformation);
    }

    @Override
    public void initialize(String defaultWorld) {
        this.defaultWorld = defaultWorld;
        storageFile = new File(DimensifyMain.get().getDataFolder(), "storage.yml");
        storage = StorageUtil.load(storageFile);
    }

    @Override
    public boolean connect() {
        return storage != null;
    }

    @Override
    public void reload() {
        storage = StorageUtil.load(storageFile);
        dimensions = getDimensions(true);
        portals = getPortals(true);
    }

    @Override
    public void drop() {
        dimensions = new ArrayList<>();
        portals = new ArrayList<>();
    }

    private boolean update(Consumer<YamlConfiguration> change) {
        try {
            YamlConfiguration proposed = new YamlConfiguration();
            proposed.loadFromString(storage.saveToString());
            change.accept(proposed);
            StorageUtil.save(proposed, storageFile);
            storage = proposed;
            dimensions = getDimensions(true);
            portals = getPortals(true);
            return true;
        } catch (Exception e) {
            DimensifyMain.get().getLogger().warning("Could not save storage: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean createPortal(PortalMeta meta) {
        WorldFiles.validateName(meta.getName());
        if (getPortal(meta.getName()).isPresent()) return false;
        return update(proposed -> {
            String base = "portal." + meta.getName() + ".";
            proposed.set(base + "x1", meta.getX1());
            proposed.set(base + "y1", meta.getY1());
            proposed.set(base + "z1", meta.getZ1());
            proposed.set(base + "x2", meta.getX2());
            proposed.set(base + "y2", meta.getY2());
            proposed.set(base + "z2", meta.getZ2());
            proposed.set(base + "world", meta.getWorld());
            proposed.set(base + "type", meta.getType().name());
            proposed.set("link." + meta.getName(), meta.getDestination().orElse(null));
        });
    }

    @Override
    public boolean removePortal(String name) {
        Optional<PortalMeta> portal = getPortal(name);
        if (!portal.isPresent()) return false;
        return update(proposed -> {
            proposed.set("portal." + portal.get().getName(), null);
            proposed.set("link." + portal.get().getName(), null);
        });
    }

    @Override
    public List<PortalMeta> getPortals(boolean skipCache) {
        if (!skipCache) return new ArrayList<>(portals);
        List<PortalMeta> loaded = new ArrayList<>();
        ConfigurationSection section = storage.getConfigurationSection("portal");
        if (section == null) return loaded;
        for (String name : section.getKeys(false)) {
            ConfigurationSection portal = section.getConfigurationSection(name);
            PortalType type = PortalType.findType(portal.getString("type", "UNKNOWN"));
            if (type == PortalType.UNKNOWN) throw new IllegalStateException("Invalid portal type: " + name);
            loaded.add(new PortalMeta(name, portal.getInt("x1"), portal.getInt("x2"), portal.getInt("y1"), portal.getInt("y2"),
                    portal.getInt("z1"), portal.getInt("z2"), portal.getString("world"),
                    Optional.ofNullable(storage.getString("link." + name)), type));
        }
        return loaded;
    }

    @Override
    public boolean setPortalDestination(String portal, String destination) {
        Optional<PortalMeta> meta = getPortal(portal);
        return meta.isPresent() && update(proposed -> proposed.set("link." + meta.get().getName(), destination));
    }

    @Override
    public boolean createDimension(Dimension dimension) {
        WorldFiles.validateName(dimension.getName());
        if (getDimension(dimension.getName()).isPresent()) return false;
        return update(proposed -> {
            String base = "dimensions." + dimension.getName() + ".";
            proposed.set(base + "name", dimension.getName());
            proposed.set(base + "type", dimension.getType());
            proposed.set(base + "meta", dimension.getMeta().orElse(""));
            proposed.set(base + "default", dimension.isDefault());
        });
    }

    @Override
    public boolean removeDimension(String name) {
        Optional<Dimension> dimension = getDimension(name);
        return dimension.isPresent() && update(proposed -> proposed.set("dimensions." + dimension.get().getName(), null));
    }

    @Override
    public List<Dimension> getDimensions(boolean skipCache) {
        if (!skipCache) return new ArrayList<>(dimensions);
        List<Dimension> loaded = new ArrayList<>();
        ConfigurationSection section = storage.getConfigurationSection("dimensions");
        if (section == null) return loaded;
        for (String name : section.getKeys(false)) {
            ConfigurationSection dimension = section.getConfigurationSection(name);
            loaded.add(new Dimension(name, dimension.getString("type", "default"), Optional.ofNullable(dimension.getString("meta")), dimension.getBoolean("default")));
        }
        return loaded;
    }

    @Override
    public Optional<Dimension> getDimension(String name) {
        return dimensions.stream().filter(dimension -> dimension.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public Optional<PortalMeta> getPortal(String name) {
        return portals.stream().filter(portal -> portal.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public boolean setDefaultDimension(String name) {
        Optional<Dimension> dimension = getDimension(name);
        if (!dimension.isPresent()) return false;
        return update(proposed -> dimensions.forEach(world -> proposed.set("dimensions." + world.getName() + ".default", world.getName().equals(dimension.get().getName()))));
    }

    @Override
    public String getDefaultDimension(boolean skipCache) {
        return getDimensions(skipCache).stream().filter(Dimension::isDefault).map(Dimension::getName).findFirst().orElse(defaultWorld);
    }
}