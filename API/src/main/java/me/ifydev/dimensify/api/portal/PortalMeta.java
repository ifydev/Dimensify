package me.ifydev.dimensify.api.portal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * @author Innectic
 * @since 06/26/2018
 */

@Getter
@AllArgsConstructor
public class PortalMeta {

    private String name;
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private int z1;
    private int z2;
    private String world;
    @Setter private Optional<String> destination;
    private PortalType type;
}
