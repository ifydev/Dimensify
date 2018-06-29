package me.ifydev.dimensifyspigot.portal;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.portal.PortalType;
import me.ifydev.dimensifyspigot.DimensifyMain;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Innectic
 * @since 06/22/2018
 */
public class PortalRegistry {

    // TODO: Use the backend handlers instead
    private Map<String, SpigotPortalMeta> portalCorners = new HashMap<>();

    public void setPortalLink(String portal, String world) {
        if (!portalCorners.containsKey(portal)) return;
        portalCorners.get(portal).setDestination(Optional.of(world));

        DimensifyMain plugin = DimensifyMain.get();
        Optional<AbstractDataHandler> handler = plugin.getApi().getDatabaseHandler();
        if (!handler.isPresent()) {
            plugin.getLogger().severe(DimensifyConstants.DATABASE_HANDLER_NOT_PRESENT);
            return;
        }
        handler.get().setPortalDestination(portal, world);
    }

    public void setPortal(String name, PortalType type, PortalCorners corners) {
        portalCorners.put(name, new SpigotPortalMeta(name, corners, type, Optional.empty()));

        DimensifyMain plugin = DimensifyMain.get();
        Optional<AbstractDataHandler> handler = plugin.getApi().getDatabaseHandler();
        if (!handler.isPresent()) {
            plugin.getLogger().severe(DimensifyConstants.DATABASE_HANDLER_NOT_PRESENT);
            return;
        }

        handler.get().createPortal(new SpigotPortalMeta(name, corners, type, Optional.empty()));
    }

    public boolean isPortalNameUsed(String name) {
        return portalCorners.containsKey(name);
    }

    public Optional<SpigotPortalMeta> findCornersFromPosition(Location location) {
        for (SpigotPortalMeta meta : portalCorners.values()) {
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
