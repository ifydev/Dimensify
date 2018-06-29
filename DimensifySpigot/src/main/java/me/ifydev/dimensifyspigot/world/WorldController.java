package me.ifydev.dimensifyspigot.world;

import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensifyspigot.DimensifyMain;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class WorldController {

    public World getWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            this.loadWorld(new DimensifyWorld(name, DimensifyMain.get()));
            world = Bukkit.getWorld(name);
        }
        return world;
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
        // TODO: We probably want to change this to whatever the default world is.
        world.getPlayers().forEach(player -> player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

        File folder = world.getWorldFolder();
        Bukkit.unloadWorld(world, false);

        // Remove from database
        DimensifyMain plugin = DimensifyMain.get();
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
}
