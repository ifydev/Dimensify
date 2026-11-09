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

import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Optional;
import java.util.Set;

public class SafeTeleport {

    private static final Set<Material> DANGEROUS = Set.of(Material.LAVA, Material.FIRE, Material.SOUL_FIRE,
            Material.CACTUS, Material.MAGMA_BLOCK, Material.CAMPFIRE, Material.SOUL_CAMPFIRE,
            Material.SWEET_BERRY_BUSH, Material.POWDER_SNOW, Material.WITHER_ROSE, Material.NETHER_PORTAL, Material.END_PORTAL);

    public static boolean safe(Location location) {
        World world = location.getWorld();
        if (location.getY() <= world.getMinHeight() || location.getY() >= world.getMaxHeight() - 1) return false;
        if (!world.getWorldBorder().isInside(location)) return false;
        Block feet = location.getBlock();
        Block head = world.getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());
        Block floor = world.getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
        return feet.isPassable() && head.isPassable() && !feet.isLiquid() && !head.isLiquid() && floor.getType().isSolid()
                && !DANGEROUS.contains(feet.getType()) && !DANGEROUS.contains(head.getType()) && !DANGEROUS.contains(floor.getType());
    }

    public static Optional<Location> find(Location destination) {
        if (safe(destination)) return Optional.of(destination.clone());
        World world = destination.getWorld();
        for (int radius = 0; radius <= 8; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != radius) continue;
                    int blockX = destination.getBlockX() + x;
                    int blockZ = destination.getBlockZ() + z;
                    int top = world.getEnvironment() == World.Environment.NETHER ? Math.min(120, world.getMaxHeight() - 2)
                            : Math.min(world.getHighestBlockYAt(blockX, blockZ) + 1, world.getMaxHeight() - 2);
                    for (int y = top; y > world.getMinHeight(); y--) {
                        Location location = new Location(world, blockX + 0.5, y, blockZ + 0.5, destination.getYaw(), destination.getPitch());
                        if (safe(location)) return Optional.of(location);
                    }
                }
            }
        }
        return Optional.empty();
    }
}
