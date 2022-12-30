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
package me.ifydev.dimensifyspigot.portal.algo;

import me.ifydev.dimensifyspigot.portal.PortalCorners;
import me.ifydev.dimensifyspigot.util.DirectionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author Innectic
 * @since 06/22/2018
 */
public class PortalCornerDetection {

    public static Optional<PortalCorners> findPortalCornersFromAnyCorner(Player player, Location starting) {
        final int MAX_HEIGHT = starting.getWorld().getMaxHeight();
        final int MAX_DISTANCE = 128;

        DirectionUtil.CardinalDirection direction = DirectionUtil.getDirection(player);

        boolean facingNorthSouth = direction == DirectionUtil.CardinalDirection.NORTH || direction == DirectionUtil.CardinalDirection.SOUTH;

        boolean portalDown = starting.getBlock().getRelative(BlockFace.DOWN).getType() == Material.NETHER_PORTAL;
        boolean portalUp = starting.getBlock().getRelative(BlockFace.UP).getType() == Material.NETHER_PORTAL;
        boolean portalLeft = starting.getBlock().getRelative(facingNorthSouth ? BlockFace.WEST : BlockFace.SOUTH).getType() == Material.NETHER_PORTAL;
        boolean portalRight = starting.getBlock().getRelative(facingNorthSouth ? BlockFace.EAST : BlockFace.NORTH).getType() == Material.NETHER_PORTAL;

        boolean isBottom = true;
        boolean isLeft = true;

        if (portalDown && portalLeft) {
            isBottom = false;
            isLeft = false;
        } else if (portalDown && portalRight) isBottom = false;
        else if (portalUp && portalLeft)    isLeft = false;

        int distanceMoved = 0;
        Block current = starting.getWorld().getBlockAt(starting);
        do {
            if (distanceMoved >= MAX_DISTANCE) {
                // This portal is too big!
                current = null;
                break;
            }
            Block next;
            next = current.getRelative(isBottom ? BlockFace.UP : BlockFace.DOWN);
            if (next.getType() != Material.NETHER_PORTAL) break;

            if (next.getY() >= MAX_HEIGHT || next.getY() <= 0) {
                current = null;
                break;
            }
            current = next;
            distanceMoved++;
        } while (true);

        if (current == null) {
            // If current is null, then this portal was probably incomplete.
            return Optional.empty();
        }

        // Now we just need to find the opposing corner.
        distanceMoved = 0;
        do {
            if (distanceMoved >= MAX_DISTANCE) {
                current = null;
                break;
            }
            Block next;
            if (!isLeft) next = current.getRelative(facingNorthSouth ? BlockFace.WEST : BlockFace.SOUTH);
            else next = current.getRelative(facingNorthSouth ? BlockFace.EAST : BlockFace.NORTH);

            if (next.getType() != Material.NETHER_PORTAL) break;
            current = next;
            distanceMoved++;
        } while (true);
        if (current == null) return Optional.empty();

        return Optional.of(new PortalCorners(starting, current.getLocation()));
    }
}
