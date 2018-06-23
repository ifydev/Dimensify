package me.ifydev.dimensifyspigot.algo;

import me.ifydev.dimensifyspigot.util.DirectionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
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
        DirectionUtil.CardinalDirection direction = DirectionUtil.getDirection(player);

        boolean facingNorthSouth = direction == DirectionUtil.CardinalDirection.NORTH || direction == DirectionUtil.CardinalDirection.SOUTH;

        Location topLeft, topRight, bottomLeft, bottomRight;

        boolean portalDown = starting.getBlock().getRelative(BlockFace.DOWN).getType() == Material.PORTAL;
        boolean portalUp = starting.getBlock().getRelative(BlockFace.UP).getType() == Material.PORTAL;
        boolean portalLeft = starting.getBlock().getRelative(facingNorthSouth ? BlockFace.WEST : BlockFace.SOUTH).getType() == Material.PORTAL;
        boolean portalRight = starting.getBlock().getRelative(facingNorthSouth ? BlockFace.EAST : BlockFace.NORTH).getType() == Material.PORTAL;

        if (portalDown && portalLeft) {
            System.out.println("TR");
            topRight = starting;
        } else if (portalDown && portalRight) {
            System.out.println("TL");
            topLeft = starting;
        } else if (portalUp && portalLeft) {
            System.out.println("BR");
            bottomRight = starting;
        } else if (portalUp && portalRight) {
            System.out.println("BL");
            bottomLeft = starting;
        }

        return Optional.empty();
    }
}
