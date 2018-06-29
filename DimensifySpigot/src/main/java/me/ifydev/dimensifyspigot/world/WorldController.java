package me.ifydev.dimensifyspigot.world;

import me.ifydev.dimensifyspigot.DimensifyMain;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class WorldController {

    public void loadAllWorlds(List<String> worldNames, DimensifyMain plugin) {
        worldNames.forEach(name -> loadWorld(new DimensifyWorld(name, plugin)));
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
        DimensifyMain.get().getLogger().info("Finished generating " + world.getName() + "!");
    }

    public boolean deleteWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;
        // Teleport all the players from the deleted world, to the main world.
        // TODO: We probably want to change this to whatever the default world is.
        world.getPlayers().forEach(player -> player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

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
