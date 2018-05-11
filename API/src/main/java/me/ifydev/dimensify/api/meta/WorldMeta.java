package me.ifydev.dimensify.api.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

/**
 * @author Innectic
 * @since 5/10/2018
 */
@AllArgsConstructor
@Getter
public class WorldMeta {
    private boolean structures;
    private Optional<String> environment;
    private Optional<String> seed;
}
