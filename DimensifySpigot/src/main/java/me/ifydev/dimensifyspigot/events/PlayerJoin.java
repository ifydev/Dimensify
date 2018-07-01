package me.ifydev.dimensifyspigot.events;

import me.ifydev.dimensifyspigot.DimensifyMain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (player == null) return;

        DimensifyMain plugin = DimensifyMain.get();
        if (plugin.isSendPlayersToDefaultWorldOnLogin()) {
            plugin.getApi().getDatabaseHandler().ifPresent(db -> {
                String dimension = db.getDefaultDimension(false);
                plugin.getWorldController().getWorld(dimension).ifPresent(dim -> player.teleport(dim.getSpawnLocation()));
            });
        }
    }
}
