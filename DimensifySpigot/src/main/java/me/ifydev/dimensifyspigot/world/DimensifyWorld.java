package me.ifydev.dimensifyspigot.world;

import lombok.Getter;
import lombok.Setter;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensifyspigot.DimensifyMain;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.Optional;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class DimensifyWorld extends WorldCreator {
    private DimensifyMain plugin;
    @Setter @Getter private Optional<String> meta = Optional.empty();
    @Setter @Getter private boolean isDefault = false;

    public DimensifyWorld(String name, DimensifyMain plugin) {
        super(name);

        this.plugin = plugin;
    }

    @Override
    public World createWorld() {
        // Register it with the plugin
        plugin.getApi().getDatabaseHandler().ifPresent(handler ->
                handler.createDimension(new Dimension(this.name(), this.type().getName(), meta, isDefault)));
        return super.createWorld();
    }
}
