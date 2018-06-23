package me.ifydev.dimensifyspigot.commands;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensify.api.meta.MetaParser;
import me.ifydev.dimensify.api.meta.WorldMeta;
import me.ifydev.dimensify.api.util.ArgumentUtil;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import me.ifydev.dimensifyspigot.world.DimensifyWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class DimensifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Optional<DimensifyMain> plugin = DimensifyMain.get();
        if (!plugin.isPresent()) {
            sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.PLUGIN_NOT_PRESENT));
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin.get(), () -> {
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
                DimensifyWorld creator = new DimensifyWorld(worldName, plugin.get());
                MetaParser.MetaResult result = MetaParser.parse(metas);
                if (result.getError().isPresent()) {
                    sender.sendMessage(ColorUtil.makeReadable(result.getError().get()));
                    return;
                }

                if (!result.getMeta().isPresent()) {
                    // if our meta isn't present, AND there wasn't an error to provide, then something's really
                    // messed up.
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INTERNAL_ERROR));
                    plugin.get().getLogger().severe("Meta was not present after parsing meta information.");
                    return;
                }
                WorldMeta meta = result.getMeta().get();

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

                Bukkit.getScheduler().runTask(plugin.get(), () -> plugin.get().getWorldController().loadWorld(creator, plugin.get()));
                plugin.get().getLogger().info("Finished generating world '" + worldName + "'!");
                sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.WORLD_CREATED.replace("<WORLD>", worldName)));
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

                String worldName = args[1];
                if (Bukkit.getWorld(worldName) == null && !plugin.get().getAllWorlds().contains(worldName)) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_WORLD.replace("<WORLD>", worldName)));
                    return;
                }

                Bukkit.getScheduler().runTask(plugin.get(), () -> {
                    World world = Bukkit.getWorld(worldName);
                    ((Player) sender).teleport(world.getSpawnLocation());
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.WHOOSH));
                });
                return;
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_DELETE));
                    return;
                }
                String worldName = args[1];
                // Make sure the world exists
                Bukkit.getScheduler().runTask(plugin.get(), () -> {
                    if (Bukkit.getWorld(worldName) == null && plugin.get().getAllWorlds().contains(worldName)) {
                        // Load the world
                        plugin.get().getWorldController().loadWorld(new DimensifyWorld(worldName, plugin.get()), plugin.get());
                    } else if (Bukkit.getWorld(worldName) == null) {
                        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_WORLD.replace("<WORLD>", worldName)));
                        return;
                    }
                    // World exists, delete it
                    boolean deleted = plugin.get().getWorldController().deleteWorld(worldName);
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

                if (Bukkit.getWorld(args[2]) == null && !plugin.get().getAllWorlds().contains(args[2])) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_WORLD.replace("<WORLD>", args[2])));
                    return;
                }

                Bukkit.getScheduler().runTask(plugin.get(), () -> {
                    if (!plugin.get().getWorldNames().contains(args[2])) {
                        // Load the world, since it's not here
                        plugin.get().getWorldController().loadWorld(new DimensifyWorld(args[2], plugin.get()), plugin.get());
                    }

                    World world = Bukkit.getWorld(args[2]);
                    player.teleport(world.getSpawnLocation());

                    player.sendMessage(ColorUtil.makeReadable(DimensifyConstants.YOU_HAVE_BEEN_SENT.replace("<WORLD>", args[2])));
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.PLAYER_HAS_BEEN_SENT.replace("<PLAYER>", args[1]).replace("<WORLD>", args[2])));
                });
                return;
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
