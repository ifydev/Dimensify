package me.ifydev.dimensifyspigot.commands;

import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.portal.PortalType;
import me.ifydev.dimensifyspigot.portal.algo.PortalCornerDetection;
import me.ifydev.dimensifyspigot.portal.PortalCorners;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author Innectic
 * @since 06/22/2018
 */
public class Test implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        Optional<PortalCorners> corners = PortalCornerDetection.findPortalCornersFromAnyCorner(player, player.getTargetBlock(null, 10).getLocation());
        if (!corners.isPresent()) {
            player.sendMessage("OOPSIE WOOPSIE THERE'S NO CORNIEWORNIES");
            return false;
        }

        DimensifyMain.get().get().getPortalRegistry().setPortal(args[0], PortalType.NETHER, corners.get());
        return true;
    }
}
