package me.ifydev.dimensifyspigot.commands.handlers;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensify.api.meta.MetaParser;
import me.ifydev.dimensify.api.meta.WorldMeta;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import me.ifydev.dimensifyspigot.util.MiscUtil;
import me.ifydev.dimensifyspigot.world.DimensifyWorld;
import me.ifydev.dimensifyspigot.world.WorldController;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 06/28/2018
 */
public class BasicHandler {

    public static String createDimension(CommandSender sender, String dimensionType, String dimensionName, String[] remaining) {
        DimensifyMain plugin = DimensifyMain.get();
        WorldType type = WorldType.getByName(dimensionType);
        if (type == null) {
            // Invalid dimension type
            return DimensifyConstants.INVALID_DIMENSION_TYPE.replace("<TYPE>", dimensionType);
        }

        // Ensure a world with that name doesn't exist already
        if (Bukkit.getWorld(dimensionName) != null)
            return DimensifyConstants.WORLD_EXISTS.replace("<WORLD>", dimensionName);
        sender.sendMessage(ColorUtil.makeReadable(DimensifyConstants.CREATING_WORLD.replace("<WORLD>", dimensionName)));

        // Parse the meta string
        MetaParser.MetaResult result = MetaParser.parse(remaining);
        if (result.getError().isPresent())
            return ColorUtil.makeReadable(result.getError().get());

        if (!result.getMeta().isPresent()) {
            // If our meta isn't present, AND there wasn't an error to provide, then something's really
            // messed up.
            plugin.getLogger().severe("Meta was not present after parsing meta information.");
            return DimensifyConstants.INTERNAL_ERROR;
        }
        WorldMeta meta = result.getMeta().get();

        // Provide the world creator with information from the meta
        DimensifyWorld creator = new DimensifyWorld(dimensionName, plugin);
        creator.setDefault(false);
        creator.setMeta(Optional.of(MiscUtil.joinStrings(' ', Arrays.asList(remaining))));
        creator.type(type);
        creator.generateStructures(meta.isStructures());

        if (meta.getSeed().isPresent()) {
            String value = meta.getSeed().get();

            long seed;
            try {
                seed = Long.valueOf(value);
            } catch (NumberFormatException e) {
                return DimensifyConstants.INVALID_SEED;
            }
            creator.seed(seed);
        }

        if (meta.getEnvironment().isPresent()) {
            World.Environment environment;
            String env = meta.getEnvironment().get();

            try {
                environment = World.Environment.valueOf(env.toUpperCase());
                creator.environment(environment);
            } catch (IllegalArgumentException e) {
                return DimensifyConstants.INVALID_ENVIRONMENT_TYPE.replace("<TYPE>", env);
            }
            creator.environment(environment);
        }
        plugin.getWorldController().loadWorld(creator);
        return ColorUtil.makeReadable(DimensifyConstants.WORLD_CREATED.replace("<WORLD>", dimensionName));
    }

    public static String goToDimension(Player player, String dimension) {
        DimensifyMain plugin = DimensifyMain.get();
        // Make sure this dimension actually exists
        Optional<World> world = plugin.getWorldController().getWorld(dimension);
        if (!world.isPresent())
            return DimensifyConstants.INVALID_WORLD.replace("<WORLD>", dimension);
        WorldController.enterDimension(player, world.get());
        return "";
    }

    public static String deleteDimension(String dimension) {
        // Make sure we're not deleting the main world.
        if (Bukkit.getWorlds().get(0).getName().equals(dimension))
            return DimensifyConstants.CANNOT_DELETE_MAIN_WORLD;
        DimensifyMain plugin = DimensifyMain.get();

        if (!plugin.getApi().getDatabaseHandler().map(db -> db.getDimension(dimension).isPresent()).orElse(false))
            plugin.getWorldController().loadWorld(new DimensifyWorld(dimension, plugin));
        else return DimensifyConstants.INVALID_WORLD.replace("<WORLD>", dimension);

        // World exists, delete it
        boolean deleted = plugin.getWorldController().deleteWorld(dimension);
        String response = deleted ? DimensifyConstants.WORLD_DELETED : DimensifyConstants.INVALID_WORLD;
        return response.replace("<WORLD>", dimension);
    }

    public static String sendPlayerToDimension(String playerName, String dimension) {
        DimensifyMain plugin = DimensifyMain.get();

        // Make sure the player and world exist
        Optional<World> world = plugin.getWorldController().getWorld(dimension);
        if (!world.isPresent())
            return DimensifyConstants.INVALID_WORLD.replace("<WORLD>", dimension);
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return DimensifyConstants.INVALID_PLAYER.replace("<PLAYER>", playerName);

        // Send the player to the dimension
        player.teleport(world.get().getSpawnLocation());
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorUtil.makeReadable(DimensifyConstants.WHOOSH)));
        return DimensifyConstants.PLAYER_HAS_BEEN_SENT.replace("<PLAYER>", player.getName()).replace("<WORLD>", dimension);
    }

    public static List<String> listWorlds() {
        DimensifyMain plugin = DimensifyMain.get();
        Optional<AbstractDataHandler> db = plugin.getApi().getDatabaseHandler();
        if (!db.isPresent()) return Collections.singletonList(DimensifyConstants.DATABASE_HANDLER_NOT_PRESENT);

        List<Dimension> portals = db.get().getDimensions(false);
        return portals.stream().map(dim -> DimensifyConstants.DIMENSION_LIST_FORMAT
                .replace("<NAME>", dim.getName())
                .replace("<TYPE>", dim.getType())
                .replace("<DEFAULT>", String.valueOf(dim.isDefault())))
                .collect(Collectors.toList());
    }

    public static String setOrGetDefaultWorld(Optional<String> world) {
        DimensifyMain plugin = DimensifyMain.get();
        Optional<AbstractDataHandler> db = plugin.getApi().getDatabaseHandler();
        if (!db.isPresent()) return DimensifyConstants.COULD_NOT_CONNECT_TO_DATABASE;

        if (!world.isPresent()) {
            String defaultDimension = db.get().getDefaultDimension(false);
            return DimensifyConstants.DEFAULT_WORLD_FORMAT.replace("<WORLD>", defaultDimension);
        }
        boolean result = db.get().setDefaultDimension(world.get());
        return result ? DimensifyConstants.DEFAULT_WORLD_SET.replace("<WORLD>", world.get()) : DimensifyConstants.COULD_NOT_SET_DEFAULT_WORLD;
    }

    public static String unloadDimension(String dimension, boolean save) {
        boolean result = Bukkit.unloadWorld(dimension, save);
        return result ? DimensifyConstants.DIMENSION_UNLOADED.replace("<NAME>", dimension) : DimensifyConstants.COULD_NOT_UNLOAD_DIMENSION;
    }

    public static String cacheStatus() {
        DimensifyMain plugin = DimensifyMain.get();
        Optional<AbstractDataHandler> db = plugin.getApi().getDatabaseHandler();
        if (!db.isPresent()) return DimensifyConstants.DATABASE_HANDLER_NOT_PRESENT;

        int dimensions = db.get().getDimensions(false).size();
        int portals = db.get().getPortals(false).size();

        return DimensifyConstants.CACHE_FORMAT
                .replace("<DIMENSIONS>", String.valueOf(dimensions))
                .replace("<PORTALS>", String.valueOf(portals));
    }

    public static String purgeCache() {
        DimensifyMain plugin = DimensifyMain.get();
        Optional<AbstractDataHandler> db = plugin.getApi().getDatabaseHandler();
        if (!db.isPresent()) return DimensifyConstants.DATABASE_HANDLER_NOT_PRESENT;

        db.get().reload();
        return DimensifyConstants.CACHE_PURGED;
    }
}
