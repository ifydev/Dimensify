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
package me.ifydev.dimensifyspigot.inventory;

import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.StorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.World;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InventoryController {

    public static final List<String> SHARES = List.of("inventory", "ender-chest", "experience", "health", "hunger", "potion-effects");
    private final DimensifyMain plugin;
    private final Map<UUID, String> active = new HashMap<>();
    private final Set<UUID> failed = new HashSet<>();
    private final Map<UUID, PlayerState> deaths = new HashMap<>();

    public InventoryController(DimensifyMain plugin) {
        this.plugin = plugin;
    }

    public boolean enabled() {
        return plugin.getConfig().getBoolean("inventories.enabled", false);
    }

    public List<String> separate() {
        if (!plugin.getConfig().isList("inventories.separate")) return SHARES;
        List<String> separate = plugin.getConfig().getStringList("inventories.separate");
        if (!SHARES.containsAll(separate)) throw new IllegalArgumentException("Unknown inventory share in config.yml.");
        return separate;
    }

    private String group(World world) {
        return plugin.getWorldController().getSettings().group(world.getName());
    }

    private File file(Player player) {
        return new File(plugin.getDataFolder(), "players/" + player.getUniqueId() + ".yml");
    }

    public void join(Player player) {
        if (!enabled()) return;
        try {
            YamlConfiguration storage = StorageUtil.load(file(player));
            String group = group(player.getWorld());
            String saved = storage.getString("active-group", group);
            if (!saved.equals(group)) PlayerState.load(storage.getConfigurationSection("groups." + group)).apply(player, separate());
            active.put(player.getUniqueId(), group);
            save(player);
        } catch (RuntimeException e) {
            failed.add(player.getUniqueId());
            throw e;
        }
    }

    public void prepare(Player player, World destination) {
        if (!enabled() || player.getWorld().equals(destination)) return;
        if (failed.contains(player.getUniqueId())) throw new IllegalStateException("Your inventory could not be loaded. Please reconnect.");
        if (group(player.getWorld()).equals(group(destination))) return;
        if (player.getOpenInventory().getCursor() != null && !player.getOpenInventory().getCursor().getType().isAir())
            throw new IllegalArgumentException("Put the item on your cursor away before changing worlds.");
        player.closeInventory();
        save(player);
        PlayerState.load(StorageUtil.load(file(player)).getConfigurationSection("groups." + group(destination)));
    }

    public void changedWorld(Player player) {
        if (!enabled()) return;
        String destination = group(player.getWorld());
        String source = active.get(player.getUniqueId());
        if (destination.equals(source)) return;
        try {
            YamlConfiguration storage = StorageUtil.load(file(player));
            PlayerState.load(storage.getConfigurationSection("groups." + destination)).apply(player, separate());
            deaths.remove(player.getUniqueId());
            active.put(player.getUniqueId(), destination);
            save(player);
        } catch (RuntimeException e) {
            failed.add(player.getUniqueId());
            plugin.getLogger().severe("Could not switch inventory for " + player.getUniqueId() + ": " + e.getMessage());
            player.kickPlayer("Could not load your world inventory. Please contact an administrator.");
        }
    }

    public void save(Player player) {
        if (!enabled() || failed.contains(player.getUniqueId())) return;
        String group = active.getOrDefault(player.getUniqueId(), group(player.getWorld()));
        YamlConfiguration storage = StorageUtil.load(file(player));
        PlayerState state = deaths.containsKey(player.getUniqueId()) ? deaths.get(player.getUniqueId()) : PlayerState.capture(player);
        state.save(storage.createSection("groups." + group));
        storage.set("active-group", group);
        StorageUtil.save(storage, file(player));
    }

    public void quit(Player player) {
        save(player);
        active.remove(player.getUniqueId());
        failed.remove(player.getUniqueId());
        deaths.remove(player.getUniqueId());
    }

    public void death(PlayerDeathEvent event) {
        if (!enabled()) return;
        deaths.put(event.getEntity().getUniqueId(), PlayerState.death(event));
        save(event.getEntity());
    }

    public void respawned(Player player) {
        deaths.remove(player.getUniqueId());
    }

    public void saveAll() {
        RuntimeException failure = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                save(player);
            } catch (RuntimeException e) {
                plugin.getLogger().severe("Could not save inventory for " + player.getUniqueId() + ": " + e.getMessage());
                failure = e;
            }
        }
        if (failure != null) throw failure;
    }
}
