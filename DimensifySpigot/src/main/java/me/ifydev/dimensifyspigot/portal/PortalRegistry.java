package me.ifydev.dimensifyspigot.portal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 06/22/2018
 */
public class PortalRegistry {

    @Getter
    @AllArgsConstructor
    private class PortalMeta {
        private PortalType type;
        private PortalCorners corners;
    }

    private Map<String, PortalMeta> portalCorners = new HashMap<>();

    public void setPortal(String name, PortalType type, PortalCorners corners) {
        portalCorners.put(name, new PortalMeta(type, corners));
    }

    public void removePortal(String name) {
        portalCorners.remove(name);
    }

    public boolean isPortalNameUsed(String name) {
        return portalCorners.containsKey(name);
    }

    public Optional<PortalCorners> findCornersFromPosition(Location location) {
        for (PortalCorners corner : portalCorners.values().stream().map(PortalMeta::getCorners).collect(Collectors.toList())) {
            Location bottom;
            Location top;

            // Find which location is the top / bottom
            if (corner.getFirst().getY() < corner.getSecond().getY()) {
                top = corner.getSecond();
                bottom = corner.getFirst();
            } else {
                top = corner.getFirst();
                bottom = corner.getSecond();
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

            if (x3 >= x1 && x3 <= x2 && y3 >= y1 && y3 <= y2 && z3 >= z1 && z3 <= z2) return Optional.of(corner);
        }
        return Optional.empty();
    }
}
