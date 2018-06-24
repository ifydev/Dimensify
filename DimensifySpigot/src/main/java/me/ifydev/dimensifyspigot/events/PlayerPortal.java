package me.ifydev.dimensifyspigot.events;

import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.world.DimensifyWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.Optional;

/**
 * @author Innectic
 * @since 06/22/2018
 */
public class PlayerPortal implements Listener {

    @EventHandler
    public void onPlayerAboutToTeleportEvent(PlayerPortalEvent e) {
        // We cancel this event if they're within one of our portals, so that we can send them ourselves.
        Optional<DimensifyMain> plugin = DimensifyMain.get();
        if (!plugin.isPresent()) return;

        plugin.get().getPortalRegistry().findCornersFromPosition(e.getPlayer().getLocation()).ifPresent(meta -> {
            Player player = e.getPlayer();
            e.setCancelled(true);

            plugin.get().getPortalRegistry().findCornersFromPosition(player.getLocation()).ifPresent(corners -> {
                if (!corners.getLink().isPresent()) return;

                String link = corners.getLink().get();
                if (!plugin.get().getWorldNames().contains(link)) {
                    // Load the world, since it's not here
                    plugin.get().getWorldController().loadWorld(new DimensifyWorld(link, plugin.get()), plugin.get());
                }

                World world = Bukkit.getWorld(link);
                player.teleport(world.getSpawnLocation());
            });
        });
    }
}
