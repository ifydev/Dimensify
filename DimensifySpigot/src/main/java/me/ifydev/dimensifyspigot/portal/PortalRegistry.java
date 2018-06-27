package me.ifydev.dimensifyspigot.portal;

import me.ifydev.dimensify.api.portal.PortalType;
import me.ifydev.dimensifyspigot.DimensifyMain;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Innectic
 * @since 06/22/2018
 */
public class PortalRegistry {

    private Map<String, SpigotPortalMeta> portalCorners = new HashMap<>();

    public void setPortalLink(String portal, String world) {
        portalCorners.get(portal).setDestination(Optional.of(world));

        Optional<DimensifyMain> plugin = DimensifyMain.get();
        if (!plugin.isPresent()) return;

        plugin.get().getConfig().set("link." + portal, world);
        plugin.get().saveConfig();
    }

    public void removePortalLink(String portal) {
//         if (portalLinkExists(link)) portalLinks.remove(link);

        Optional<DimensifyMain> plugin = DimensifyMain.get();
        if (!plugin.isPresent()) return;

        plugin.get().getConfig().set("link." + portal, null);
        plugin.get().saveConfig();
    }

    public void setPortal(String name, PortalType type, PortalCorners corners) {
        portalCorners.put(name, new SpigotPortalMeta(corners, type, Optional.empty()));

        Optional<DimensifyMain> plugin = DimensifyMain.get();
        if (!plugin.isPresent()) return;

        plugin.get().getConfig().set("portal." + name + ".x1", corners.getFirst().getBlockX());
        plugin.get().getConfig().set("portal." + name + ".y1", corners.getFirst().getBlockY());
        plugin.get().getConfig().set("portal." + name + ".z1", corners.getFirst().getBlockZ());
        plugin.get().getConfig().set("portal." + name + ".world", corners.getFirst().getWorld().getName());
        plugin.get().getConfig().set("portal." + name + ".type", type.toString());

        plugin.get().getConfig().set("portal." + name + ".x2", corners.getSecond().getBlockX());
        plugin.get().getConfig().set("portal." + name + ".y2", corners.getSecond().getBlockY());
        plugin.get().getConfig().set("portal." + name + ".z2", corners.getSecond().getBlockZ());

        plugin.get().saveConfig();
    }

    public void removePortal(String name) {
        portalCorners.remove(name);

        Optional<DimensifyMain> plugin = DimensifyMain.get();
        if (!plugin.isPresent()) return;

        plugin.get().getConfig().set("portal." + name, null);
        plugin.get().saveConfig();
    }

    public void load() {
        Optional<DimensifyMain> plugin = DimensifyMain.get();
        if (!plugin.isPresent()) return;

        if (plugin.get().getConfig().getConfigurationSection("portal") == null) return;
        Set<String> keys = plugin.get().getConfig().getConfigurationSection("portal").getKeys(false);

        keys.forEach(key -> {
            int x1 = plugin.get().getConfig().getInt("portal." + key + ".x1");
            int y1 = plugin.get().getConfig().getInt("portal." + key + ".y1");
            int z1 = plugin.get().getConfig().getInt("portal." + key + ".z1");

            int x2 = plugin.get().getConfig().getInt("portal." + key + ".x2");
            int y2 = plugin.get().getConfig().getInt("portal." + key + ".y2");
            int z2 = plugin.get().getConfig().getInt("portal." + key + ".z2");

            String world = plugin.get().getConfig().getString("portal." + key + ".world");
            String type = plugin.get().getConfig().getString("portal." + key + ".type");
            if (world == null || type == null) return;

            System.out.println(key);
            portalCorners.put(key, new SpigotPortalMeta(new PortalCorners(
                    new Location(Bukkit.getWorld(world), x1, y1, z1),
                    new Location(Bukkit.getWorld(world), x2, y2, z2)
            ), PortalType.findType(type), Optional.ofNullable(plugin.get().getConfig().getString("link." + key, null))));
        });
    }

    public boolean isPortalNameUsed(String name) {
        return portalCorners.containsKey(name);
    }

    public Optional<SpigotPortalMeta> getPortal(String name) {
        return Optional.ofNullable(portalCorners.getOrDefault(name, null));
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
