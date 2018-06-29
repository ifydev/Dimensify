package me.ifydev.dimensifyspigot.portal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

/**
 * @author Innectic
 * @since 06/22/2018
 */
@AllArgsConstructor
@Getter
public class PortalCorners {
    private Location first;
    private Location second;

    @Override
    public String toString() {
        return String.format("X1: %d, Y1: %d, Z1: %d -> X2: %d, Y2: %d, Z2: %d",
                first.getBlockX(), first.getBlockY(), first.getBlockZ(),
                second.getBlockX(), second.getBlockY(), second.getBlockZ());
    }
}
