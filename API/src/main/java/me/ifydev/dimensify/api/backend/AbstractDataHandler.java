package me.ifydev.dimensify.api.backend;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.ifydev.dimensify.api.dimensions.Dimension;
import me.ifydev.dimensify.api.portal.PortalMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Innectic
 * @since 06/26/2018
 */
@RequiredArgsConstructor
public abstract class AbstractDataHandler {

    @Getter protected List<Dimension> dimensions = new ArrayList<>();
    @Getter protected List<PortalMeta> portals = new ArrayList<>();
    @Getter protected final ConnectionInformation connectionInformation;

    /**
     * Setup anything that's needed before we can connect.
     */
    public abstract void initialize();

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

    public abstract void createPortal(PortalMeta meta);
    public abstract void removePortal(String name);
    public abstract void loadPortals();

    public abstract void createDimension(Dimension dimension);
    public abstract void removeDimension(String name);
    public abstract void loadDimensions();
}
