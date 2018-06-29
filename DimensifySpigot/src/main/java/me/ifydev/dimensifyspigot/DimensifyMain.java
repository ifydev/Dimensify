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
import me.ifydev.dimensify.api.DimensifyAPI;
import me.ifydev.dimensify.api.backend.BackendType;
import me.ifydev.dimensifyspigot.backend.SpigotFlatFileHandler;
import me.ifydev.dimensifyspigot.commands.DimensifyCommand;
import me.ifydev.dimensifyspigot.events.PlayerJoin;
import me.ifydev.dimensifyspigot.events.PlayerPortal;
import me.ifydev.dimensifyspigot.portal.PortalRegistry;
import me.ifydev.dimensifyspigot.util.Verifier;
import me.ifydev.dimensifyspigot.world.WorldController;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class DimensifyMain extends JavaPlugin {

    @Getter private DimensifyAPI api;
    @Getter private boolean preloadWorlds = false;

    @Getter private WorldController worldController;
    @Getter private PortalRegistry portalRegistry;

    @Getter private boolean allowEntryByDefault = true;
    @Getter private boolean permissionRestrictDimensions = false;

    @Override
    public void onEnable() {
        createConfig();

        // Attempt to get the data handler
        String handler = getConfig().getString("storage", BackendType.SQLITE.getDisplayName());
        Optional<BackendType> backendType = BackendType.getHandlerForType(handler);
        if (!backendType.isPresent()) {
            getLogger().severe("Invalid storage type!");
            return;
        }

        getLogger().info("Initializing Dimensify API...");
        api = new DimensifyAPI();
        try {
            api.initialize(SpigotFlatFileHandler.class, backendType.get(), Verifier.verifyConnectionInformation(backendType.get()));
        } catch (Exception e) {
            getLogger().severe("Could not initialize Dimensify API!");
            e.printStackTrace();
            return;
        }
        getLogger().info("Done!");

        worldController = new WorldController();
        portalRegistry = new PortalRegistry();

        preloadWorlds = getConfig().getBoolean("preload_worlds", true);

//        if (preloadWorlds) worldController.loadAllWorlds(allWorlds, this);

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {

    }

    private void registerListeners() {
        PluginManager manager = getServer().getPluginManager();

        manager.registerEvents(new PlayerJoin(), this);
        manager.registerEvents(new PlayerPortal(), this);
    }

    private void registerCommands() {
        getCommand("dimensify").setExecutor(new DimensifyCommand());
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
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static DimensifyMain get() {
        return DimensifyMain.getPlugin(DimensifyMain.class);
    }
}
