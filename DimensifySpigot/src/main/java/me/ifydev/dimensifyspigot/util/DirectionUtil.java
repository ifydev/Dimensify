package me.ifydev.dimensifyspigot.util;

import org.bukkit.entity.Player;

/**
 * @author Innectic
 * @since 06/22/2018
 */
public class DirectionUtil {

    public enum CardinalDirection {
        SOUTH, WEST, NORTH, EAST
    }

    public static CardinalDirection getDirection(Player player) {
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) yaw += 360;

        if (yaw >= 315 || yaw < 45) return CardinalDirection.SOUTH;
        else if (yaw < 135) return CardinalDirection.WEST;
        else if (yaw < 225) return CardinalDirection.NORTH;
        return CardinalDirection.EAST;
    }
}
