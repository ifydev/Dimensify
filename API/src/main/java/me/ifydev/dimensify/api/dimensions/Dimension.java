package me.ifydev.dimensify.api.dimensions;

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
public class Dimension {

    private String name;
    private String type;
    private Optional<String> meta;
    @Setter private boolean isDefault;

    @Override
    public String toString() {
        return "Dimension [" +
                "name=" + name +
                ", type=" + type +
                ", meta=" + meta +
                ", isDefault=" + isDefault +
                "]";
    }
}
