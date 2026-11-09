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
import me.ifydev.dimensifyspigot.util.ColorUtil;
import me.ifydev.dimensifyspigot.world.WorldController;
import me.ifydev.dimensifyspigot.world.WorldProperties;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.Location;
import org.bukkit.World;

@RequiredArgsConstructor
public class WorldListener implements Listener {

    private final DimensifyMain plugin;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null || event.getFrom().getWorld().equals(event.getTo().getWorld())) return;
        try {
            if (!plugin.getWorldController().canEnter(event.getPlayer(), event.getTo().getWorld()))
                throw new IllegalArgumentException("You cannot enter that world, or it is full.");
            plugin.getInventories().prepare(event.getPlayer(), event.getTo().getWorld());
        } catch (RuntimeException e) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ColorUtil.makeReadable("&c" + e.getMessage()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        plugin.getInventories().changedWorld(event.getPlayer());
        WorldProperties.applyPlayer(event.getPlayer(), plugin.getWorldController().getSettings().get(event.getPlayer().getWorld().getName()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        WorldController controller = plugin.getWorldController();
        ConfigurationSection settings = controller.getSettings().get(event.getPlayer().getWorld().getName());
        try {
            boolean bed = event.isBedSpawn() && (settings == null || settings.getBoolean("bed-respawn", true));
            boolean anchor = event.isAnchorSpawn() && (settings == null || settings.getBoolean("anchor-respawn", true));
            if (!bed && !anchor && settings != null) {
                World target = settings.contains("respawn-world") ? controller.requireWorld(settings.getString("respawn-world")) : event.getPlayer().getWorld();
                event.setRespawnLocation(controller.spawn(target));
            }
            if (!controller.canEnter(event.getPlayer(), event.getRespawnLocation().getWorld()))
                event.setRespawnLocation(controller.spawn(Bukkit.getWorlds().get(0)));
            if (!controller.canEnter(event.getPlayer(), event.getRespawnLocation().getWorld()))
                throw new IllegalStateException("No permitted respawn world is available.");
            plugin.getInventories().prepare(event.getPlayer(), event.getRespawnLocation().getWorld());
        } catch (RuntimeException e) {
            plugin.getLogger().warning("Could not prepare respawn: " + e.getMessage());
            event.setRespawnLocation(event.getPlayer().getLocation());
            Bukkit.getScheduler().runTask(plugin, () -> event.getPlayer().kickPlayer("Could not load your respawn world. Please contact an administrator."));
        }
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getInventories().respawned(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        plugin.getInventories().death(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        try {
            plugin.getInventories().quit(event.getPlayer());
        } catch (RuntimeException e) {
            plugin.getLogger().severe("Could not save player inventory: " + e.getMessage());
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        WorldProperties.apply(event.getWorld(), plugin.getWorldController().getSettings().get(event.getWorld().getName()));
    }
}
