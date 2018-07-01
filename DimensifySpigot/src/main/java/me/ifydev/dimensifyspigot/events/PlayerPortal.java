package me.ifydev.dimensifyspigot.events;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import me.ifydev.dimensifyspigot.world.WorldController;
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
        DimensifyMain plugin = DimensifyMain.get();

        Player player = e.getPlayer();
        plugin.getPortalRegistry().findCornersFromPosition(e.getFrom()).ifPresent(corners -> {
            e.setCancelled(true);

            if (!corners.getDestination().isPresent()) return;

            String link = corners.getDestination().get();
            Optional<World> dimension = DimensifyMain.get().getWorldController().getWorld(link);
            if (!dimension.isPresent()) {
                player.sendMessage(ColorUtil.makeReadable(DimensifyConstants.THIS_DIMENSION_DOES_NOT_EXIST_ANYMORE));
                return;
            }
            WorldController.enterDimension(player, dimension.get());
        });
    }
}
