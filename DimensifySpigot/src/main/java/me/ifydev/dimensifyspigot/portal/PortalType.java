package me.ifydev.dimensifyspigot.portal;

import lombok.AllArgsConstructor;

/**
 * @author Innectic
 * @since 06/23/2018
 */
@AllArgsConstructor
public enum PortalType {
    // TODO: Someday have "custom" portals

    UNKNOWN, NETHER, END;

    public static PortalType findType(String check) {
        try {
            return valueOf(check.toUpperCase());
        } catch (Exception e) {
            return PortalType.UNKNOWN;
        }
    }
}
