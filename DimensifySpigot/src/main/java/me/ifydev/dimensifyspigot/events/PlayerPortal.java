package me.ifydev.dimensifyspigot.events;

import me.ifydev.dimensifyspigot.DimensifyMain;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

/**
 * @author Innectic
 * @since 06/22/2018
 */
public class PlayerPortal implements Listener {

    @EventHandler
    public void onPlayerAboutToTeleportEvent(PlayerPortalEvent e) {
        // We cancel this event if they're within one of our portals, so that we can send them ourselves.

        DimensifyMain.get().get().getCornerRegistry().findCornersFromPosition(e.getPlayer().getLocation()).ifPresent(corner -> {
            Player player = e.getPlayer();
            e.setCancelled(true);
            player.sendMessage("WOAH YOU'RE WITHIN A _REAL_ PORTAL!!!!!");

            World world = Bukkit.getWorld("test");
            player.teleport(world.getSpawnLocation());
        });
    }
}
