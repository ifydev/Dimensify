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
import me.ifydev.dimensifyspigot.commands.DimensifyCommand;
import me.ifydev.dimensifyspigot.events.PlayerJoin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class DimensifyMain extends JavaPlugin {

    @Getter private DimensifyAPI api;
    @Getter private boolean preloadWorlds = false;
    @Getter private List<String> worldNames;

    @Override
    public void onEnable() {
        getLogger().info("Initializing Dimensify API...");
        api = new DimensifyAPI();
        api.intialize();
        getLogger().info("Done!");

        createConfig();

        preloadWorlds = getConfig().getBoolean("preload_worlds", false);
        worldNames = getConfig().getStringList("worlds");

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {

    }

    private void registerListeners() {
        PluginManager manager = getServer().getPluginManager();

        manager.registerEvents(new PlayerJoin(), this);
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

    public void addWorld(String name) {
        worldNames.add(name);
        getConfig().set("worlds", worldNames);
        saveConfig();
    }

    public static Optional<DimensifyMain> get() {
        return Optional.ofNullable(DimensifyMain.getPlugin(DimensifyMain.class));
    }
}
