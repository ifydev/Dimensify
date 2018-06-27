package me.ifydev.dimensify.api.dimensions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

/**
 * @author Innectic
 * @since 06/26/2018
 */
@Getter
@AllArgsConstructor
public class Dimension {

    private String name;
    private String type;
    private Optional<String> meta;
    private boolean isDefault;
}
