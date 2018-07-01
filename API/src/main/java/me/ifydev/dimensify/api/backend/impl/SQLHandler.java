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

import me.ifydev.dimensify.api.DimensifyAPI;
import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.backend.ConnectionError;
import me.ifydev.dimensify.api.backend.ConnectionInformation;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensify.api.portal.PortalMeta;
import me.ifydev.dimensify.api.portal.PortalType;

import java.sql.*;
import java.util.*;

/**
 * @author Innectic
 * @since 06/26/2018
 */
public class SQLHandler extends AbstractDataHandler {

    private boolean isUsingSQLite = false;
    private String baseConnectionUrl;

    public SQLHandler(ConnectionInformation connectionInformation) {
        super(connectionInformation);

        String type;
        String databaseURL;

        if (connectionInformation.getMeta().containsKey("sqlite")) {
            type = "sqlite";
            Map sqliteData = (Map) connectionInformation.getMeta().get("sqlite");
            databaseURL = (String) sqliteData.get("file");
            isUsingSQLite = true;
        } else {
            type = "mysql";
            databaseURL = "//" + connectionInformation.getUrl() + ":" + connectionInformation.getPort();
        }
        baseConnectionUrl = "jdbc:" + type + ":" + databaseURL;
    }

    private Optional<Connection> getConnection() {
        try {
            if (isUsingSQLite) return Optional.ofNullable(DriverManager.getConnection(baseConnectionUrl));
            String connectionURL = baseConnectionUrl + "/" + connectionInformation.getDatabase();
            return Optional.ofNullable(DriverManager.getConnection(connectionURL, connectionInformation.getUsername(), connectionInformation.getPassword()));
        } catch (SQLException e) {
            DimensifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }
        return Optional.empty();
    }

    @Override
    public void initialize(String defaultWorld) {
        this.dimensions = new ArrayList<>();

        try {
            Connection connection;
            if (isUsingSQLite) connection = DriverManager.getConnection(baseConnectionUrl);
            else connection = DriverManager.getConnection(baseConnectionUrl, connectionInformation.getUsername(), connectionInformation.getPassword());
            if (connection == null) {
                DimensifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
                return;
            }

            String database = connectionInformation.getDatabase();

            if (!isUsingSQLite) {
                PreparedStatement statement = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS " + database);
                statement.execute();
                statement.close();
                database += ".";
            } else database = "";

            PreparedStatement dimensionsStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + database +
                    "dimensions (`name` VARCHAR(50) NOT NULL, `type` VARCHAR(20) NOT NULL, meta VARCHAR(767), `default` TINYINT NOT NULL)");
            dimensionsStatement.execute();
            dimensionsStatement.close();

            PreparedStatement portalsStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + database +
                    "portals (`world` VARCHAR(50) NOT NULL, x1 INTEGER NOT NULL, x2 INTEGER NOT NULL, y1 INTEGER NOT NULL, y2 INTEGER NOT NULL" +
                    ", z1 INTEGER NOT NULL, z2 INTEGER NOT NULL, destination VARCHAR(60), `type` VARCHAR(60) NOT NULL, `name` VARCHAR(60) NOT NULL)");
            portalsStatement.execute();
            portalsStatement.close();

            connection.close();
        } catch (SQLException e) {
            System.out.println("Encountered an error while trying to setup the database:");
            e.printStackTrace();
        }
    }

    @Override
    public boolean connect() {
        Optional<Connection> connection = getConnection();
        boolean connected = connection.isPresent();

        connection.ifPresent(c -> {
            try {
                c.close();
            } catch (SQLException ignored) {}
        });
        return connected;
    }

    @Override
    public void reload() {
        this.drop();

        this.portals = this.getPortals(true);
        this.dimensions = this.getDimensions(true);
    }

    @Override
    public void drop() {
        dimensions = new ArrayList<>();
        portals = new ArrayList<>();
    }

    @Override
    public boolean createPortal(PortalMeta meta) {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            DimensifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }
        this.portals.add(meta);

        try {
            PreparedStatement statement = connection.get().prepareStatement("INSERT INTO portals " +
                    "(world, x1, y1, z1, x2, y2, z2, destination, `type`, `name`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            statement.setString(1, meta.getWorld());
            statement.setInt(2, meta.getX1());
            statement.setInt(3, meta.getY1());
            statement.setInt(4, meta.getZ1());

            statement.setInt(5, meta.getX2());
            statement.setInt(6, meta.getY2());
            statement.setInt(7, meta.getZ2());

            statement.setString(8, meta.getDestination().orElse(null));
            statement.setString(9, meta.getType().name());
            statement.setString(10, meta.getName());

            statement.execute();
            statement.close();
            connection.get().close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removePortal(String name) {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            DimensifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("DELETE FROM portals WHERE `name`=?");
            statement.setString(1, name);

            statement.execute();
            statement.close();
            connection.get().close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<PortalMeta> getPortals(boolean skipCache) {
        if (!skipCache) return portals;

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) return Collections.emptyList();

        List<PortalMeta> portals = new ArrayList<>();
        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT * FROM portals");
            ResultSet results = statement.executeQuery();

            while (results.next())
                portals.add(new PortalMeta(
                        results.getString("name"),
                        results.getInt("x1"), results.getInt("x2"), results.getInt("y1"),
                        results.getInt("y2"), results.getInt("z1"), results.getInt("z2"),
                        results.getString("world"), Optional.ofNullable(results.getString("destination")),
                        PortalType.findType(results.getString("type"))
                ));
            results.close();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return portals;
    }

    @Override
    public boolean setPortalDestination(String portal, String destination) {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            DimensifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }

        Optional<PortalMeta> meta = this.getPortal(portal);
        if (!meta.isPresent()) return false;
        meta.get().setDestination(Optional.of(destination));

        try {
            PreparedStatement statement = connection.get().prepareStatement("UPDATE portals SET destination=? WHERE `name`=?");
            statement.setString(1, destination);
            statement.setString(2, portal);
            statement.execute();

            statement.close();
            connection.get().close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Dimension> getDimensions(boolean skipCache) {
        if (!skipCache) return this.dimensions;

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) return Collections.emptyList();

        List<Dimension> dimensions = new ArrayList<>();
        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT * FROM dimensions");
            ResultSet results = statement.executeQuery();

            while (results.next())
                dimensions.add(new Dimension(
                        results.getString("name"), results.getString("type"),
                        Optional.ofNullable(results.getString("meta")), results.getBoolean("default")));
            results.close();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return dimensions;
    }

    @Override
    public Optional<Dimension> getDimension(String name) {
        String defaultDimension = getDefaultDimension(false);
        if (name.equalsIgnoreCase(defaultDimension)) return Optional.of(new Dimension(defaultDimension, "", Optional.empty(), false));
        return this.dimensions.stream().filter(d -> d.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public Optional<PortalMeta> getPortal(String name) {
        return this.portals.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public boolean setDefaultDimension(String name) {
        this.dimensions.stream().filter(Dimension::isDefault).findFirst().ifPresent(dim -> dim.setDefault(false));

        Optional<Dimension> dimension = dimensions.stream().filter(d -> d.getName().equalsIgnoreCase(name)).findFirst();
        if (!dimension.isPresent()) return false;

        dimension.get().setDefault(true);

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            DimensifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }
        try {
            PreparedStatement removeCurrent = connection.get().prepareStatement("UPDATE dimensions SET `default`=0 WHERE `default`=1");
            removeCurrent.execute();
            removeCurrent.close();

            PreparedStatement update = connection.get().prepareStatement("UPDATE dimensions SET `default`=1 WHERE `name`=?");
            update.setString(1, dimension.get().getName());
            update.execute();
            update.close();
            connection.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public String getDefaultDimension(boolean skipCache) {
        if (!skipCache)
            for (Dimension dimension : this.getDimensions(false)) if (dimension.isDefault()) return dimension.getName();
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) return "";

        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT * FROM dimensions WHERE `default`=1");
            ResultSet results = statement.executeQuery();

            if (results.first()) return results.getString("name");
            results.close();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean createDimension(Dimension dimension) {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            DimensifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }

        if (dimensions.stream().anyMatch(d -> d.getName().equalsIgnoreCase(dimension.getName()))) return false;
        this.dimensions.add(dimension);

        try {
            PreparedStatement statement = connection.get().prepareStatement("INSERT INTO dimensions (`name`, `default`, `type`, `meta`)" +
                    " VALUES (?, ?, ?, ?)");

            statement.setString(1, dimension.getName());
            statement.setBoolean(2, dimension.isDefault());
            statement.setString(3, dimension.getType());
            statement.setString(4, dimension.getMeta().orElse(""));

            statement.execute();
            statement.close();
            connection.get().close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeDimension(String name) {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            DimensifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }

        Optional<Dimension> dimension = this.dimensions.stream().filter(d -> d.getName().equalsIgnoreCase(name)).findFirst();
        if (!dimension.isPresent()) return false;
        dimensions.remove(dimension.get());

        try {
            PreparedStatement statement = connection.get().prepareStatement("DELETE FROM dimensions WHERE `name`=?");
            statement.setString(1, name);
            statement.execute();

            statement.close();
            connection.get().close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
