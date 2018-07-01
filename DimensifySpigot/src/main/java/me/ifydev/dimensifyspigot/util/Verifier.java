package me.ifydev.dimensifyspigot.util;

import me.ifydev.dimensify.api.backend.BackendType;
import me.ifydev.dimensify.api.backend.ConnectionInformation;
import me.ifydev.dimensify.api.backend.impl.SQLHandler;
import me.ifydev.dimensifyspigot.DimensifyMain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Innectic
 * @since 06/27/2018
 */
public class Verifier {

    public static Optional<ConnectionInformation> verifyConnectionInformation(BackendType type) {
        DimensifyMain plugin = DimensifyMain.get();
        Optional<ConnectionInformation> connectionInformation = Optional.empty();

        if (type.getHandler().isPresent() && type.getHandler().get() == SQLHandler.class) {
            if (type.getDisplayName().equalsIgnoreCase("mysql")) {
                if (plugin.getConfig().getString("connection.host") == null) return Optional.empty();
                if (plugin.getConfig().getString("connection.database") == null) return Optional.empty();
                if (plugin.getConfig().getString("connection.port") == null) return Optional.empty();
                if (plugin.getConfig().getString("connection.username") == null) return Optional.empty();
                if (plugin.getConfig().getString("connection.password") == null) return Optional.empty();

                connectionInformation = Optional.of(new ConnectionInformation(
                        plugin.getConfig().getString("connection.host"),
                        plugin.getConfig().getString("connection.database"),
                        plugin.getConfig().getInt("connection.port"),
                        plugin.getConfig().getString("connection.username"),
                        plugin.getConfig().getString("connection.password"),
                        new HashMap<>())
                );
            } else if (type.getDisplayName().equalsIgnoreCase("sqlite")) {
                if (plugin.getConfig().getString("connection.file") == null) return Optional.empty();

                Map<String, Object> sqliteMeta = new HashMap<>();
                sqliteMeta.put("file", plugin.getDataFolder() + "/" + plugin.getConfig().getString("connection.file"));
                Map<String, Object> meta = new HashMap<>();
                meta.put(type.getDisplayName().toLowerCase(), sqliteMeta);

                connectionInformation = Optional.of(new ConnectionInformation("", "", 0, "", "", meta));
            }
        }
        return connectionInformation;
    }
}
