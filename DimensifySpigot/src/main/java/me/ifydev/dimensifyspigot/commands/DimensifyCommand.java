package me.ifydev.dimensifyspigot.commands;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensify.api.meta.MetaParser;
import me.ifydev.dimensify.api.meta.WorldMeta;
import me.ifydev.dimensify.api.portal.PortalType;
import me.ifydev.dimensify.api.util.ArgumentUtil;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.portal.PortalCorners;
import me.ifydev.dimensifyspigot.portal.algo.PortalCornerDetection;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import me.ifydev.dimensifyspigot.util.MiscUtil;
import me.ifydev.dimensifyspigot.world.DimensifyWorld;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
                WorldType type = WorldType.getByName(dimensionType);
                if (type == null) {
                    // Invalid dimension type
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_DIMENSION_TYPE.replace("<TYPE>", dimensionType)));
                    return;
                }

                String worldName = args[2];
                // Remaining arguments are referred to as meta.
                String[] metas = ArgumentUtil.getRemainingArgs(2, args);

                // Ensure a world with that name doesn't exist already
                if (Bukkit.getWorld(worldName) != null) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.WORLD_EXISTS.replace("<WORLD>", worldName)));
                    return;
                }
                sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.CREATING_WORLD.replace("<WORLD>", worldName)));
                MetaParser.MetaResult result = MetaParser.parse(metas);
                if (result.getError().isPresent()) {
                    sender.sendMessage(ColorUtil.makeReadable(result.getError().get()));
                    return;
                }

                if (!result.getMeta().isPresent()) {
                    // if our meta isn't present, AND there wasn't an error to provide, then something's really
                    // messed up.
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INTERNAL_ERROR));
                    plugin.getLogger().severe("Meta was not present after parsing meta information.");
                    return;
                }
                WorldMeta meta = result.getMeta().get();
                DimensifyWorld creator = new DimensifyWorld(worldName, plugin);
                creator.setDefault(false);
                creator.setMeta(Optional.of(MiscUtil.joinStrings(' ', Arrays.asList(metas))));

                creator.type(type);
                creator.generateStructures(meta.isStructures());

                meta.getEnvironment().ifPresent(env -> {
                    World.Environment environment;
                    try {
                        environment = World.Environment.valueOf(env.toUpperCase());
                        creator.environment(environment);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(DimensifyConstants.INVALID_ENVIRONMENT_TYPE.replace("<TYPE>", env));
                        return;
                    }
                });

                meta.getSeed().ifPresent(seed -> creator.seed(Long.valueOf(seed)));

                Bukkit.getScheduler().runTask(plugin, () -> plugin.getWorldController().loadWorld(creator));
                plugin.getLogger().info("Finished generating world '" + worldName + "'!");
                sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.WORLD_CREATED.replace("<WORLD>", worldName)));
                return;
            } else if (args[0].equalsIgnoreCase("go")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_ARENT_A_PLAYER));
                    return;
                }
                Player player = (Player) sender;

                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_GO));
                    return;
                }

                String worldName = args[1];
                if (Bukkit.getWorld(worldName) == null && !dimensionNames.contains(worldName)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_WORLD.replace("<WORLD>", worldName)));
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    World world = Bukkit.getWorld(worldName);

                    player.teleport(world.getSpawnLocation());
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorUtil.makeReadable(DimensifyConstants.WHOOSH)));
                });
                return;
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_DELETE));
                    return;
                }
                String worldName = args[1];
                if (Bukkit.getWorlds().get(0).getName().equals(worldName)) {
                    // You can't delete the main world.
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.CANNOT_DELETE_MAIN_WORLD));
                    return;
                }
                // Make sure the world exists
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (Bukkit.getWorld(worldName) == null && dimensionNames.contains(worldName)) {
                        // Load the world
                        plugin.getWorldController().loadWorld(new DimensifyWorld(worldName, plugin));
                    } else if (Bukkit.getWorld(worldName) == null) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_WORLD.replace("<WORLD>", worldName)));
                        return;
                    }
                    // World exists, delete it
                    boolean deleted = plugin.getWorldController().deleteWorld(worldName);
                    String response = deleted ? DimensifyConstants.WORLD_DELETED : DimensifyConstants.INVALID_WORLD;
                    response = response.replace("<WORLD>", worldName);
                    sender.sendMessage(ColorUtil.makeReadable(response));
                });

                return;
            } else if (args[0].equalsIgnoreCase("send")) {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_SEND_PLAYER));
                    return;
                }

                Player player = Bukkit.getPlayer(args[1]);
                if (player == null || !player.isOnline()) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_PLAYER.replace("<PLAYER>", args[1])));
                    return;
                }

                if (Bukkit.getWorld(args[2]) == null && !dimensionNames.contains(args[2])) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_WORLD.replace("<WORLD>", args[2])));
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!dimensionNames.contains(args[2])) {
                        // Load the world, since it's not here
                        plugin.getWorldController().loadWorld(new DimensifyWorld(args[2], plugin));
                    }

                    World world = Bukkit.getWorld(args[2]);
                    player.teleport(world.getSpawnLocation());

                    player.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_HAVE_BEEN_SENT.replace("<WORLD>", args[2])));
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.PLAYER_HAS_BEEN_SENT.replace("<PLAYER>", args[1]).replace("<WORLD>", args[2])));
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
                    if (block == null || block.getType() != Material.PORTAL) {
                        player.sendMessage(ColorUtil.makeReadable(DimensifyConstants.MUST_LOOK_AT_PORTAL_BLOCKS));
                        return;
                    }

                    // We're just going to assume the user game us a corner of the portal.
                    Optional<PortalCorners> corners = PortalCornerDetection.findPortalCornersFromAnyCorner(player, block.getLocation());
                    if (!corners.isPresent()) {
                        player.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_PORTAL));
                        return;
                    }
                    if (plugin.getPortalRegistry().isPortalNameUsed(portalName)) {
                        player.sendMessage(ColorUtil.makeReadable(DimensifyConstants.PORTAL_NAME_ALREADY_USED.replace("<NAME>", portalName)));
                        return;
                    }
                    plugin.getPortalRegistry().setPortal(portalName, PortalType.NETHER, corners.get());
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.PORTAL_CREATED.replace("<PORTAL>", portalName)));
                    return;
                } else if (args[1].equalsIgnoreCase("delete")) {
                    if(args.length < 3) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_PORTAL_DELETE));
                        return;
                    }

                    String portal = args[2];
                    if (!plugin.getPortalRegistry().isPortalNameUsed(portal)) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.PORTAL_DOES_NOT_EXIST.replace("<PORTAL>", portal)));
                        return;
                    }
                    plugin.getApi().getDatabaseHandler().ifPresent(db -> db.removePortal(portal));
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.PORTAL_DELETED));
                    return;
                } else if (args[1].equalsIgnoreCase("link")) {
                    if (args.length < 4) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_PORTAL_LINK));
                        return;
                    }
                    String portalA = args[2];
                    String world = args[3];

                    // Make sure the portal exists.
                    if (!plugin.getPortalRegistry().isPortalNameUsed(portalA)) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.PORTAL_DOES_NOT_EXIST.replace("<PORTAL>", portalA)));
                        return;
                    }
                    // And make sure the world exists
                    if (!plugin.getApi().getDatabaseHandler().map(db -> db.getDimension(world).isPresent()).orElse(true)) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_WORLD.replace("<WORLD>", world)));
                        return;
                    }

                    plugin.getPortalRegistry().setPortalLink(portalA, world);
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.PORTALS_LINKED));
                    return;
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                // List the current worlds.
                // TODO
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
