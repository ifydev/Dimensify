package me.ifydev.dimensify.api.backend.impl;

import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.backend.ConnectionInformation;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensify.api.portal.PortalMeta;
import me.ifydev.dimensify.api.portal.PortalType;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

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
            // TODO
            // PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }
        return Optional.empty();
    }

    @Override
    public void initialize() {
        this.dimensions = new ArrayList<>();

        try {
            Optional<Connection> connection = getConnection();
            if (!connection.isPresent()) return;

            String database = connectionInformation.getDatabase();

            if (!isUsingSQLite) {
                PreparedStatement statement = connection.get().prepareStatement("CREATE DATABASE IF NOT EXISTS " + database);
                statement.execute();
                statement.close();
                database += ".";
            } else database = "";

            PreparedStatement dimensionsStatement = connection.get().prepareStatement("CREATE TABLE IF NOT EXISTS " + database +
                    "dimensions (`name` VARCHAR(50) NOT NULL, `type` VARCHAR(20) NOT NULL, meta VARCHAR(767), `default` TINYINT NOT NULL)");
            dimensionsStatement.execute();
            dimensionsStatement.close();

            PreparedStatement portalsStatement = connection.get().prepareStatement("CREATE TABLE IF NOT EXISTS" + database +
                    "portals (x1 INTEGER NOT NULL, x2 INTEGER NOT NULL, y1 INTEGER NOT NULL, y2 INTEGER NOT NULL" +
                    ", z1 INTEGER NOT NULL, z2 INTEGER NOT NULL, destination VARCHAR(60), `type` VARCHAR(60) NOT NULL `name` VARCHAR(60) NOT NULL)");
            portalsStatement.execute();
            portalsStatement.close();

            connection.get().close();
        } catch (SQLException e) {
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
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) return;

        try {
            PreparedStatement statement = connection.get().prepareStatement("INSERT INTO portals " +
                    "(x1, x2, y1, y2, z1, z2, destination, `type`, `name`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setInt(1, meta.getX1());
            statement.setInt(2, meta.getY1());
            statement.setInt(3, meta.getZ1());

            statement.setInt(4, meta.getX2());
            statement.setInt(5, meta.getY2());
            statement.setInt(6, meta.getZ2());

            statement.setString(7, meta.getDestination().orElse(null));
            statement.setString(8, meta.getType().name());
            statement.setString(9, meta.getName());

            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removePortal(String name) {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) return;

        try {
            PreparedStatement statement = connection.get().prepareStatement("DELETE FROM portals WHERE name=?");
            statement.setString(1, name);

            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadDimensions() {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) return;

        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT * FROM dimensions");
            ResultSet results = statement.executeQuery();

            while (results.next())
                this.dimensions.add(new Dimension(
                        results.getString("name"), results.getString("type"),
                        Optional.ofNullable(results.getString("meta")), results.getBoolean("default")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadPortals() {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) return;

        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT * FROM dimensions");
            ResultSet results = statement.executeQuery();

            while (results.next())
                this.portals.add(new PortalMeta(
                        results.getString("name"),
                        results.getInt("x1"), results.getInt("y1"), results.getInt("z1"),
                        results.getInt("x2"), results.getInt("y2"), results.getInt("z2"),
                        results.getString("world"), Optional.ofNullable(results.getString("destination")),
                        PortalType.findType(results.getString("type"))
                ));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createDimension(Dimension dimension) {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) return;

        try {
            PreparedStatement statement = connection.get().prepareStatement("INSERT INTO dimensions (`name`, `type`, meta, `default`)" +
                    " VALUES (?, ?, ?, ?)");

            statement.setString(1, dimension.getName());
            statement.setString(2, dimension.getType());
            statement.setString(3, dimension.getMeta().orElse(null));
            statement.setBoolean(4, dimension.isDefault());

            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeDimension(String name) {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) return;

        try {
            PreparedStatement statement = connection.get().prepareStatement("DELETE FROM dimensions WHERE `name`=?");
            statement.setString(1, name);

            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
