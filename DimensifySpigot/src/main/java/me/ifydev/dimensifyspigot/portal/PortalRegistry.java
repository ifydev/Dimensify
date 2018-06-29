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

        List<PortalMeta> portalCorners = plugin.getApi().getDatabaseHandler().get().getPortals();
        for (PortalMeta regularMeta : portalCorners) {
            SpigotPortalMeta meta = (SpigotPortalMeta) regularMeta;

            Optional<PortalCorners> cornersOptional = meta.getCorners();
            if (!cornersOptional.isPresent()) continue;
            PortalCorners corner = cornersOptional.get();

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

            if (x3 >= x1 && x3 <= x2 && y3 >= y1 && y3 <= y2 && z3 >= z1 && z3 <= z2) return Optional.of(meta);
        }
        return Optional.empty();
    }
}
