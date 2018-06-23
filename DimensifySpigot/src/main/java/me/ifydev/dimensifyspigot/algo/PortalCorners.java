package me.ifydev.dimensifyspigot.algo;

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

    private Location topLeft;
    private Location topRight;
    private Location bottomLeft;
    private Location bottomRight;
}
