package me.ifydev.dimensifyspigot.world;

import me.ifydev.dimensify.api.DimensifyCallback;
import me.ifydev.dimensifyspigot.DimensifyMain;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class WorldController {

    public void loadAllWorlds(List<String> worldNames,  DimensifyMain plugin) {
        worldNames.forEach(name -> loadWorld(new DimensifyWorld(name, plugin), plugin, callback));
    }

    public void loadWorld(DimensifyWorld creator, DimensifyMain plugin, DimensifyCallback callback) {
        creator.createWorld();
        plugin.getWorldNames().add(creator.name());

        Map<String, String> meta = new HashMap<>();
        meta.put("world", creator.name());

        callback.success(meta);
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
