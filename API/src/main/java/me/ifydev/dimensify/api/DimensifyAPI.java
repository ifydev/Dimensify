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
package me.ifydev.dimensify.api;

import lombok.Getter;
import me.ifydev.dimensify.api.backend.AbstractDataHandler;
import me.ifydev.dimensify.api.backend.BackendType;
import me.ifydev.dimensify.api.backend.ConnectionInformation;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * @author Innectic
 * @since 10/1/2017
 *
 * The main API class of Dimensify.
 */
public class DimensifyAPI {

    private static Optional<DimensifyAPI> api;

    @Getter private Optional<AbstractDataHandler> databaseHandler;

    public void initialize(String defaultWorld, Class<? extends AbstractDataHandler> handler, BackendType backendType, Optional<ConnectionInformation> connectionInformation) throws Exception {
        api = Optional.of(this);

        try {
            // If we don't have a database handler, then we default to the flat-file handler.
            Class<? extends AbstractDataHandler> clazz = backendType.getHandler().orElse(handler);
            databaseHandler = Optional.of(clazz.getConstructor(ConnectionInformation.class).newInstance(connectionInformation.orElse(null)));
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (!databaseHandler.isPresent()) throw new Exception("No data handler present.");

        databaseHandler.ifPresent(h -> {
            System.out.println("Using " + defaultWorld + " as the default world...");
            h.initialize(defaultWorld);
            h.reload();
            if (h.connect()) System.out.println("Connected to database!");
            else System.out.println("Unable to connect to database!");
        });
    }

    public static Optional<DimensifyAPI> get() {
        return api;
    }
}
