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
package me.ifydev.dimensifyspigot.commands.handlers;

import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.world.WorldController;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BasicHandler {

    public static String createDimension(CommandSender sender, String type, String name, String[] options) {
        DimensifyMain.get().getWorldController().create(name, type, options);
        return "World created: " + name;
    }

    public static String goToDimension(Player player, String dimension) {
        WorldController controller = DimensifyMain.get().getWorldController();
        return controller.teleport(player, controller.destination(dimension)) ? "" : "Teleport was cancelled.";
    }

    public static String deleteDimension(CommandSender sender, String dimension) {
        return "World deleted. Original files: " + DimensifyMain.get().getWorldController().delete(dimension);
    }

    public static String sendPlayerToDimension(CommandSender sender, String playerName, String dimension) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return "Player is not online.";
        return goToDimension(player, dimension);
    }

    public static List<String> listWorlds() {
        return DimensifyMain.get().getWorldController().names().stream().map(name -> name + (Bukkit.getWorld(name) == null ? " (unloaded)" : " (loaded)"))
                .collect(Collectors.toList());
    }

    public static String setOrGetDefaultWorld(Optional<String> world) {
        DimensifyMain plugin = DimensifyMain.get();
        if (world.isPresent() && !plugin.getApi().getDatabaseHandler().get().setDefaultDimension(plugin.getWorldController().requireWorld(world.get()).getName()))
            return "Could not set default world.";
        return plugin.getApi().getDatabaseHandler().get().getDefaultDimension(false);
    }

    public static String unloadDimension(String dimension, boolean save) {
        DimensifyMain.get().getWorldController().unload(dimension, save);
        return "World unloaded.";
    }

    public static String cacheStatus() {
        AbstractDataHandler database = DimensifyMain.get().getApi().getDatabaseHandler().get();
        return database.getDimensions(false).size() + " worlds, " + database.getPortals(false).size() + " portals";
    }

    public static String purgeCache() {
        DimensifyMain.get().getApi().getDatabaseHandler().get().reload();
        return "Cache reloaded.";
    }
}