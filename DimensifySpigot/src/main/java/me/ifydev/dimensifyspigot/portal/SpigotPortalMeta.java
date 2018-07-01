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

import me.ifydev.dimensify.api.portal.PortalMeta;
import me.ifydev.dimensify.api.portal.PortalType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

/**
 * @author Innectic
 * @since 06/26/2018
 */
public class SpigotPortalMeta extends PortalMeta {

    public SpigotPortalMeta(String name, PortalCorners corners, PortalType type, Optional<String> destination) {
        super(name, corners.getFirst().getBlockX(), corners.getSecond().getBlockX(), corners.getFirst().getBlockY(),
                corners.getSecond().getBlockY(), corners.getFirst().getBlockZ(), corners.getSecond().getBlockZ(),
                corners.getFirst().getWorld().getName(), destination, type);
    }

    public SpigotPortalMeta(String name, int x1, int x2, int y1, int y2, int z1, int z2, String world, Optional<String> destination, PortalType type) {
        super(name, x1, x2, y1, y2, z1, z2, world, destination, type);
    }

    public Optional<Location> getFirst() {
        World world = Bukkit.getWorld(this.getWorld());
        if (world == null) return Optional.empty();

        return Optional.of(new Location(world, getX1(), getY1(), getZ1()));
    }

    public Optional<Location> getSecond() {
        World world = Bukkit.getWorld(this.getWorld());
        if (world == null) return Optional.empty();

        return Optional.of(new Location(world, getX2(), getY2(), getZ2()));
    }

    public Optional<PortalCorners> getCorners() {
        Optional<Location> first = getFirst();
        Optional<Location> second = getSecond();

        if (!first.isPresent() || !second.isPresent()) return Optional.empty();

        return Optional.of(new PortalCorners(first.get(), second.get()));
    }
}
