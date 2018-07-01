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

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class WorldController {

    public Optional<World> getWorld(String name) {
        return getWorld(null, name, false);
    }

    public Optional<World> getWorld(CommandSender sender, String name, boolean andMake) {
        DimensifyMain plugin = DimensifyMain.get();

        World world = Bukkit.getWorld(name);

        if (world == null && (plugin.getApi().getDatabaseHandler().map(db -> db.getDimension(name).isPresent()).orElse(false) || andMake)) {
            if (sender != null) sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.LOADING_WORLD));
            this.loadWorld(new DimensifyWorld(name, DimensifyMain.get()));
            world = Bukkit.getWorld(name);
        }
        return Optional.ofNullable(world);
    }

    public void loadWorld(DimensifyWorld creator) {
        World world;
        try {
            world = creator.createWorld();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (world == null) {
            System.out.println("World was null after generation.");
            return;
        }
        DimensifyMain plugin = DimensifyMain.get();

        plugin.getApi().getDatabaseHandler().ifPresent(db ->
                db.createDimension(new Dimension(creator.name(), creator.type().getName(), creator.getMeta(), creator.isDefault())));
    }

    public boolean deleteWorld(CommandSender sender, String dimensionName) {
        DimensifyMain plugin = DimensifyMain.get();
        boolean worldExists = plugin.getApi().getDatabaseHandler().map(db -> db.getDimension(dimensionName).isPresent()).orElse(false);
        if (!worldExists) return false;

        Optional<World> world = plugin.getWorldController().getWorld(sender, dimensionName, false);
        if (!world.isPresent()) return false;
        // Teleport all the players from the deleted world, to the main world.
        plugin.getApi().getDatabaseHandler().ifPresent(db ->
                db.removeDimension(dimensionName));

        world.get().getPlayers().forEach(player -> player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

        File folder = world.get().getWorldFolder();
        Bukkit.unloadWorld(world.get(), false);

        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void enterDimension(Player player, World dimension) {
        if (dimension == null) return;

        DimensifyMain plugin = DimensifyMain.get();

        if (plugin.isPermissionRestrictDimensions()) {
            boolean playerIsAllowedToEnterDimension = (player.hasPermission("dimension." + dimension.getName() + ".allow") && !player.hasPermission("dimension." + dimension.getName() + ".deny")) || plugin.isAllowEntryByDefault();
            if (!playerIsAllowedToEnterDimension) {
                player.sendMessage(ColorUtil.makeReadable(DimensifyConstants.CANNOT_ENTER_THIS_DIMENSION));
                return;
            }
        }

        player.teleport(dimension.getSpawnLocation());
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorUtil.makeReadable(DimensifyConstants.WHOOSH)));
    }
}
