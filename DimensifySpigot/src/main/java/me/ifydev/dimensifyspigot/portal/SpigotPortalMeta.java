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
        super(name, corners.getFirst().getBlockX(), corners.getFirst().getBlockY(), corners.getFirst().getBlockZ(),
                corners.getSecond().getBlockX(), corners.getSecond().getBlockY(), corners.getSecond().getBlockZ(),
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
