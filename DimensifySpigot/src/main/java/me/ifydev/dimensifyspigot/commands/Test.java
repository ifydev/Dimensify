package me.ifydev.dimensifyspigot.commands;

import me.ifydev.dimensifyspigot.algo.PortalCornerDetection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Innectic
 * @since 06/22/2018
 */
public class Test implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        PortalCornerDetection.findPortalCornersFromAnyCorner(player, player.getTargetBlock(null, 10).getLocation());
        return true;
    }
}
