package me.ifydev.dimensifyspigot.commands;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensify.api.util.ArgumentUtil;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
            if (args[0].equalsIgnoreCase("create")) {
                // Make sure there's enough arguments here
                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_CREATE_WORLD));
                    return;
                }

                String dimensionType = args[1];
                String worldName = String.join("_", ArgumentUtil.getRemainingArgs(2, args));

                // Ensure a world with that name doesn't exist already
                if (Bukkit.getWorld(worldName) != null) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.WORLD_EXISTS.replace("<WORLD>", worldName)));
                    return;
                }
                sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.CREATING_WORLD));
                WorldCreator creator = new WorldCreator(worldName);
                doWorldCreateSync(plugin.get(), creator, (Player) sender);
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
                if (Bukkit.getWorld(worldName) == null && plugin.get().getWorldNames().contains(worldName)) {
                    // Load the world
                    doWorldCreateSync(plugin.get(), new WorldCreator(worldName), null);
                } else if (Bukkit.getWorld(worldName) == null) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_WORLD.replace("<WORLD>", worldName)));
                    return;
                }

                Bukkit.getScheduler().runTask(plugin.get(), () -> {
                    World world = Bukkit.getWorld(worldName);
                    ((Player) sender).teleport(world.getSpawnLocation());
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.WHOOSH));
                });
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.NOT_ENOUGH_ARGUMENTS_DELETE));
                    return;
                }

                String worldName = String.join("_", ArgumentUtil.getRemainingArgs(2, args));
                // Make sure the world exists
                if (Bukkit.getWorld(worldName) == null) {
                    sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_WORLD));
                    return;
                }

                // World exists, delete it
                World world = Bukkit.getWorld(worldName);
                deleteWorldSync(plugin.get(), world, sender);
                return;
            }
            // TODO: send help to the player here
        });

        return false;
    }

    private void doWorldCreateSync(DimensifyMain plugin, WorldCreator creator, Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            creator.createWorld();
            if (player != null) {
                plugin.addWorld(creator.name());
                player.sendMessage(ColorUtil.makeReadable(DimensifyConstants.WORLD_CREATED.replace("<WORLD>", creator.name())));
            }
        });
    }

    private void deleteWorldSync(Plugin plugin, World world, CommandSender sender) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.unloadWorld(world, false);
            boolean deleted = world.getWorldFolder().delete();
            if (deleted) {
                sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.WORLD_DELETED));
                return;
            }
            sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.INVALID_WORLD));
        });
    }
}
