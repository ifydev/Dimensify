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

import lombok.RequiredArgsConstructor;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.portal.PortalLinks;
import me.ifydev.dimensifyspigot.portal.SpigotPortalMeta;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

@RequiredArgsConstructor
public class PlayerPortal implements Listener {

    private final DimensifyMain plugin;

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerAboutToTeleportEvent(PlayerPortalEvent event) {
        if (event.getCause() != TeleportCause.NETHER_PORTAL && event.getCause() != TeleportCause.END_PORTAL) return;
        boolean end = event.getCause() == TeleportCause.END_PORTAL;
        PortalLinks links = new PortalLinks(plugin.getWorldController());
        if (!links.enabled(event.getFrom().getWorld(), end)) {
            event.setCancelled(true);
            return;
        }
        Optional<SpigotPortalMeta> portal = plugin.getPortalRegistry().findCornersFromPosition(event.getFrom());
        if (portal.isPresent() && portal.get().getDestination().isPresent()) {
            event.setCancelled(true);
            String destination = portal.get().getDestination().get();
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    if (event.getPlayer().isOnline()) plugin.getWorldController().teleport(event.getPlayer(), plugin.getWorldController().destination(destination));
                } catch (RuntimeException e) {
                    event.getPlayer().sendMessage(ColorUtil.makeReadable("&c" + e.getMessage()));
                }
            });
            return;
        }
        try {
            Optional<Location> destination = links.destination(event.getFrom(), end);
            if (destination.isPresent()) {
                event.setTo(destination.get());
                event.setCanCreatePortal(!end);
            }
        } catch (RuntimeException e) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ColorUtil.makeReadable("&c" + e.getMessage()));
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        boolean end = event.getFrom().getWorld().getEnvironment() == World.Environment.THE_END
                || event.getTo() != null && event.getTo().getWorld().getEnvironment() == World.Environment.THE_END;
        PortalLinks links = new PortalLinks(plugin.getWorldController());
        if (!links.enabled(event.getFrom().getWorld(), end)) {
            event.setCancelled(true);
            return;
        }
        try {
            Optional<SpigotPortalMeta> portal = plugin.getPortalRegistry().findCornersFromPosition(event.getFrom());
            Optional<Location> destination = portal.isPresent() && portal.get().getDestination().isPresent()
                    ? Optional.of(plugin.getWorldController().destination(portal.get().getDestination().get())) : links.destination(event.getFrom(), end);
            destination.ifPresent(event::setTo);
        } catch (RuntimeException e) {
            event.setCancelled(true);
            plugin.getLogger().warning("Could not route entity portal: " + e.getMessage());
        }
    }
}
