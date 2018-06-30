package me.ifydev.dimensifyspigot.events;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import me.ifydev.dimensifyspigot.world.DimensifyWorld;
import me.ifydev.dimensifyspigot.world.WorldController;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
        DimensifyMain plugin = DimensifyMain.get();

        plugin.getPortalRegistry().findCornersFromPosition(e.getPlayer().getLocation()).ifPresent(meta -> {
            Player player = e.getPlayer();
            e.setCancelled(true);

            plugin.getPortalRegistry().findCornersFromPosition(player.getLocation()).ifPresent(corners -> {
                if (!corners.getDestination().isPresent()) return;

                String link = corners.getDestination().get();
                if (!plugin.getApi().getDatabaseHandler().map(db -> db.getDimension(link).isPresent()).orElse(true)) {
                    // Load the world, since it's not here
                    plugin.getWorldController().loadWorld(new DimensifyWorld(link, plugin));
                }
                WorldController.enterDimension(player, link);
            });
        });
    }
}
