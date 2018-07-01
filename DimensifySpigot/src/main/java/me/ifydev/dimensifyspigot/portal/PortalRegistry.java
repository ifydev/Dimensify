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
import me.ifydev.dimensifyspigot.DimensifyMain;
import org.bukkit.Location;

import java.util.List;
import java.util.Optional;

/**
 * @author Innectic
 * @since 06/22/2018
 */
public class PortalRegistry {

    public Optional<SpigotPortalMeta> findCornersFromPosition(Location location) {
        DimensifyMain plugin = DimensifyMain.get();
        if (!plugin.getApi().getDatabaseHandler().isPresent()) return Optional.empty();

        List<PortalMeta> portalCorners = plugin.getApi().getDatabaseHandler().get().getPortals(false);
        for (PortalMeta regularMeta : portalCorners) {
            SpigotPortalMeta meta = new SpigotPortalMeta(regularMeta.getName(), regularMeta.getX1(), regularMeta.getX2(),
                    regularMeta.getY1(), regularMeta.getY2(), regularMeta.getZ1(), regularMeta.getZ2(),
                    regularMeta.getWorld(), regularMeta.getDestination(), regularMeta.getType());

            Optional<PortalCorners> cornersOptional = meta.getCorners();
            if (!cornersOptional.isPresent()) continue;
            PortalCorners corners = cornersOptional.get();

            Location bottom;
            Location top;

            // Find which location is the top / bottom
            if (corners.getFirst().getY() < corners.getSecond().getY()) {
                top = corners.getSecond();
                bottom = corners.getFirst();
            } else {
                top = corners.getFirst();
                bottom = corners.getSecond();
            }

            int x1 = Math.min(top.getBlockX(), bottom.getBlockX());
            int y1 = Math.min(top.getBlockY(), bottom.getBlockY());
            int z1 = Math.min(top.getBlockZ(), bottom.getBlockZ());

            int x2 = Math.max(top.getBlockX(), bottom.getBlockX());
            int y2 = Math.max(top.getBlockY(), bottom.getBlockY());
            int z2 = Math.max(top.getBlockZ(), bottom.getBlockZ());

            int x3 = location.getBlockX();
            int y3 = location.getBlockY();
            int z3 = location.getBlockZ();

            if (x3 >= x1 && x3 <= x2 && y3 >= y1 && y3 <= y2 && z3 >= z1 && z3 <= z2) return Optional.of(meta);
        }
        return Optional.empty();
    }
}
