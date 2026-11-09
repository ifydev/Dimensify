/*
 *
 * This file is part of Dimensify, licensed under the MIT License (MIT).
 * Copyright (c) Innectic
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.ifydev.dimensify.api.backend.impl;

import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.backend.ConnectionInformation;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensify.api.portal.PortalMeta;
import me.ifydev.dimensify.api.portal.PortalType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SQLHandler extends AbstractDataHandler {

    private final boolean sqlite;
    private final String url;
    private String defaultWorld;

    public SQLHandler(ConnectionInformation connectionInformation) {
        super(connectionInformation);
        if (connectionInformation == null) throw new IllegalArgumentException("Database connection settings are missing.");
        sqlite = connectionInformation.getMeta().containsKey("sqlite");
        if (sqlite) {
            Map<?, ?> meta = (Map<?, ?>) connectionInformation.getMeta().get("sqlite");
            url = "jdbc:sqlite:" + meta.get("file");
        } else {
            if (!connectionInformation.getDatabase().matches("[A-Za-z0-9_]+")) throw new IllegalArgumentException("Invalid database name.");
            url = "jdbc:mysql://" + connectionInformation.getUrl() + ":" + connectionInformation.getPort() + "/" + connectionInformation.getDatabase();
        }
    }

    private Connection connection() throws SQLException {
        return sqlite ? DriverManager.getConnection(url)
                : DriverManager.getConnection(url, connectionInformation.getUsername(), connectionInformation.getPassword());
    }

    @Override
    public void initialize(String defaultWorld) {
        this.defaultWorld = defaultWorld;
        try {
            Class.forName(sqlite ? "org.sqlite.JDBC" : "com.mysql.cj.jdbc.Driver");
            try (Connection connection = connection(); Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS dimensions (`name` VARCHAR(50) NOT NULL, `type` VARCHAR(20) NOT NULL, meta VARCHAR(767), `default` TINYINT NOT NULL)");
                statement.execute("CREATE TABLE IF NOT EXISTS portals (`world` VARCHAR(50) NOT NULL, x1 INTEGER NOT NULL, x2 INTEGER NOT NULL, y1 INTEGER NOT NULL, y2 INTEGER NOT NULL, z1 INTEGER NOT NULL, z2 INTEGER NOT NULL, destination VARCHAR(60), `type` VARCHAR(60) NOT NULL, `name` VARCHAR(60) NOT NULL)");
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new IllegalStateException("Could not initialize database. For MySQL, create the configured database first.", e);
        }
    }

    @Override
    public boolean connect() {
        try (Connection connection = connection()) {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void reload() {
        List<Dimension> loadedDimensions = getDimensions(true);
        List<PortalMeta> loadedPortals = getPortals(true);
        dimensions = loadedDimensions;
        portals = loadedPortals;
    }

    @Override
    public void drop() {
        dimensions = new ArrayList<>();
        portals = new ArrayList<>();
    }

    private boolean update(String sql, Object... values) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            if (statement.executeUpdate() == 0) return false;
            reload();
            return true;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update database.", e);
        }
    }

    @Override
    public boolean createPortal(PortalMeta meta) {
        if (getPortal(meta.getName()).isPresent()) return false;
        return update("INSERT INTO portals (world, x1, y1, z1, x2, y2, z2, destination, `type`, `name`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                meta.getWorld(), meta.getX1(), meta.getY1(), meta.getZ1(), meta.getX2(), meta.getY2(), meta.getZ2(),
                meta.getDestination().orElse(null), meta.getType().name(), meta.getName());
    }

    @Override
    public boolean removePortal(String name) {
        Optional<PortalMeta> portal = getPortal(name);
        return portal.isPresent() && update("DELETE FROM portals WHERE `name`=?", portal.get().getName());
    }

    @Override
    public List<PortalMeta> getPortals(boolean skipCache) {
        if (!skipCache) return new ArrayList<>(portals);
        List<PortalMeta> loaded = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM portals"); ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                loaded.add(new PortalMeta(results.getString("name"), results.getInt("x1"), results.getInt("x2"),
                        results.getInt("y1"), results.getInt("y2"), results.getInt("z1"), results.getInt("z2"), results.getString("world"),
                        Optional.ofNullable(results.getString("destination")), PortalType.findType(results.getString("type"))));
            }
            return loaded;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load portals.", e);
        }
    }

    @Override
    public boolean setPortalDestination(String portal, String destination) {
        Optional<PortalMeta> meta = getPortal(portal);
        if (!meta.isPresent()) return false;
        if (meta.get().getDestination().filter(destination::equals).isPresent()) return true;
        return update("UPDATE portals SET destination=? WHERE `name`=?", destination, meta.get().getName());
    }

    @Override
    public List<Dimension> getDimensions(boolean skipCache) {
        if (!skipCache) return new ArrayList<>(dimensions);
        List<Dimension> loaded = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM dimensions"); ResultSet results = statement.executeQuery()) {
            while (results.next()) loaded.add(new Dimension(results.getString("name"), results.getString("type"),
                    Optional.ofNullable(results.getString("meta")), results.getBoolean("default")));
            return loaded;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load worlds.", e);
        }
    }

    @Override
    public Optional<Dimension> getDimension(String name) {
        return dimensions.stream().filter(dimension -> dimension.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public Optional<PortalMeta> getPortal(String name) {
        return portals.stream().filter(portal -> portal.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public boolean setDefaultDimension(String name) {
        Optional<Dimension> dimension = getDimension(name);
        if (!dimension.isPresent()) return false;
        try (Connection connection = connection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement reset = connection.prepareStatement("UPDATE dimensions SET `default`=0");
                 PreparedStatement set = connection.prepareStatement("UPDATE dimensions SET `default`=1 WHERE `name`=?")) {
                reset.executeUpdate();
                set.setString(1, dimension.get().getName());
                if (set.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not set default world.", e);
        }
        reload();
        return true;
    }

    @Override
    public String getDefaultDimension(boolean skipCache) {
        return getDimensions(skipCache).stream().filter(Dimension::isDefault).map(Dimension::getName).findFirst().orElse(defaultWorld);
    }

    @Override
    public boolean createDimension(Dimension dimension) {
        if (getDimension(dimension.getName()).isPresent()) return false;
        return update("INSERT INTO dimensions (`name`, `default`, `type`, meta) VALUES (?, ?, ?, ?)",
                dimension.getName(), dimension.isDefault(), dimension.getType(), dimension.getMeta().orElse(""));
    }

    @Override
    public boolean removeDimension(String name) {
        Optional<Dimension> dimension = getDimension(name);
        return dimension.isPresent() && update("DELETE FROM dimensions WHERE `name`=?", dimension.get().getName());
    }
}