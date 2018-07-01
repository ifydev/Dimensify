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
package me.ifydev.dimensify.api.backend;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensify.api.portal.PortalMeta;

import java.util.*;

/**
 * @author Innectic
 * @since 06/26/2018
 */
@RequiredArgsConstructor
public abstract class AbstractDataHandler {

    protected List<Dimension> dimensions = new ArrayList<>();
    protected List<PortalMeta> portals = new ArrayList<>();
    @Getter protected final ConnectionInformation connectionInformation;

    /**
     * Setup anything that's needed before we can connect.
     */
    public abstract void initialize(String defaultWorld);

    /**
     * Connect to whatever kind of handler we're going for.
     *
     * @return if it was successful.
     */
    public abstract boolean connect();

    /**
     * Force all data within the cache to be reloaded.
     */
    public abstract void reload();

    /**
     * Drop all values from the cache.
     */
    public abstract void drop();

    public abstract boolean createPortal(PortalMeta meta);
    public abstract boolean removePortal(String name);
    public abstract List<PortalMeta> getPortals(boolean skipCache);

    public abstract boolean setPortalDestination(String portal, String destination);

    public abstract boolean createDimension(Dimension dimension);
    public abstract boolean removeDimension(String name);
    public abstract List<Dimension> getDimensions(boolean skipCache);

    public abstract Optional<Dimension> getDimension(String name);
    public abstract Optional<PortalMeta> getPortal(String name);

    public abstract boolean setDefaultDimension(String name);
    public abstract String getDefaultDimension(boolean skipCache);
}
