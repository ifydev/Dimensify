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
import me.ifydev.dimensifyspigot.world.WorldProperties;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.World;

@RequiredArgsConstructor
public class PlayerJoin implements Listener {

    private final DimensifyMain plugin;

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        try {
            plugin.getInventories().join(event.getPlayer());
            if (plugin.isSendPlayersToDefaultWorldOnLogin()) {
                String world = plugin.getApi().getDatabaseHandler().get().getDefaultDimension(false);
                if (!world.isEmpty()) plugin.getWorldController().teleport(event.getPlayer(), plugin.getWorldController().destination(world));
            }
            if (!plugin.getWorldController().canEnter(event.getPlayer(), event.getPlayer().getWorld())) {
                World fallback = Bukkit.getWorlds().get(0);
                if (!plugin.getWorldController().teleport(event.getPlayer(), plugin.getWorldController().spawn(fallback)))
                    throw new IllegalStateException("Could not enter the fallback world.");
            }
            WorldProperties.applyPlayer(event.getPlayer(), plugin.getWorldController().getSettings().get(event.getPlayer().getWorld().getName()));
        } catch (RuntimeException e) {
            plugin.getLogger().severe("Could not load player data: " + e.getMessage());
            event.getPlayer().kickPlayer("Could not load your world data. Please contact an administrator.");
        }
    }
}
