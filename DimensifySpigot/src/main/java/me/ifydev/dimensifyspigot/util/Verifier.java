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
