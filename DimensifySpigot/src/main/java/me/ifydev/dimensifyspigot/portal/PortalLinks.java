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
package me.ifydev.dimensifyspigot.portal;

import lombok.RequiredArgsConstructor;
import me.ifydev.dimensifyspigot.world.WorldController;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

@RequiredArgsConstructor
public class PortalLinks {

    private final WorldController controller;

    public boolean enabled(World world, boolean end) {
        ConfigurationSection settings = controller.getSettings().get(world.getName());
        return settings == null || settings.getBoolean(end ? "end-portals" : "nether-portals", true);
    }

    public Optional<Location> destination(Location from, boolean end) {
        World source = from.getWorld();
        ConfigurationSection settings = controller.getSettings().get(source.getName());
        if (settings == null) return Optional.empty();
        String property = source.getEnvironment() == World.Environment.NORMAL ? (end ? "end" : "nether") : "overworld";
        String destination = settings.getString(property);
        if (destination == null) return Optional.empty();
        World world = controller.requireWorld(destination);
        if (world.getEnvironment() == World.Environment.THE_END) return Optional.of(WorldController.readLocation(null, world));
        if (end || source.getEnvironment() == World.Environment.THE_END)
            return Optional.of(controller.spawn(world));
        double scale = scale(source) / scale(world);
        double halfBorder = Math.max(0, world.getWorldBorder().getSize() / 2 - 1);
        Location center = world.getWorldBorder().getCenter();
        double x = Math.max(center.getX() - halfBorder, Math.min(center.getX() + halfBorder, from.getX() * scale));
        double z = Math.max(center.getZ() - halfBorder, Math.min(center.getZ() + halfBorder, from.getZ() * scale));
        double y = Math.max(world.getMinHeight() + 1, Math.min(world.getMaxHeight() - 2, from.getY()));
        return Optional.of(new Location(world, x, y, z, from.getYaw(), from.getPitch()));
    }

    private double scale(World world) {
        ConfigurationSection settings = controller.getSettings().get(world.getName());
        return settings == null ? (world.getEnvironment() == World.Environment.NETHER ? 8 : 1)
                : settings.getDouble("coordinate-scale", world.getEnvironment() == World.Environment.NETHER ? 8 : 1);
    }
}
