package me.ifydev.dimensifyspigot.backend;

import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.backend.ConnectionInformation;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensify.api.portal.PortalMeta;
import me.ifydev.dimensify.api.portal.PortalType;
import me.ifydev.dimensifyspigot.DimensifyMain;
import me.ifydev.dimensifyspigot.portal.SpigotPortalMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * @author Innectic
 * @since 06/27/2018
 */
public class SpigotFlatFileHandler extends AbstractDataHandler {

    private File storageFile;
    private FileConfiguration storage;

    public SpigotFlatFileHandler(ConnectionInformation connectionInformation) {
        super(connectionInformation);
    }

    private void saveStorage() {
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            DimensifyMain.get().ifPresent(plugin -> plugin.getLogger().severe("Could not save storage config!"));
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        Optional<DimensifyMain> plugin = DimensifyMain.get();
        if (!plugin.isPresent()) return;
        File data = plugin.get().getDataFolder();

        // Ensure the files exist
        storageFile = new File(data, "storage.yml");
        if (!storageFile.exists()) {
            storageFile.getParentFile().mkdirs();
            plugin.get().saveResource("storage.yml", false);
        }
        storage = new YamlConfiguration();

        try {
            storage.load(storageFile);
        } catch (InvalidConfigurationException | IOException e) {
            plugin.get().getLogger().severe("Could not create storage.yml");
            e.printStackTrace();
        }
    }

    @Override
    public boolean connect() {
        return true;
    }

    @Override
    public void reload() {
        this.drop();

        loadPortals();
        loadDimensions();
    }

    @Override
    public void drop() {
        dimensions = new ArrayList<>();
        portals = new ArrayList<>();
    }

    @Override
    public void createPortal(PortalMeta meta) {
        // Make sure this does not exist in the cache already
        Optional<PortalMeta> portal = this.portals.stream().filter(m -> m.getName().equals(meta.getName())).findFirst();
        if (portal.isPresent()) return;

        storage.set("portal." + meta.getName() + "x1", meta.getX1());
        storage.set("portal." + meta.getName() + "y1", meta.getY1());
        storage.set("portal." + meta.getName() + "z1", meta.getZ1());

        storage.set("portal." + meta.getName() + "x1", meta.getX2());
        storage.set("portal." + meta.getName() + "y1", meta.getY2());
        storage.set("portal." + meta.getName() + "z1", meta.getZ2());

        storage.set("portal." + meta.getName() + ".world", meta.getWorld());
        storage.set("portal." + meta.getName() + ".type", meta.getType().name());

        if (meta.getDestination().isPresent()) {
            String destination = meta.getDestination().get();
            storage.set("link." + meta.getName(), destination);
        }

        saveStorage();
    }

    @Override
    public void removePortal(String name) {
        // Make sure the portal exists in the cache.
        Optional<PortalMeta> meta = this.portals.stream().filter(m -> m.getName().equals(name)).findFirst();
        if (!meta.isPresent()) return;

        this.portals.remove(meta.get());

        storage.set("portal." + name, null);
        storage.set("link." + name, null);

        saveStorage();
    }

    @Override
    public void loadPortals() {
        ConfigurationSection portalSection = storage.getConfigurationSection("portal");
        if (portalSection == null) return;

        portalSection.getKeys(false).forEach(key -> {
            String base = key + ".";
            if (!portalSection.isInt(base + "x1") || !portalSection.isInt(base + "y1") || !portalSection.isInt(base + "z1") ||
                    !portalSection.isInt(base + "x2") || !portalSection.isInt(base + "y2") || !portalSection.isInt(base + "z2") ||
                    !portalSection.isString(base + "world") || !portalSection.isString(base + "type")) {
                // This is not a valid portal section.
                return;
            }
            int x1 = portalSection.getInt(base + "x1");
            int y1 = portalSection.getInt(base + "y1");
            int z1 = portalSection.getInt(base + "z1");

            int x2 = portalSection.getInt(base + "x2");
            int y2 = portalSection.getInt(base + "y2");
            int z2 = portalSection.getInt(base + "z2");

            String world = portalSection.getString(base + "world");
            String type = portalSection.getString(base + "type");

            // Make sure this type is valid.
            PortalType portalType = PortalType.findType(type);
            if (portalType == PortalType.UNKNOWN) return;

            // Check for a destination
            Optional<String> destination = Optional.ofNullable(storage.getString("link." + key, null));

            portals.add(new SpigotPortalMeta(key, x1, x2, y1, y2, z1, z2, world, destination, portalType));
        });
    }

    @Override
    public void createDimension(Dimension dimension) {

    }

    @Override
    public void removeDimension(String name) {

    }

    @Override
    public void loadDimensions() {

    }
}
