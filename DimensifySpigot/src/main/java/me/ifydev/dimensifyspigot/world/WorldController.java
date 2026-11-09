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

import lombok.Getter;
import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensify.api.portal.PortalMeta;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class WorldController {

    private final DimensifyMain plugin;
    @Getter private final WorldSettings settings;
    private final WorldFiles files;
    private final Set<String> busy = new HashSet<>();

    public WorldController(DimensifyMain plugin) {
        this.plugin = plugin;
        settings = new WorldSettings(new File(plugin.getDataFolder(), "worlds.yml"));
        files = new WorldFiles(Bukkit.getWorldContainer().toPath());
    }

    private AbstractDataHandler database() {
        return plugin.getApi().getDatabaseHandler().orElseThrow(() -> new IllegalStateException("No data handler present."));
    }

    public void initialize() {
        for (Dimension dimension : new ArrayList<>(database().getDimensions(false))) {
            if (settings.get(dimension.getName()) != null) continue;
            ConfigurationSection section = settings.create(dimension.getName());
            section.set("type", dimension.getType().isEmpty() ? "default" : dimension.getType());
            if (dimension.getMeta().isPresent() && !dimension.getMeta().get().isBlank()) {
                GenerationOptions.parse(dimension.getMeta().get().split("\\s+")).forEach(section::set);
            }
            section.set("autoload", plugin.isPreloadWorlds() && plugin.getConfig().getStringList("world_preload").contains(dimension.getName()));
        }
        for (World world : Bukkit.getWorlds()) {
            if (settings.get(world.getName()) == null) capture(world);
            if (database().getDimensions(false).stream().noneMatch(d -> d.getName().equals(world.getName()))) register(world);
        }
        settings.save();
        Bukkit.getWorlds().forEach(world -> WorldProperties.apply(world, settings.get(world.getName())));
    }

    public void preload() {
        for (String name : settings.names()) {
            if (settings.get(name).getBoolean("autoload", false)) {
                try {
                    requireWorld(name);
                } catch (RuntimeException e) {
                    plugin.getLogger().warning("Could not preload " + name + ": " + e.getMessage());
                }
            }
        }
    }

    private void capture(World world) {
        ConfigurationSection section = settings.create(world.getName());
        section.set("type", world.getWorldType().getName());
        section.set("environment", world.getEnvironment().name());
        section.set("seed", String.valueOf(world.getSeed()));
        section.set("structures", world.canGenerateStructures());
        section.set("autoload", true);
        section.set("pvp", world.getPVP());
        section.set("difficulty", world.getDifficulty().name());
        section.set("monsters", world.getAllowMonsters());
        section.set("animals", world.getAllowAnimals());
    }

    private void register(World world) {
        boolean primary = world.equals(Bukkit.getWorlds().get(0)) && database().getDefaultDimension(false).isEmpty();
        if (!database().createDimension(new Dimension(world.getName(), world.getWorldType().getName(), Optional.empty(), primary)))
            throw new IllegalStateException("Could not register world: " + world.getName());
    }

    public List<String> names() {
        return settings.names();
    }

    public void pair(String overworld, String nether, String end) {
        World normalWorld = requireWorld(overworld);
        World netherWorld = requireWorld(nether);
        World endWorld = requireWorld(end);
        if (normalWorld.getEnvironment() != World.Environment.NORMAL || netherWorld.getEnvironment() != World.Environment.NETHER
                || endWorld.getEnvironment() != World.Environment.THE_END)
            throw new IllegalArgumentException("Pair requires an Overworld, Nether and End, in that order.");
        settings.get(normalWorld.getName()).set("nether", netherWorld.getName());
        settings.get(normalWorld.getName()).set("end", endWorld.getName());
        settings.get(netherWorld.getName()).set("overworld", normalWorld.getName());
        settings.get(endWorld.getName()).set("overworld", normalWorld.getName());
        settings.save();
    }

    public void link(String name, String type, String destination) {
        if (!List.of("nether", "end", "overworld").contains(type)) throw new IllegalArgumentException("Link type must be nether, end or overworld.");
        World source = requireWorld(name);
        String target = destination.equalsIgnoreCase("none") ? null : requireWorld(destination).getName();
        settings.get(source.getName()).set(type, target);
        settings.save();
    }

    public World requireWorld(String name) {
        name = settings.resolve(name);
        checkBusy(name);
        ConfigurationSection section = settings.get(name);
        if (section == null) throw new IllegalArgumentException("World is not registered: " + name);
        World world = Bukkit.getWorld(name);
        if (world != null) return world;
        if (!Files.isRegularFile(files.resolve(name).resolve("level.dat")))
            throw new IllegalArgumentException("World folder is missing. Import an existing world or create a new one.");
        world = GenerationOptions.creator(name, section).createWorld();
        if (world == null) throw new IllegalStateException("Could not load world: " + name);
        WorldProperties.apply(world, section);
        return world;
    }

    public Optional<World> getWorld(String name) {
        return getWorld(null, name, false);
    }

    public Optional<World> getWorld(CommandSender sender, String name, boolean andMake) {
        try {
            if (andMake && settings.get(settings.resolve(name)) == null) return Optional.of(create(name, "default", new String[0]));
            return Optional.of(requireWorld(name));
        } catch (RuntimeException e) {
            if (sender != null) sender.sendMessage(ColorUtil.makeReadable("&c" + e.getMessage()));
            return Optional.empty();
        }
    }

    public World create(String name, String type, String[] options) {
        WorldFiles.validateName(name);
        checkBusy(name);
        if (files.exists(name) || settings.get(name) != null || Bukkit.getWorld(name) != null)
            throw new IllegalArgumentException("World already exists. Use import for an existing folder.");
        YamlConfiguration config = new YamlConfiguration();
        config.set("type", GenerationOptions.type(type).getName());
        GenerationOptions.parse(options).forEach(config::set);
        WorldCreator creator = GenerationOptions.creator(name, config);
        World world = creator.createWorld();
        if (world == null) throw new IllegalStateException("Could not create world: " + name);
        capture(world);
        config.getValues(false).forEach(settings.get(name)::set);
        settings.save();
        register(world);
        return world;
    }

    public World importWorld(String name, String environment, String generator) {
        WorldFiles.validateName(name);
        checkBusy(name);
        if (settings.get(name) != null) throw new IllegalArgumentException("World is already registered.");
        if (!Files.isRegularFile(files.resolve(name).resolve("level.dat"))) throw new IllegalArgumentException("World folder has no level.dat.");
        YamlConfiguration config = new YamlConfiguration();
        config.set("environment", environment);
        if (generator != null) config.set("generator", generator);
        World world = GenerationOptions.creator(name, config).createWorld();
        if (world == null) throw new IllegalStateException("Could not import world.");
        capture(world);
        if (generator != null) settings.get(name).set("generator", generator);
        settings.save();
        register(world);
        return world;
    }

    public void loadWorld(DimensifyWorld creator) {
        requireWorld(creator.name());
    }

    private void checkBusy(String name) {
        if (busy.contains(name.toLowerCase(Locale.ROOT))) throw new IllegalStateException("A world operation is already running for " + name);
    }

    public boolean isBusy() {
        return !busy.isEmpty();
    }

    private void protect(String name) {
        if (Bukkit.getWorlds().get(0).getName().equalsIgnoreCase(name) || database().getDefaultDimension(false).equalsIgnoreCase(name))
            throw new IllegalArgumentException("The main or default world cannot be unloaded, removed or reset.");
    }

    public void unload(String name, boolean save) {
        name = settings.resolve(name);
        checkBusy(name);
        protect(name);
        World world = requireWorld(name);
        if (!world.getPlayers().isEmpty()) throw new IllegalArgumentException("Move players out of the world first.");
        if (!Bukkit.unloadWorld(world, save)) throw new IllegalStateException("Could not unload world.");
    }

    public void unregister(String name) {
        name = settings.resolve(name);
        checkBusy(name);
        protect(name);
        if (Bukkit.getWorld(name) != null) unload(name, true);
        if (!database().removeDimension(name)) throw new IllegalStateException("Could not unregister world.");
        cleanup(name);
        settings.remove(name);
    }

    private void cleanup(String name) {
        for (PortalMeta portal : new ArrayList<>(database().getPortals(false))) {
            if (portal.getWorld().equalsIgnoreCase(name) || portal.getDestination().filter(name::equalsIgnoreCase).isPresent())
                database().removePortal(portal.getName());
        }
        for (String world : names()) {
            ConfigurationSection section = settings.get(world);
            for (String property : List.of("nether", "end", "overworld", "respawn-world")) {
                if (section.getString(property, "").equalsIgnoreCase(name)) section.set(property, null);
            }
        }
        for (String anchor : new ArrayList<>(settings.anchors().getKeys(false))) {
            if (settings.anchors().getString(anchor + ".world", "").equalsIgnoreCase(name)) settings.anchors().set(anchor, null);
        }
    }

    public Path delete(String name) {
        name = settings.resolve(name);
        protect(name);
        requireWorld(name);
        evacuate(name);
        unload(name, true);
        Path backup = backupPath(name);
        try {
            files.move(name, backup);
            if (!database().removeDimension(name)) {
                Files.move(backup, files.resolve(name));
                requireWorld(name);
                throw new IllegalStateException("Could not remove world record; world was restored.");
            }
            cleanup(name);
            settings.remove(name);
            return backup;
        } catch (IOException e) {
            throw new IllegalStateException("Could not archive world. Its registration has been retained.", e);
        } catch (RuntimeException e) {
            try {
                if (Files.exists(backup) && !files.exists(name)) {
                    Files.move(backup, files.resolve(name));
                    requireWorld(name);
                }
            } catch (Exception restore) {
                e.addSuppressed(restore);
            }
            throw e;
        }
    }

    public boolean deleteWorld(CommandSender sender, String name) {
        delete(name);
        return true;
    }

    private Path backupPath(String name) {
        return plugin.getDataFolder().toPath().resolve("backups").resolve(name + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID());
    }

    private void evacuate(String name) {
        World world = requireWorld(name);
        if (world.getPlayers().isEmpty()) return;
        Location destination = spawn(Bukkit.getWorlds().get(0));
        for (Player player : new ArrayList<>(world.getPlayers())) {
            if (!teleport(player, destination)) throw new IllegalStateException("Could not move every player out of the world.");
        }
    }

    public void cloneWorld(String source, String destination, Consumer<String> response) {
        source = settings.resolve(source);
        WorldFiles.validateName(destination);
        checkBusy(destination);
        if (files.exists(destination) || settings.get(destination) != null) throw new IllegalArgumentException("Destination already exists.");
        World world = requireWorld(source);
        unload(source, true);
        String original = source;
        busy.add(original.toLowerCase(Locale.ROOT));
        busy.add(destination.toLowerCase(Locale.ROOT));
        Map<String, Object> properties = new LinkedHashMap<>(settings.get(source).getValues(true));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Exception failure = null;
            try {
                files.copy(original, destination);
            } catch (Exception e) {
                failure = e;
            }
            Exception result = failure;
            Bukkit.getScheduler().runTask(plugin, () -> {
                busy.remove(original.toLowerCase(Locale.ROOT));
                busy.remove(destination.toLowerCase(Locale.ROOT));
                try {
                    requireWorld(original);
                    if (result != null) throw new IllegalStateException("Could not copy world. Partial destination folder was retained for inspection.", result);
                    ConfigurationSection section = settings.create(destination);
                    properties.forEach((key, value) -> { if (!(value instanceof ConfigurationSection)) section.set(key, value); });
                    for (String property : List.of("alias", "nether", "end", "overworld", "respawn-world", "inventory-group")) section.set(property, null);
                    settings.save();
                    World cloned = requireWorld(destination);
                    cloned.setSpawnLocation(readLocation(null, cloned));
                    cloned.save();
                    register(cloned);
                    response.accept("World cloned: " + destination);
                } catch (RuntimeException e) {
                    plugin.getLogger().warning(e.getMessage());
                    response.accept("Could not finish clone: " + e.getMessage());
                }
            });
        });
    }

    public Path regenerate(String name, String seed) {
        name = settings.resolve(name);
        protect(name);
        World original = requireWorld(name);
        ConfigurationSection section = settings.get(name);
        YamlConfiguration proposed = new YamlConfiguration();
        section.getValues(false).forEach(proposed::set);
        if (seed != null) proposed.set("seed", seed);
        WorldCreator creator = GenerationOptions.creator(name, proposed);
        evacuate(name);
        unload(name, true);
        Path backup = backupPath(name);
        try {
            files.move(name, backup);
            World regenerated = creator.createWorld();
            if (regenerated == null) throw new IllegalStateException("World generation failed.");
            if (seed != null) section.set("seed", seed);
            settings.save();
            WorldProperties.apply(regenerated, section);
            return backup;
        } catch (Exception e) {
            try {
                World failed = Bukkit.getWorld(name);
                if (failed != null && !Bukkit.unloadWorld(failed, false)) throw new IOException("Could not unload failed world.");
                if (Files.exists(backup)) {
                    if (files.exists(name)) files.move(name, backupPath(name + "-failed"));
                    Files.move(backup, files.resolve(name));
                    section.set("seed", String.valueOf(original.getSeed()));
                    settings.save();
                    requireWorld(name);
                }
            } catch (Exception restore) {
                e.addSuppressed(restore);
            }
            throw new IllegalStateException("Could not regenerate world. Original backup: " + backup, e);
        }
    }

    public void modify(String name, String property, String value) {
        name = settings.resolve(name);
        checkBusy(name);
        ConfigurationSection section = settings.get(name);
        if (section == null) throw new IllegalArgumentException("World is not registered.");
        Object parsed = WorldProperties.parse(property, value);
        if (property.equals("respawn-world")) requireWorld(value);
        if (property.equals("inventory-group") && Bukkit.getWorld(name) != null && !Bukkit.getWorld(name).getPlayers().isEmpty())
            throw new IllegalArgumentException("Move players out of the world before changing its inventory group.");
        if (property.equals("alias") && names().stream().anyMatch(world -> !world.equals(section.getName())
                && (world.equalsIgnoreCase(value) || settings.get(world).getString("alias", "").equalsIgnoreCase(value))))
            throw new IllegalArgumentException("Alias is already in use.");
        Object previous = section.get(property);
        section.set(property, parsed);
        try {
            settings.save();
        } catch (RuntimeException e) {
            section.set(property, previous);
            throw e;
        }
        World world = Bukkit.getWorld(name);
        if (world != null) WorldProperties.apply(world, section);
    }

    public Location spawn(World world) {
        ConfigurationSection section = settings.get(world.getName());
        Location location = readLocation(section == null ? null : section.getConfigurationSection("spawn"), world);
        return SafeTeleport.find(location).orElseThrow(() -> new IllegalArgumentException("No safe destination near the spawn of " + world.getName()));
    }

    public static Location readLocation(ConfigurationSection section, World world) {
        if (section == null) {
            Location spawn = world.getSpawnLocation().clone();
            spawn.setWorld(world);
            return spawn;
        }
        Location location = new Location(world, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"),
                (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
        if (!Double.isFinite(location.getX()) || !Double.isFinite(location.getY()) || !Double.isFinite(location.getZ())
                || !Float.isFinite(location.getYaw()) || !Float.isFinite(location.getPitch()))
            throw new IllegalArgumentException("Invalid saved location.");
        return location;
    }

    public static void writeLocation(ConfigurationSection section, Location location) {
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }

    public void setSpawn(Player player) {
        World world = player.getWorld();
        ConfigurationSection section = settings.get(world.getName());
        if (section == null) throw new IllegalArgumentException("World is not registered.");
        if (!SafeTeleport.safe(player.getLocation())) throw new IllegalArgumentException("Stand at a safe location first.");
        writeLocation(section.createSection("spawn"), player.getLocation());
        settings.save();
        world.setSpawnLocation(player.getLocation());
    }

    public Location destination(String destination) {
        if (!destination.startsWith("@")) return spawn(requireWorld(destination));
        ConfigurationSection anchor = settings.anchors().getConfigurationSection(destination.substring(1));
        if (anchor == null) throw new IllegalArgumentException("Unknown anchor: " + destination);
        return SafeTeleport.find(readLocation(anchor, requireWorld(anchor.getString("world"))))
                .orElseThrow(() -> new IllegalArgumentException("No safe location near that anchor."));
    }

    public void anchor(Player player, String name) {
        WorldFiles.validateName(name);
        if (!SafeTeleport.safe(player.getLocation())) throw new IllegalArgumentException("Stand at a safe location first.");
        writeLocation(settings.anchors().createSection(name), player.getLocation());
        settings.save();
    }

    public boolean canEnter(Player player, World world) {
        String name = world.getName();
        String deny = "dimension." + name + ".deny";
        if (player.isPermissionSet(deny) && player.hasPermission(deny)) return false;
        if (plugin.isPermissionRestrictDimensions() && !plugin.isAllowEntryByDefault() && !player.hasPermission("dimension." + name + ".allow"))
            return false;
        ConfigurationSection section = settings.get(name);
        int limit = section == null ? 0 : section.getInt("player-limit");
        return player.getWorld().equals(world) || limit == 0 || world.getPlayers().size() < limit;
    }

    public boolean teleport(Player player, Location destination) {
        if (!canEnter(player, destination.getWorld())) throw new IllegalArgumentException("You cannot enter that world, or it is full.");
        boolean teleported = player.teleport(destination);
        if (teleported) player.setFallDistance(0);
        return teleported;
    }

    public static void enterDimension(Player player, World dimension) {
        DimensifyMain plugin = DimensifyMain.get();
        try {
            plugin.getWorldController().teleport(player, plugin.getWorldController().spawn(dimension));
        } catch (RuntimeException e) {
            player.sendMessage(ColorUtil.makeReadable("&c" + e.getMessage()));
        }
    }
}
