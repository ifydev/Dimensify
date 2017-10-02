package me.ifydev.dimensifyspigot.world;

import me.ifydev.dimensifyspigot.DimensifyMain;
import org.bukkit.World;
import org.bukkit.WorldCreator;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class DimensifyWorld extends WorldCreator {
    private DimensifyMain plugin;

    public DimensifyWorld(String name, DimensifyMain plugin) {
        super(name);

        this.plugin = plugin;
    }

    @Override
    public World createWorld() {
        // Register it with the plugin
        plugin.addWorld(name());
        return super.createWorld();
    }
}
