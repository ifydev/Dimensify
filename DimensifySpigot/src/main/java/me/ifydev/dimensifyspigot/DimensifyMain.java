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
package me.ifydev.dimensifyspigot;

import lombok.Getter;
import me.ifydev.dimensify.api.backend.BackendType;
import me.ifydev.dimensify.api.DimensifyAPI;
import me.ifydev.dimensifyspigot.backend.SpigotFlatFileHandler;
import me.ifydev.dimensifyspigot.commands.DimensifyCommand;
import me.ifydev.dimensifyspigot.events.PlayerJoin;
import me.ifydev.dimensifyspigot.events.PlayerPortal;
import me.ifydev.dimensifyspigot.events.WorldListener;
import me.ifydev.dimensifyspigot.inventory.InventoryController;
import me.ifydev.dimensifyspigot.portal.PortalRegistry;
import me.ifydev.dimensifyspigot.util.DisplayUtil;
import me.ifydev.dimensifyspigot.util.StorageUtil;
import me.ifydev.dimensifyspigot.util.Verifier;
import me.ifydev.dimensifyspigot.world.WorldController;
import me.ifydev.dimensifyspigot.world.WorldProperties;
import me.ifydev.dimensifyspigot.world.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.World;

import java.io.File;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.Optional;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class DimensifyMain extends JavaPlugin {

    @Getter private DimensifyAPI api;
    @Getter private boolean preloadWorlds = false;

    @Getter private WorldController worldController;
    @Getter private PortalRegistry portalRegistry;
    @Getter private InventoryController inventories;

    @Getter private boolean allowEntryByDefault = true;
    @Getter private boolean permissionRestrictDimensions = false;
    @Getter private boolean sendPlayersToDefaultWorldOnLogin = false;

    @Override
    public void onEnable() {
        try {
            createConfig();
        } catch (RuntimeException e) {
            getLogger().log(Level.SEVERE, "Could not load configuration", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Attempt to get the data handler
        String handler = getConfig().getString("storage", BackendType.SQLITE.getDisplayName());
        Optional<BackendType> backendType = BackendType.getHandlerForType(handler);
        if (!backendType.isPresent()) {
            getLogger().severe("Invalid storage type!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Initializing Dimensify API...");
        api = new DimensifyAPI();
        try {
            api.initialize(Bukkit.getWorlds().get(0).getName(), new DisplayUtil(), SpigotFlatFileHandler.class, backendType.get(), Verifier.verifyConnectionInformation(backendType.get()));
        } catch (Exception e) {
            getLogger().severe("Could not initialize Dimensify API!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Done!");

        preloadWorlds = getConfig().getBoolean("preload_worlds", true);
        allowEntryByDefault = getConfig().getBoolean("default_dimension_restriction", true);
        permissionRestrictDimensions= getConfig().getBoolean("permission_restrict_dimensions", false);
        sendPlayersToDefaultWorldOnLogin = getConfig().getBoolean("send_players_to_default_world_on_login", false);

        try {
            worldController = new WorldController(this);
            portalRegistry = new PortalRegistry();
            inventories = new InventoryController(this);
            inventories.separate();
            worldController.initialize();
            registerListeners();
            registerCommands();
            Bukkit.getScheduler().runTask(this, worldController::preload);
            Bukkit.getOnlinePlayers().forEach(inventories::join);
            Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getWorlds().forEach(world ->
                    WorldProperties.tick(world, worldController.getSettings().get(world.getName()))), 20, 20);
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                try {
                    inventories.saveAll();
                } catch (RuntimeException e) {
                    getLogger().severe("Could not save player inventories: " + e.getMessage());
                }
            }, 1200, 1200);
        } catch (RuntimeException e) {
            getLogger().log(Level.SEVERE, "Could not initialize world management", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (inventories != null) {
            try {
                inventories.saveAll();
            } catch (RuntimeException e) {
                getLogger().log(Level.SEVERE, "Could not save player inventories", e);
            }
        }
    }

    private void registerListeners() {
        PluginManager manager = getServer().getPluginManager();

        manager.registerEvents(new PlayerJoin(this), this);
        manager.registerEvents(new PlayerPortal(this), this);
        manager.registerEvents(new WorldListener(this), this);
    }

    private void registerCommands() {
        DimensifyCommand command = new DimensifyCommand();
        getCommand("dimensify").setExecutor(command);
        getCommand("dimensify").setTabCompleter(command);
    }

    public void reloadSettings() {
        if (worldController.isBusy()) throw new IllegalStateException("Wait for the world copy to finish before reloading.");
        YamlConfiguration config = StorageUtil.load(new File(getDataFolder(), "config.yml"));
        config.setDefaults(getConfig().getDefaults());
        WorldSettings proposed = new WorldSettings(new File(getDataFolder(), "worlds.yml"));
        if (!new HashSet<>(proposed.names()).equals(new HashSet<>(worldController.names())))
            throw new IllegalArgumentException("Use world commands to register and unregister worlds.");
        if (!InventoryController.SHARES.containsAll(config.getStringList("inventories.separate")))
            throw new IllegalArgumentException("Unknown inventory share in config.yml.");
        if (!config.getString("storage", "sqlite").equalsIgnoreCase(getConfig().getString("storage", "sqlite")))
            throw new IllegalArgumentException("Restart the server to change storage backends.");
        if (!config.getConfigurationSection("connection").getValues(true).equals(getConfig().getConfigurationSection("connection").getValues(true)))
            throw new IllegalArgumentException("Restart the server to change database connection settings.");
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            if (config.getBoolean("inventories.enabled") != inventories.enabled()
                    || !config.getStringList("inventories.separate").equals(getConfig().getStringList("inventories.separate")))
                throw new IllegalArgumentException("Change inventory settings while no players are online.");
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!proposed.group(player.getWorld().getName()).equals(worldController.getSettings().group(player.getWorld().getName())))
                    throw new IllegalArgumentException("Move players out of worlds whose inventory groups changed.");
            }
        }
        for (String name : proposed.names()) {
            ConfigurationSection section = proposed.get(name);
            for (String property : WorldProperties.PROPERTIES) {
                if (section.contains(property)) WorldProperties.parse(property, section.getString(property));
            }
        }
        inventories.saveAll();
        reloadConfig();
        inventories.separate();
        worldController.getSettings().reload();
        api.getDatabaseHandler().get().reload();
        preloadWorlds = getConfig().getBoolean("preload_worlds", false);
        allowEntryByDefault = getConfig().getBoolean("default_dimension_restriction", true);
        permissionRestrictDimensions = getConfig().getBoolean("permission_restrict_dimensions", false);
        sendPlayersToDefaultWorldOnLogin = getConfig().getBoolean("send_players_to_default_world_on_login", false);
        Bukkit.getWorlds().forEach(world -> WorldProperties.apply(world, worldController.getSettings().get(world.getName())));
    }

    private void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                boolean created = getDataFolder().mkdirs();
                if (!created) getLogger().log(Level.SEVERE, "Could not create config!");
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                StorageUtil.load(file);
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not load config.yml", e);
        }
    }

    public static DimensifyMain get() {
        return DimensifyMain.getPlugin(DimensifyMain.class);
    }
}
