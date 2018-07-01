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
package me.ifydev.dimensifyspigot.events;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import me.ifydev.dimensifyspigot.world.WorldController;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.Optional;

/**
 * @author Innectic
 * @since 06/22/2018
 */
public class PlayerPortal implements Listener {

    @EventHandler
    public void onPlayerAboutToTeleportEvent(PlayerPortalEvent e) {
        // We cancel this event if they're within one of our portals, so that we can send them ourselves.
        DimensifyMain plugin = DimensifyMain.get();

        plugin.getPortalRegistry().findCornersFromPosition(e.getFrom()).ifPresent(corners -> {
            e.setCancelled(true);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Player player = e.getPlayer();

                if (!corners.getDestination().isPresent()) return;

                String link = corners.getDestination().get();
                Optional<World> dimension = DimensifyMain.get().getWorldController().getWorld(link);
                if (!dimension.isPresent()) {
                    player.sendMessage(ColorUtil.makeReadable(DimensifyConstants.THIS_DIMENSION_DOES_NOT_EXIST_ANYMORE));
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    WorldController.enterDimension(player, dimension.get());
                    // Make sure the player won't get damaged for no reason
                    player.setFallDistance(0);
                });
            });
        });
    }
}
