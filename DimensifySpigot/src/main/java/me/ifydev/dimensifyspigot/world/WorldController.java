package me.ifydev.dimensifyspigot.world;

import me.ifydev.dimensifyspigot.DimensifyMain;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class WorldController {

    public void loadAllWorlds(List<String> worldNames,  DimensifyMain plugin) {
        worldNames.forEach(name -> loadWorld(new WorldCreator(name), plugin));
    }

    public void loadWorld(WorldCreator creator, DimensifyMain plugin) {
        creator.createWorld();
        plugin.getWorldNames().add(creator.name());
    }

    public boolean deleteWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;
        File folder = world.getWorldFolder();
        Bukkit.unloadWorld(world, false);

        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
