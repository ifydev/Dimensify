package me.ifydev.dimensifyspigot.commands;

import me.ifydev.dimensify.api.DimensifyConstants;
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

import java.util.List;
import java.util.Optional;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class DimensifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        DimensifyMain plugin = DimensifyMain.get();

        if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_BASE)) {
            sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (args.length < 1) {
                sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.DIMENSIFY_HELP_HEADER));
                DimensifyConstants.HELP_RESPONSE.forEach(messages ->
                        messages.forEach(message -> sender.sendMessage(ColorUtil.makeReadable(message))));
                sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.DIMENSIFY_HELP_FOOTER));
                return;
            }
            if (args[0].equalsIgnoreCase("create")) {
                if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_CREATE_DIMENSION)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                    return;
                }
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
                if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_GO)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                    return;
                }

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
                if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_REMOVE_DIMENSION)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                    return;
                }

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
                if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_SEND)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                    return;
                }

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
                if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_PORTAL)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                    return;
                }

                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_PORTAL));
                    return;
                }
                if (args[1].equalsIgnoreCase("create")) {
                    if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_ADD_PORTAL)) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                        return;
                    }

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
                    if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_REMOVE_PORTAL)) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                        return;
                    }

                    if(args.length < 3) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_PORTAL_DELETE));
                        return;
                    }

                    String response = ColorUtil.makeReadable(PortalHandler.deletePortal(args[2]));
                    sender.sendMessage(response);
                    return;
                } else if (args[1].equalsIgnoreCase("link")) {
                    if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_LINK)) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                        return;
                    }

                    if (args.length < 4) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_PORTAL_LINK));
                        return;
                    }
                    String portal = args[2];
                    String destination = args[3];

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String response = ColorUtil.makeReadable(PortalHandler.linkPortal(portal, destination));
                        sender.sendMessage(response);
                    });

                    return;
                } else if (args[1].equalsIgnoreCase("list")) {
                    if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_LIST_PORTALS)) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                        return;
                    }

                    // List the current portals
                    List<String> response = PortalHandler.listPortals();
                    if (response.size() == 0) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.THERE_ARE_NONE.replace("<ITEM>", "portals")));
                        return;
                    }
                    response.stream().map(ColorUtil::makeReadable).forEach(sender::sendMessage);
                    return;
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_LIST_DIMENSIONS)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                    return;
                }

                // List the current worlds.
                List<String> response = BasicHandler.listWorlds();
                if (response.size() == 0) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.THERE_ARE_NONE.replace("<ITEM>", "worlds")));
                    return;
                }
                response.stream().map(ColorUtil::makeReadable).forEach(sender::sendMessage);
                return;
            } else if (args[0].equalsIgnoreCase("default")) {
                if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_DEFAULT)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                    return;
                }

                String response = BasicHandler.setOrGetDefaultWorld(args.length >= 2 ? Optional.of(args[1]) :Optional.empty());
                response = ColorUtil.makeReadable(response);
                sender.sendMessage(response);
                return;
            } else if (args[0].equalsIgnoreCase("cache")) {
                if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_CACHE)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                    return;
                }

                boolean purge = args.length >= 2 && args[1].equalsIgnoreCase("purge");

                String result = purge ? BasicHandler.purgeCache() : BasicHandler.cacheStatus();
                result = ColorUtil.makeReadable(result);

                sender.sendMessage(result);
                return;
            } else if (args[0].equalsIgnoreCase("unload")) {
                if (!sender.hasPermission(DimensifyConstants.DIMENSIFY_UNLOAD)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_DONT_HAVE_PERMISSION));
                    return;
                }

                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_UNLOAD_DIMENSION));
                    return;
                }
                String dimension = args[1];
                boolean save = !(args.length >= 3 && args[2].equalsIgnoreCase("false"));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String result = ColorUtil.makeReadable(BasicHandler.unloadDimension(dimension, save));
                    sender.sendMessage(result);
                });
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
