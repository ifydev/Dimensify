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

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.portal.PortalMeta;
import me.ifydev.dimensify.api.portal.PortalType;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.portal.PortalCorners;
import me.ifydev.dimensifyspigot.portal.SpigotPortalMeta;
import me.ifydev.dimensifyspigot.portal.algo.PortalCornerDetection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 06/28/2018
 */
public class PortalHandler {

    public static String createPortal(Player player, Block lookingAt, String name) {
        if (lookingAt == null || lookingAt.getType() != Material.NETHER_PORTAL) return DimensifyConstants.MUST_LOOK_AT_PORTAL_BLOCKS;

        DimensifyMain plugin = DimensifyMain.get();
        Optional<AbstractDataHandler> db = plugin.getApi().getDatabaseHandler();
        if (!db.isPresent()) return DimensifyConstants.DATABASE_HANDLER_NOT_PRESENT;

        // We're just going to assume the user game us a corner of the portal.
        Optional<PortalCorners> corners = PortalCornerDetection.findPortalCornersFromAnyCorner(player, lookingAt.getLocation());
        if (!corners.isPresent()) return DimensifyConstants.INVALID_PORTAL;
        if (db.get().getPortal(name).isPresent())
            return DimensifyConstants.PORTAL_NAME_ALREADY_USED.replace("<NAME>", name);

        return db.get().createPortal(new SpigotPortalMeta(name, corners.get(), PortalType.NETHER, Optional.empty())) ?
            DimensifyConstants.PORTAL_CREATED.replace("<PORTAL>", name) : DimensifyConstants.COULD_NOT_CONNECT_TO_DATABASE;
    }

    public static String deletePortal(String portal) {
        DimensifyMain plugin = DimensifyMain.get();
        Optional<AbstractDataHandler> db = plugin.getApi().getDatabaseHandler();
        if (!db.isPresent()) return DimensifyConstants.DATABASE_HANDLER_NOT_PRESENT;

        if (!db.get().getPortal(portal).isPresent())
            return DimensifyConstants.PORTAL_DOES_NOT_EXIST.replace("<PORTAL>", portal);
        return db.get().removePortal(portal)
                ? DimensifyConstants.PORTAL_DELETED.replace("<PORTAL>", portal) : DimensifyConstants.COULD_NOT_CONNECT_TO_DATABASE;
    }

    public static String linkPortal(String portal, String destination) {
        DimensifyMain plugin = DimensifyMain.get();
        Optional<AbstractDataHandler> db = plugin.getApi().getDatabaseHandler();
        if (!db.isPresent()) return DimensifyConstants.DATABASE_HANDLER_NOT_PRESENT;

        // Make sure the portal exists.
        if (!db.get().getPortal(portal).isPresent())
            return DimensifyConstants.PORTAL_DOES_NOT_EXIST.replace("<PORTAL>", portal);
        // And make sure the world exists
        Optional<World> world = plugin.getWorldController().getWorld(destination);
        if (!world.isPresent())
            return DimensifyConstants.INVALID_WORLD.replace("<WORLD>", destination);

        return db.get().setPortalDestination(portal, destination)
                ? DimensifyConstants.PORTALS_LINKED : DimensifyConstants.COULD_NOT_CONNECT_TO_DATABASE;
    }

    public static List<String> listPortals() {
        DimensifyMain plugin = DimensifyMain.get();
        Optional<AbstractDataHandler> db = plugin.getApi().getDatabaseHandler();
        if (!db.isPresent()) return Collections.singletonList(DimensifyConstants.DATABASE_HANDLER_NOT_PRESENT);

        List<PortalMeta> portals = db.get().getPortals(false);
        return portals.stream().map(portal -> DimensifyConstants.PORTAL_LIST_FORMAT
                .replace("<NAME>", portal.getName())
                .replace("<X>", String.valueOf(portal.getX1()))
                .replace("<Y>", String.valueOf(portal.getY1()))
                .replace("<Z>", String.valueOf(portal.getZ1()))
                .replace("<DIMENSION>", portal.getWorld())
                .replace("<DESTINATION>", portal.getDestination()
                        .orElse(ChatColor.RED + "" + ChatColor.BOLD + "NONE"))).collect(Collectors.toList());
    }
}
