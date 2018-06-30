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
    protected Map<String, String> destinations = new HashMap<>();
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
    public abstract List<PortalMeta> getPortals();

    public abstract boolean setPortalDestination(String portal, String destination);

    public abstract boolean createDimension(Dimension dimension);
    public abstract boolean removeDimension(String name);
    public abstract List<Dimension> getDimensions();

    public abstract Optional<Dimension> getDimension(String name);
    public abstract Optional<PortalMeta> getPortal(String name);

    public abstract boolean setDefaultDimension(String name);
    public abstract String getDefaultDimension(boolean skipCache);
}
