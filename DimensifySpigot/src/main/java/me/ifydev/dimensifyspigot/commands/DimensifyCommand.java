package me.ifydev.dimensifyspigot.commands;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensify.api.util.ArgumentUtil;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.commands.handlers.BasicHandler;
import me.ifydev.dimensifyspigot.commands.handlers.PortalHandler;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class DimensifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        DimensifyMain plugin = DimensifyMain.get();
        List<Dimension> dimensions = plugin.getApi().getDatabaseHandler().map(AbstractDataHandler::getDimensions).orElse(Collections.emptyList());
        List<String> dimensionNames = dimensions.stream().map(Dimension::getName).collect(Collectors.toList());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (args.length < 1) {
                sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.DIMENSIFY_HELP_HEADER));
                DimensifyConstants.HELP_RESPONSE.forEach(messages ->
                        messages.forEach(message -> sender.sendMessage(ColorUtil.makeReadable(message))));
                sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.DIMENSIFY_HELP_FOOTER));
                return;
            }
            if (args[0].equalsIgnoreCase("create")) {
                // Make sure there's enough arguments here
                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_CREATE_WORLD));
                    return;
                }

                String dimensionType = args[1];
                String worldName = args[2];
                String[] metas = ArgumentUtil.getRemainingArgs(2, args);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    String result = BasicHandler.createDimension(sender, dimensionType, worldName, metas);
                    result = ColorUtil.makeReadable(result);
                    sender.sendMessage(result);
                });

                return;
            } else if (args[0].equalsIgnoreCase("go")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_ARENT_A_PLAYER));
                    return;
                }

                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_GO));
                    return;
                }

                Player player = (Player) sender;
                String destination = args[1];
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String result = BasicHandler.goToDimension(player, destination);
                    if (result.equals("")) return;
                    player.sendMessage(ColorUtil.makeReadable(result));
                });
                return;
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_DELETE));
                    return;
                }
                String worldName = args[1];
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String result = ColorUtil.makeReadable(BasicHandler.deleteDimension(worldName));
                    sender.sendMessage(result);
                });
                return;
            } else if (args[0].equalsIgnoreCase("send")) {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_SEND_PLAYER));
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String result = ColorUtil.makeReadable(BasicHandler.sendPlayerToDimension(args[1], args[2]));
                    sender.sendMessage(result);
                });
                return;
            } else if (args[0].equalsIgnoreCase("portal")) {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_PORTAL));
                    return;
                }
                if (args[1].equalsIgnoreCase("create")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(DimensifyConstants.YOU_ARENT_A_PLAYER);
                        return;
                    }
                    Player player = (Player) sender;

                    if (args.length < 3) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_PORTAL_CREATE));
                        return;
                    }
                    String portalName = args[2];
                    Block block = player.getTargetBlock(null, 8);

                    String response = ColorUtil.makeReadable(PortalHandler.createPortal(player, block, portalName));
                    player.sendMessage(response);
                    return;
                } else if (args[1].equalsIgnoreCase("delete")) {
                    if(args.length < 3) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_PORTAL_DELETE));
                        return;
                    }

                    String response = ColorUtil.makeReadable(PortalHandler.deletePortal(args[2]));
                    sender.sendMessage(response);
                    return;
                } else if (args[1].equalsIgnoreCase("link")) {
                    if (args.length < 4) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_PORTAL_LINK));
                        return;
                    }
                    String portal = args[2];
                    String destination = args[3];

                    String response = ColorUtil.makeReadable(PortalHandler.linkPortal(portal, destination));
                    sender.sendMessage(response);

                    return;
                } else if (args[1].equalsIgnoreCase("list")) {
                    // List the current portals
                    PortalHandler.listPortals().stream().map(ColorUtil::makeReadable).forEach(sender::sendMessage);
                    return;
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                // List the current worlds.
                BasicHandler.listWorlds().stream().map(ColorUtil::makeReadable).forEach(sender::sendMessage);
                return;
            }
            // Send help
            sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.DIMENSIFY_HELP_HEADER));
            DimensifyConstants.HELP_RESPONSE.forEach(section -> section.stream().map(ColorUtil::makeReadable).forEach(sender::sendMessage));
            sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.DIMENSIFY_HELP_FOOTER));
        });

        return false;
    }
}
