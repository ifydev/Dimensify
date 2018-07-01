package me.ifydev.dimensifyspigot.world;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.util.ColorUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class WorldController {

    public Optional<World> getWorld(String name) {
        return getWorld(name, false);
    }

    public Optional<World> getWorld(String name, boolean andMake) {
        DimensifyMain plugin = DimensifyMain.get();

        World world = Bukkit.getWorld(name);

        if (world == null && (plugin.getApi().getDatabaseHandler().map(db -> db.getDimension(name).isPresent()).orElse(false)) || andMake) {
            this.loadWorld(new DimensifyWorld(name, DimensifyMain.get()));
            world = Bukkit.getWorld(name);
        }
        return Optional.ofNullable(world);
    }

    public void loadWorld(DimensifyWorld creator) {
        World world;
        try {
            world = creator.createWorld();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (world == null) {
            System.out.println("World was null after generation.");
            return;
        }
        DimensifyMain plugin = DimensifyMain.get();

        plugin.getApi().getDatabaseHandler().ifPresent(db ->
                db.createDimension(new Dimension(creator.name(), creator.type().getName(), creator.getMeta(), creator.isDefault())));
    }

    public boolean deleteWorld(String dimensionName) {
        World world = Bukkit.getWorld(dimensionName);
        if (world == null) return false;
        // Teleport all the players from the deleted world, to the main world.
        DimensifyMain plugin = DimensifyMain.get();
        Optional<World> defaultDimension = plugin.getWorldController().getWorld(plugin.getApi().getDatabaseHandler()
                .map(db -> db.getDefaultDimension(false)).orElse(Bukkit.getWorlds().get(0).getName()));
        if (!defaultDimension.isPresent()) {
            plugin.getLogger().severe("Could not find a suitable dimension to send players to?!");
            return false;
        }
        world.getPlayers().forEach(player -> player.teleport(defaultDimension.get().getSpawnLocation()));

        File folder = world.getWorldFolder();
        Bukkit.unloadWorld(world, false);

        // Remove from database
        plugin.getApi().getDatabaseHandler().ifPresent(db ->
                db.removeDimension(dimensionName));

        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void enterDimension(Player player, World dimension) {
        if (dimension == null) return;

        DimensifyMain plugin = DimensifyMain.get();

        if (plugin.isPermissionRestrictDimensions()) {
            boolean playerIsAllowedToEnterDimension = player.hasPermission("dimension." + dimension + ".allow") || plugin.isAllowEntryByDefault();
            if (!playerIsAllowedToEnterDimension) {
                player.sendMessage(ColorUtil.makeReadable(DimensifyConstants.CANNOT_ENTER_THIS_DIMENSION));
                return;
            }
        }

        player.teleport(dimension.getSpawnLocation());
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorUtil.makeReadable(DimensifyConstants.WHOOSH)));
    }
}
