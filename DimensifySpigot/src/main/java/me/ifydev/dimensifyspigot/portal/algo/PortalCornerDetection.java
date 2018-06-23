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

        boolean portalDown = starting.getBlock().getRelative(BlockFace.DOWN).getType() == Material.PORTAL;
        boolean portalUp = starting.getBlock().getRelative(BlockFace.UP).getType() == Material.PORTAL;
        boolean portalLeft = starting.getBlock().getRelative(facingNorthSouth ? BlockFace.WEST : BlockFace.SOUTH).getType() == Material.PORTAL;
        boolean portalRight = starting.getBlock().getRelative(facingNorthSouth ? BlockFace.EAST : BlockFace.NORTH).getType() == Material.PORTAL;

        boolean isBottom = true;
        boolean isLeft = true;

        if (portalDown && portalLeft) {
            isBottom = false;
            isLeft = false;
        } else if (portalDown && portalRight) isBottom = false;
        else if   (portalUp && portalLeft)    isLeft = false;

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
            if (next.getType() != Material.PORTAL) break;

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

            if (next.getType() != Material.PORTAL) break;
            current = next;
            distanceMoved++;
        } while (true);
        if (current == null) return Optional.empty();

        return Optional.of(new PortalCorners(starting, current.getLocation()));
    }
}
