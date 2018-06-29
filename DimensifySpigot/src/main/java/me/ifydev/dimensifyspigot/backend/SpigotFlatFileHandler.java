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
import java.util.Collections;
import java.util.List;
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
            DimensifyMain.get().getLogger().severe("Could not save storage config!");
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        DimensifyMain plugin = DimensifyMain.get();
        File data = plugin.getDataFolder();

        // Ensure the files exist
        storageFile = new File(data, plugin.getConfig().getString("connection.file", "storage.yml"));
        if (!storageFile.exists()) {
            storageFile.getParentFile().mkdirs();
            plugin.saveResource("storage.yml", false);
        }
        storage = new YamlConfiguration();

        try {
            storage.load(storageFile);
        } catch (InvalidConfigurationException | IOException e) {
            plugin.getLogger().severe("Could not create storage.yml");
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

        this.portals = this.getPortals();
        this.dimensions = this.getDimensions();
    }

    @Override
    public void drop() {
        dimensions = new ArrayList<>();
        portals = new ArrayList<>();
    }

    @Override
    public boolean createPortal(PortalMeta meta) {
        // Make sure this does not exist in the cache already
        Optional<PortalMeta> portal = this.portals.stream().filter(m -> m.getName().equals(meta.getName())).findFirst();
        if (portal.isPresent()) return false;
        this.portals.add(meta);

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
        return true;
    }

    @Override
    public boolean removePortal(String name) {
        // Make sure the portal exists in the cache.
        Optional<PortalMeta> meta = this.portals.stream().filter(m -> m.getName().equals(name)).findFirst();
        if (!meta.isPresent()) return false;

        // Remove the portal from all caches
        this.portals.remove(meta.get());
        this.destinations.remove(name);

        // Remove the portal from the configs
        storage.set("portal." + name, null);
        storage.set("link." + name, null);

        saveStorage();
        return true;
    }

    @Override
    public List<PortalMeta> getPortals() {
        ConfigurationSection portalSection = storage.getConfigurationSection("portal");
        if (portalSection == null) return Collections.emptyList();

        List<PortalMeta> portals = new ArrayList<>();
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
        return portals;
    }

    @Override
    public void setPortalDestination(String portal, String destination) {
        this.destinations.put(portal, destination);

        storage.set("link." + portal, destination);
        saveStorage();
    }

    @Override
    public boolean createDimension(Dimension dimension) {
        Optional<Dimension> d = this.dimensions.stream().filter(dim -> dim.getName().equals(dimension.getName())).findFirst();
        if (d.isPresent()) return false;
        this.dimensions.add(dimension);

        String base = "dimensions." + dimension.getName();
        this.storage.set(base + ".name", dimension.getName());
        this.storage.set(base + ".type", dimension.getType());
        this.storage.set(base + ".meta", dimension.getMeta().orElse(""));
        this.storage.set(base + ".default", dimension.isDefault());

        saveStorage();
        return true;
    }

    @Override
    public boolean removeDimension(String name) {
        // Make sure it's cached
        Optional<Dimension> dimension = this.dimensions.stream().filter(d -> d.getName().equals(name)).findFirst();
        if (!dimension.isPresent()) return false;

        // Remove from the cache
        this.dimensions.remove(dimension.get());

        // Remove from file
        storage.set("dimensions." + name, null);
        saveStorage();
        return true;
    }

    @Override
    public List<Dimension> getDimensions() {
        ConfigurationSection dimensions = storage.getConfigurationSection("dimensions");

        List<Dimension> loadedDimensions = new ArrayList<>();
        dimensions.getKeys(false).forEach(dimension -> {
            if (!dimensions.isString(dimension + ".name") || !dimensions.isString(dimension + "type") ||
                    !dimensions.isString(dimension + "meta") || !dimensions.isBoolean(dimension + "default")) {
                // This isn't a valid dimension.
                return;
            }
            String name = dimensions.getString(dimension + ".name");
            String type = dimensions.getString(dimension + ".type");
            String meta = dimensions.getString(dimension + ".meta", null);
            boolean isDefault = dimensions.getBoolean(dimension + ".default");

            loadedDimensions.add(new Dimension(name, type, Optional.ofNullable(meta), isDefault));
        });
        return loadedDimensions;
    }

    @Override
    public Optional<Dimension> getDimension(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<PortalMeta> getPortal(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getDestinationForPortal(String portalName) {
        return Optional.empty();
    }
}
